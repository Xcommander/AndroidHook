package com.jason.hook.helper

import android.annotation.SuppressLint
import android.app.Instrumentation
import android.os.Build
import android.util.Log
import com.jason.hook.app.HookApplication
import com.jason.hook.proxy.IActivityManagerProxy
import com.jason.hook.proxy.InstrumentationProxy

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

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    fun hookIActivityManager() {
        //对于不同api，IActivityManager不一样
        if (Build.VERSION.SDK_INT >= 30) {
            //对于api为30，IActivityManager为IActivityTaskManager.IActivityTaskManager来自于singleton，
            // 而singleton是静态变量，来自于ActivityTaskManager.IActivityTaskManagerSingleton
            // 1、获取IActivityTaskManagerSingleton对象
            val activityTaskManagerClass = Class.forName("android.app.ActivityTaskManager")
            val iActivityTaskManagerSingletonField =
                activityTaskManagerClass.getDeclaredField("IActivityTaskManagerSingleton")
            iActivityTaskManagerSingletonField.isAccessible = true
            val iActivityTaskManagerSingleton = iActivityTaskManagerSingletonField.get(null)


            //2、从iActivityTaskManagerSingleton获取IActivityTaskActivity
            val singletonClass = Class.forName("android.util.Singleton")
            val mInstanceField=singletonClass.getDeclaredField("mInstance")
            val getMethod = singletonClass.getDeclaredMethod("get")
            getMethod.isAccessible = true
            val iActivityTaskManager = getMethod.invoke(iActivityTaskManagerSingleton)


            //获取IActivityTaskManager的class对象
            val iActivityTaskManagerClass = Class.forName("android.app.IActivityTaskManager")

            //3、获取代理
            val proxy = IActivityManagerProxy.newProxyInstance(
                HookApplication.instance.classLoader,
                arrayOf(iActivityTaskManagerClass), IActivityManagerProxy(iActivityTaskManager)
            )
            //4、偷梁换柱
            mInstanceField.set(iActivityTaskManager,proxy)

        }

    }


}