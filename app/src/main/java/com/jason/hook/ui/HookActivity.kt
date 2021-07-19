package com.jason.hook.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import com.jason.hook.R

class HookActivity : AppCompatActivity() {
    companion object {
        val TAG: String = HookActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instrument_hook)
        lifecycle.addObserver(MyObserver())
    }

    private class MyObserver : LifecycleObserver {
        @OnLifecycleEvent(value = Lifecycle.Event.ON_CREATE)
        fun onCreateInActivity() {
            Log.e(TAG, "onCreateInActivity")
        }

        @OnLifecycleEvent(value = Lifecycle.Event.ON_START)
        fun onStartInActivity() {
            Log.e(TAG, "onStartInActivity")
        }

        @OnLifecycleEvent(value = Lifecycle.Event.ON_RESUME)
        fun onResumeInActivity() {
            Log.e(TAG, "onResumeInActivity")
        }

        @OnLifecycleEvent(value = Lifecycle.Event.ON_PAUSE)
        fun onPauseInActivity() {
            Log.e(TAG, "onPauseInActivity")
        }

        @OnLifecycleEvent(value = Lifecycle.Event.ON_STOP)
        fun onStopInActivity() {
            Log.e(TAG, "onStopInActivity")
        }

        @OnLifecycleEvent(value = Lifecycle.Event.ON_DESTROY)
        fun onDestroyInActivity() {
            Log.e(TAG, "onDestroyInActivity")
        }
    }
}