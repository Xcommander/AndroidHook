package com.jasonxu.dexplugin

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.ContextThemeWrapper
import com.jasonxu.dexplugin.helper.LoadResourcesHelper

open class BaseActivity : AppCompatActivity() {
    lateinit var mContext: Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseContext::class.java
        mContext = ContextThemeWrapper(baseContext, 0)
        LoadResourcesHelper.loadResourcesPlugin(mContext)
    }

}