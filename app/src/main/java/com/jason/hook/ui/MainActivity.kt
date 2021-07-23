package com.jason.hook.ui

import android.content.ComponentName
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.jason.hook.R
import com.jason.hook.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dataBinding =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        dataBinding.startInstrument.setOnClickListener {
            startActivity(Intent(this, HookActivity::class.java))
        }
        dataBinding.startDex.setOnClickListener {//跳转到插件Activity
            val intent = Intent()
            intent.component =
                ComponentName("com.jasonxu.dexplugin", "com.jasonxu.dexplugin.PluginActivity")
            startActivity(intent)
        }

    }

}