package com.jason.hook.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.jason.hook.R
import com.jason.hook.databinding.ActivityMainBinding
import com.jason.hook.helper.HookHelper

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       val dataBinding=DataBindingUtil.setContentView<ActivityMainBinding>(this,R.layout.activity_main)
        dataBinding.startInstrument.setOnClickListener {
            startActivity(Intent(this,InstrumentationHookActivity::class.java))
        }

    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        //hook时机
        HookHelper.attachContext()
    }

}