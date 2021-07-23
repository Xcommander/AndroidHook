package com.jason.hook.proxy

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.jason.hook.Constant
import com.jason.hook.app.HookApplication
import java.lang.reflect.*

//动态代理类，在不同API下，IActivityManager是不一样的。
class IActivityManagerProxy(activityManager: Any?) : InvocationHandler {
    private val mActivityManager = activityManager

    companion object {
        // END Android-changed: How proxies are generated.
        @Throws(IllegalArgumentException::class)
        fun newProxyInstance(
            loader: ClassLoader?,
            interfaces: Array<Class<*>?>,
            h: InvocationHandler
        ): Any? {
            return Proxy.newProxyInstance(loader, interfaces, h)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("QueryPermissionsNeeded")
    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any? {
        if ("startActivity" == method?.name) {//1.拦截到对应方法调用
            //2.进行替换
            args?.let {
                for (ob in args) {//循环遍历参数
                    if (ob is Intent) {//如果是Intent类型，则先保存目标Activity，再替换占坑Activity
                        //1. 查询是否注册
                        val resolveInfoList =
                            HookApplication.instance.packageManager.queryIntentActivities(
                                ob,
                                PackageManager.MATCH_ALL
                            )
                        if (resolveInfoList.size == 0) {//2. 未查询到，则需要占位Activity来帮忙
                            val intent = ob.cloneFilter()
                            ob.putExtra(Constant.DEX_PLUGIN, intent)
                            ob.setClassName(
                                HookApplication.instance.packageName,
                                Constant.STUB_ACTIVITY
                            )
                        }
                        break
                    }
                }

            }
        }
        //通过反射，转发给对应的委托类。
        // 这里有个大坑，也就是在kotlin array是是数组，而invoke中的args是可变参数类型。所以会报类型错误。
        //Kotlin的解决方法是在array加上*，为了防止空指针名，可以用empty()来表示。所以最终表达为*(args?:empty())
        return method?.invoke(mActivityManager, *(args ?: emptyArray()))
    }

}