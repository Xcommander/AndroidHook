package com.jason.hook.proxy

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Message
import com.jason.hook.Constant
import com.jason.hook.app.HookApplication


class HCallbackProxy(private val handler: Handler) : Handler.Callback {
    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            100 -> {//属于API 28之前的做法
                handleLaunchActivity(msg)
            }
            159 -> {//属于API 28及后续做法
                handleLaunchActivityItem(msg)
            }
        }
        handler.handleMessage(msg)
        return true;
    }

    //API 28之前的处理方式
    private fun handleLaunchActivity(msg: Message) {
        //获取intent
        val r = msg.obj
        val intentField = r::class.java.getDeclaredField("intent")
        intentField.isAccessible = true
        val intent = intentField.get(r) as Intent
        //从intent拿到数据,进行还原
        val target = intent.getStringExtra(Constant.TARGET_ACTIVITY)
        if (target != null && target != "") {
            intent.setClassName(HookApplication.instance.packageName, target)
        }
    }

    //API 28之后的处理方式，各种状态机模式。
    // LaunchActivityItem，PauseActivityItem等，而这些状态机都存放在ClientTransaction的mActivityCallbacks
    @SuppressLint("PrivateApi")
    private fun handleLaunchActivityItem(msg: Message) {
        //先获取ClientTransaction
        val transaction = msg.obj
        //获取状态机列表
        val clientTransactionClass =
            Class.forName("android.app.servertransaction.ClientTransaction")
        val mActivityCallbacksField = clientTransactionClass.getDeclaredField("mActivityCallbacks")
        mActivityCallbacksField.isAccessible = true
        val mActivityCallbacks = mActivityCallbacksField.get(transaction)
        if (mActivityCallbacks != null && mActivityCallbacks is List<*> && mActivityCallbacks.size > 0) {
            val launchActivityItemClassName = "android.app.servertransaction.LaunchActivityItem"
            //从状态机列表中，找到启动activity的状态机。也就是LaunchActivityItem
            if (mActivityCallbacks[0]!!::class.java.canonicalName == launchActivityItemClassName) {
                val launchActivityItem = mActivityCallbacks[0]

                //从LaunchActivityItem找到miIntent进行修改
                val mIntentField =
                    Class.forName(launchActivityItemClassName).getDeclaredField("mIntent")
                mIntentField.isAccessible = true
                val mIntent = mIntentField.get(launchActivityItem) as Intent
                val target = mIntent.getStringExtra(Constant.TARGET_ACTIVITY)
                if (target != null && target != "") {//则说明这个是插件Activity
                    //还原为插件Activity
                    val targetPKG = mIntent.getStringExtra(Constant.DEX_PLUGIN)

                    mIntent.setClassName(HookApplication.instance.packageName, target)
                }
            }
        }

    }
}