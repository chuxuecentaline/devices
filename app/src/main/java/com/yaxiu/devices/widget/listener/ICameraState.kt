package com.yaxiu.devices.widget.listener

/**
 * @author meicet
 * @date 2021/7/19 10:56
 * @modify 回调相关
 * @email
 */
/******************UI相关**********************/
interface ICameraStateCallback {
    fun onSuccess()
    fun onFail(errorCord: Int)
    fun retry()
    fun fpsTip(fps: String)
}

/******************硬件的按键事件**********************/
interface ICameraKeyListener {

    /**
     * 连接状态
     */
    fun connectState(state: Boolean)

    /**
     * 左键
     */
    fun onLeft()

    /**
     * 右键
     */
    fun onRight()

    /**
     * 删除键
     */
    fun deleteCurrentPic()

    /**
     * 图片回调
     */
    fun postPath(path: String)

    /**
     * 弹出wifi
     */
    fun showWifi()
}