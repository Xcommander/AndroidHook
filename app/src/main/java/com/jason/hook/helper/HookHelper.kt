package com.jason.hook.helper

import android.app.Instrumentation
import com.jason.hook.proxy.InstrumentationProxy

object HookHelper {
    //Hook mInstrumentation
    fun attachContext() {
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
        val instrumentationProxy = InstrumentationProxy(mInstrumentation)

        //偷梁换柱：将原来的mInstrumentation替换成代理类
        mInstrumentationField.set(currentActivityThread, instrumentationProxy)


    }
}