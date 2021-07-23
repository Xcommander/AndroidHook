package com.jasonxu.dexplugin.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources

object LoadResourcesHelper {
    @SuppressLint("DiscouragedPrivateApi")
    fun loadResourcesPlugin(context: Context) {

        //新建一个AssetManager，来替换Resources中的AssetManager
        //1.通过反射，拿到构造函数，然后初始化AssetManager。
        val assetManager = AssetManager::class.java.newInstance()
        val addAssetPathMethod = AssetManager::class.java.getDeclaredMethod("addAssetPath",String::class.java)
        //2. 通过反射，将资源目录设置进去AssetManager
        val dir = context.getDir("third_apk", Context.MODE_PRIVATE)
        val resourcePath = "$dir/dexplugin-debug.apk"
        addAssetPathMethod.invoke(assetManager, resourcePath)

        //3. AssetManager替换到resources。
        // 通过新建一个Resources对象，内部会创建一个新的ResourcesImpl对象，这时候，会将我们的AssetManager初始化进去
        val resources = context.resources
        val newResources =
            Resources(assetManager, resources.displayMetrics, resources.configuration)

        //4. 将新的resources对象，替换到context中,这个context其实是实现类ContextImpl
        val contextClass = context.javaClass
        val mResourcesField = contextClass.getDeclaredField("mResources")
        mResourcesField.isAccessible = true
        mResourcesField.set(context, newResources)

    }
}