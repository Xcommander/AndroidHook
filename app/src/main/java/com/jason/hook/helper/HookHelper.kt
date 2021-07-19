package com.jason.hook.helper

import android.annotation.SuppressLint
import android.app.Instrumentation
import android.os.Build
import android.os.Handler
import com.jason.hook.app.HookApplication
import com.jason.hook.proxy.HCallbackProxy
import com.jason.hook.proxy.IActivityManagerProxy
import com.jason.hook.proxy.InstrumentationProxy
import dalvik.system.DexClassLoader
import dalvik.system.PathClassLoader

object HookHelper {
    //Hook mInstrumentation
    @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
    fun hookInstrumentation() {
        //通过反射，先拿到ActivityThread对象
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val currentActivityThreadMethod =
            activityThreadClass.getDeclaredMethod("currentActivityThread")
        currentActivityThreadMethod.isAccessible = true
        val currentActivityThread = currentActivityThreadMethod.invoke(null)
        //再拿到mInstrumentation字段
        val mInstrumentationField = activityThreadClass.getDeclaredField("mInstrumentation")
        mInstrumentationField.isAccessible = true
        val mInstrumentation = mInstrumentationField.get(currentActivityThread) as Instrumentation

        //通过代理类，植入代码
        val instrumentationProxy =
            InstrumentationProxy(mInstrumentation, HookApplication.instance.packageManager)

        //偷梁换柱：将原来的mInstrumentation替换成代理类
        mInstrumentationField.set(currentActivityThread, instrumentationProxy)
    }


    fun hookAMS() {
        //Hook Activity启动点，Activity还原点
        hookIActivityManager()
        hookCallback()
    }

    //Hook IActivityManager
    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    private fun hookIActivityManager() {
        //对于不同api，IActivityManager不一样

        //对于api为29以上，IActivityManager为ActivityTaskManager中的Singleton
        //对于api为26-28，IActivityManager为ActivityManager的Singleton
        //对于api为26以下，IActivityManager为ActivityManagerNative的Singleton

        val activityManagerCLassName = when {
            Build.VERSION.SDK_INT >= 29 -> {
                "android.app.ActivityTaskManager"
            }
            Build.VERSION.SDK_INT >= 26 -> {
                "android.app.ActivityManager"
            }
            else -> {
                "android.app.ActivityManagerNative"
            }
        }
        val iActivityManagerSingletonFieldName = when {
            Build.VERSION.SDK_INT >= 29 -> {
                "IActivityTaskManagerSingleton"
            }
            Build.VERSION.SDK_INT >= 26 -> {
                "IActivityManagerSingleton"
            }
            else -> {
                "gDefault"
            }
        }

        val iActivityManagerClassName = if (Build.VERSION.SDK_INT >= 29) {
            "android.app.IActivityTaskManager"
        } else {
            "android.app.IActivityManager"
        }
        // 而singleton是静态变量，来自于ActivityTaskManager.IActivityTaskManagerSingleton
        // 1、获取IActivityTaskManagerSingleton对象
        val activityManagerClass = Class.forName(activityManagerCLassName)
        val iActivityManagerSingletonField =
            activityManagerClass.getDeclaredField(iActivityManagerSingletonFieldName)
        iActivityManagerSingletonField.isAccessible = true
        val iActivityManagerSingleton = iActivityManagerSingletonField.get(null)


        //2、从iActivityTaskManagerSingleton获取IActivityTaskManager
        val singletonClass = Class.forName("android.util.Singleton")
        val mInstanceField = singletonClass.getDeclaredField("mInstance")
        mInstanceField.isAccessible = true
        //这里注意：为什么不从mInstanceField拿？
        // 是因为mInstance拿到的null，此时Singleton没有初始化好。所以我们主动调用get方法，通过getMethod来拿到iActivityTaskManager
        val getMethod = singletonClass.getDeclaredMethod("get")
        getMethod.isAccessible = true
        val iActivityTaskManager = getMethod.invoke(iActivityManagerSingleton)


        //获取IActivityTaskManager的class对象
        val iActivityManagerClass = Class.forName(iActivityManagerClassName)

        //3、获取代理
        val proxy = IActivityManagerProxy.newProxyInstance(
            Thread.currentThread().contextClassLoader,
            arrayOf(iActivityManagerClass), IActivityManagerProxy(iActivityTaskManager)
        )
        //4、偷梁换柱
        mInstanceField.set(iActivityManagerSingleton, proxy)

    }

    //Hook ActivityThread中Handler的mCallback
    @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
    private fun hookCallback() {
        //先获取ActivityThread
        //通过反射，先拿到ActivityThread对象
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val currentActivityThreadMethod =
            activityThreadClass.getDeclaredMethod("currentActivityThread")
        currentActivityThreadMethod.isAccessible = true
        val currentActivityThread = currentActivityThreadMethod.invoke(null)

        //获取Handler对象
        val mHField = activityThreadClass.getDeclaredField("mH")
        mHField.isAccessible = true
        val mH = mHField.get(currentActivityThread) as Handler

        //从Handler对象中获取mCallback
        val mHClass = Class.forName("android.os.Handler")
        val mCallbackField = mHClass.getDeclaredField("mCallback")
        mCallbackField.isAccessible = true


        //进行替换
        val proxy = HCallbackProxy(mH)
        mCallbackField.set(mH, proxy)

    }


}