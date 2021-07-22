package com.yaxiu.devices.widget.listener

/**
 * @author meicet
 * @date 2021/7/19 11:26
 * @modify
 * @email
 */
interface ApiNetStateCallback {
    fun notifyState(state: Int)
}
enum class ApiNetState(val state: Int) {
    StatePrepare(0),
    StateConnected(1),
    StateHandShake(2),
    StateOnSend(3),
    StateSuccess(4),//连接成功通信成功
    StateFail(5),//JAVA 层连接失败
    StateConnectSuccess(6),//socket连接成功通信成功
    StateClose(100)//连接断开或者被中断

}

