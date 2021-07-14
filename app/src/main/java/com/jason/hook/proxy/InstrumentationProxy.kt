package com.jason.hook.proxy

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import java.lang.RuntimeException

//通过代理模式（静态代理类），来修改/替换Hook的对象中的功能，然后替换
class InstrumentationProxy(mInstrumentation: Instrumentation) : Instrumentation() {
    private var mBase: Instrumentation = mInstrumentation
    fun execStartActivity(
        who: Context, contextThread: IBinder, token: IBinder, target: Activity,
        intent: Intent, requestCode: Int, options: Bundle
    ): ActivityResult {
        Log.e("xulinchao", "我们Hook了 Activity的启动流程")
        //通过反射，调用Instrumentation真正的启动流程
        try {
            val execStartActivityMethod =
                Instrumentation::class.java.getDeclaredMethod(
                    "execStartActivity",
                    Context::class.java,
                    IBinder::class.java,
                    IBinder::class.java,
                    Activity::class.java,
                    Intent::class.java,
                    Int::class.java,
                    Bundle::class.java
                )
            execStartActivityMethod.isAccessible = true
            return execStartActivityMethod.invoke(
                mBase,
                who,
                contextThread,
                token,
                target,
                intent,
                requestCode,
                options
            ) as ActivityResult
        } catch (e: Exception) {
            throw RuntimeException("出问题了，快去适配")
        }

    }

}