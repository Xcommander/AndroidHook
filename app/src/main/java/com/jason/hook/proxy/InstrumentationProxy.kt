package com.jason.hook.proxy

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import com.jason.hook.Constant

//通过代理模式（静态代理类），来修改/替换Hook的对象中的功能，然后替换
open class InstrumentationProxy(mInstrumentation: Instrumentation, packageManager: PackageManager) :
    Instrumentation() {
    private var mBase: Instrumentation = mInstrumentation
    private var mPackageManager: PackageManager = packageManager

    //执行启动Activity流程
    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("QueryPermissionsNeeded")
    open fun execStartActivity(
        who: Context, contextThread: IBinder?, token: IBinder?, target: Activity?,
        intent: Intent, requestCode: Int, options: Bundle?
    ): ActivityResult? {
        Log.e("xulinchao", "我们Hook了 Activity的启动流程")

        //判断启动的Activity，是否在AndroidManifest.xml注册，没有注册，则通过占位Activity帮助其绕过AMS的检查
        //1. 查询是否注册
        val resolveInfoList =
            mPackageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        if (resolveInfoList.size == 0) {//2. 未查询到，则需要占位Activity来帮忙
            //3. 保存目标activity的类名，后面使用
            intent.putExtra(Constant.TARGET_ACTIVITY, intent.component?.className)
            //4. 将占位activity替换进去，用于AMS检查
            intent.setClassName(who, Constant.STUB_ACTIVITY)
        }


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
            ) as? ActivityResult
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("出问题了，快去适配")
        }
    }

    //创建Activity
    @Throws(
        InstantiationException::class,
        IllegalAccessException::class,
        ClassNotFoundException::class
    )
    override fun newActivity(
        cl: ClassLoader?, className: String?,
        intent: Intent?
    ): Activity? {
        Log.e("xulinchao", "new Activity")
        val targetName = intent?.getStringExtra(Constant.TARGET_ACTIVITY)

        return mBase.newActivity(
            cl, if (!TextUtils.isEmpty(targetName)) {//不为null，则说明这个没有注册的activity
                targetName
            } else {
                className
            }, intent
        )
    }
}