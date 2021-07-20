package com.jason.hook.helper

import android.content.Context
import android.util.Log
import com.jason.hook.app.HookApplication
import dalvik.system.PathClassLoader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

object DexPluginHelper {
    private const val APK_DIR = "third_apk"
    private const val FILE_FILTER = ".apk"
    private const val ASSETS_PLUGINS = "plugins"
    private const val TAG = "DexPluginHelper"
    fun copyAllAssetsApk() {
        val startTime = System.currentTimeMillis()
        try {
            HookApplication.instance.run {

                val dex = getDir(APK_DIR, Context.MODE_PRIVATE)
                if (!dex.exists()) {//目录不存在,创建目录,私有的存放插件的apk
                    dex.mkdir()
                }
                if (assets.list(ASSETS_PLUGINS) != null) {
                    for (file in assets.list(ASSETS_PLUGINS)!!) {//遍历目录下的文件
                        if (file.endsWith(FILE_FILTER)) {//是否以.apk结尾
                            val ins: InputStream = assets.open("$ASSETS_PLUGINS/$file")
                            val dexFile = File(dex, file)
                            if (dex.exists() && dexFile.length()
                                    .toInt() == ins.available()
                            ) {//是否已经在私有目录下
                                Log.i(TAG, "$$file no change")
                                continue
                            }
                            Log.i(TAG, "$$file change")

                            val ous: OutputStream = FileOutputStream(dexFile)
                            val buffer = ByteArray(2048)
                            var read: Int = ins.read(buffer)
                            while (read != -1) {
                                ous.write(buffer, 0, read)
                                read = ins.read(buffer)
                            }

                            ins.close()
                            ous.flush()
                            ous.close()
                            Log.i(TAG, "$file copy over")
                        }
                    }
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.e(TAG, "copy apk to 3rd time is ${System.currentTimeMillis() - startTime}")
    }

    fun installDexPlugin() {
        HookApplication.instance.run {

            val dir = getDir(APK_DIR, Context.MODE_PRIVATE)
            var dexPath = ""
            if (dir.exists() && dir.isDirectory) {
                for (file in dir.list()) {
                    //将文件添加到目录中去
                    dexPath += if (dexPath.isEmpty()) {
                        "$dir/$file"
                    } else {
                        ":$dir/$file"
                    }
                }
            }
            if (dexPath.isNotEmpty()) {//有插件，则进行加载

                //1. 先获取宿主的dexPathList
                val baseDexClassLoaderClass = Class.forName("dalvik.system.BaseDexClassLoader")
                val pathListField = baseDexClassLoaderClass.getDeclaredField("pathList")
                pathListField.isAccessible = true
                val hostPathList = pathListField.get(classLoader)

                //2. 获取pathList中的Elements数组，也就是dexElements
                val dexPathListClass = Class.forName("dalvik.system.DexPathList")
                val dexElementsField = dexPathListClass.getDeclaredField("dexElements")
                dexElementsField.isAccessible = true
                val hostDexElements = dexElementsField.get(hostPathList) as Array<*>

                //3. 获取插件类的dexPathList
                val pluginPathClassLoader = PathClassLoader(dexPath, classLoader)
                val pluginPathList = pathListField.get(pluginPathClassLoader)

                //4. 获取插件类的dexElements
                val pluginDexElements = dexElementsField.get(pluginPathList) as Array<*>

                //将宿主类的dexElements和插件类的dexElements合并
                val newDexElements =
                    java.lang.reflect.Array.newInstance(
                        hostDexElements::class.java.componentType!!,
                        hostDexElements.size + pluginDexElements.size
                    )
                System.arraycopy(hostDexElements, 0, newDexElements, 0, hostDexElements.size)
                System.arraycopy(
                    pluginDexElements,
                    0,
                    newDexElements,
                    hostDexElements.size,
                    pluginDexElements.size
                )
                dexElementsField.set(hostPathList, newDexElements)

            }
        }


    }
}