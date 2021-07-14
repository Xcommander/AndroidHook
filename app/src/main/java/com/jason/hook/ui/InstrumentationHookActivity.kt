package com.jason.hook.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.jason.hook.R

class InstrumentationHookActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instrumentation_hookctivity)
    }
}