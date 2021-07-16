package com.jason.hook.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AndroidRuntimeException
import com.jason.hook.R

//占位Activity，为了帮助其他没有注册的Activity，绕过AMS审查
class StubActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instrumentation_hookctivity)
    }
}