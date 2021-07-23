package com.jasonxu.dexplugin

import android.os.Bundle
import android.view.LayoutInflater

class PluginActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(mContext).inflate(R.layout.activity_main, null)
        setContentView(view)
    }
}