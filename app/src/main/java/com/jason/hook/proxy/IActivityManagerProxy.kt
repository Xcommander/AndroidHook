package com.jason.hook.proxy

import android.content.Intent
import com.jason.hook.Constant
import com.jason.hook.app.HookApplication
import java.lang.reflect.*

//动态代理类，在不同API下，IActivityManager是不一样的。
class IActivityManagerProxy(IActivityTaskManager: Any?) : InvocationHandler {
    private val mActivityManager = IActivityTaskManager

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

    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any? {
        if ("startActivity" == method?.name) {//1.拦截到对应方法调用
            //2.进行替换
            args?.let {
                for (ob in args) {//循环遍历参数
                    if (ob is Intent) {//如果是Intent类型，则先保存目标Activity，再替换占坑Activity
                        ob.putExtra(Constant.TARGET_ACTIVITY, ob.component?.className)
                        ob.setClassName(
                            HookApplication.instance.packageName,
                            Constant.STUB_ACTIVITY
                        )
                        break
                    }
                }

            }
        }
        //通过反射，转发给对应的委托类
        return method?.invoke(mActivityManager, args)
    }

}