package com.yaxiu.devices.widget.listener

/**
 * @author meicet
 * @date 2021/7/19 11:26
 * @modify
 * @email
 */
interface ApiNetStateCallback {
    //    error socket连接错误码
    fun notifyState(state: Int, actionState: Int, error: Int)
}
enum class ApiNetState(val state: Int) {
    StateIdol(-1),
    StatePrepare(0),
    StateConnected(1),
    StateHandShake(2),
    StateOnSend(3),
    StateSuccess(4),//连接成功通信成功
    StateFail(5),//JAVA 层连接失败
    StateConnectSuccess(6),//socket连接成功通信成功
    StateDeviceNoSupport(7),//设备端口号错误
    StateClose(100)//连接断开或者被中断
}

