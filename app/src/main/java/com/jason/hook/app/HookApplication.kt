package com.jason.hook.app

import android.app.Application
import android.content.Context
import com.jason.hook.helper.HookHelper

class HookApplication : Application() {
    companion object {
        lateinit var instance: HookApplication
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        instance = this
        HookHelper.hookAMS()
    }

}