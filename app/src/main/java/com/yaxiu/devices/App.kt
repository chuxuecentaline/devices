package com.yaxiu.devices

import android.app.Application
import android.content.Context
import xcrash.XCrash

/**
 * @author meicet
 * @date 2021/7/20 14:01
 * @modify
 * @email
 */
class App : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        //  val parameters= XCrash.InitParameters().setAppVersion(BuildConfig.VERSION_NAME).setLogDir(FileCreate.getCrashDir())
        XCrash.init(this)
    }
}