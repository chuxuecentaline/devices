package com.meicet.network_hp

import android.util.Log
import androidx.annotation.WorkerThread
import com.yaxiu.devices.widget.listener.ApiNetState
import com.yaxiu.devices.widget.listener.ApiNetStateCallback
import com.yaxiu.devices.widget.listener.FrameDataCallback

/**
 * @author meicet
 * @date 2021/7/19 10:31
 * @modify 设备帮助类
 * @email
 */
class ApiNet {
    init {
        System.loadLibrary("hpsocket")
        System.loadLibrary("hpsocket4c")
        System.loadLibrary("networkY")
        System.loadLibrary("nativeD-lib")
    }

    companion object {
        // Used to load the 'native-lib' library on application startup.
        const val TAG = "ApiNet"
    }

    var currentState = ApiNetState.StateIdol.state

    //NET对像全局指针地址  JNI赋值 client 客户端
    private var apiNetID: Long = 0L

    //NET对像全局指针地址  JNI赋值 server 服务端
    private var apiNetServerID: Long = 0L

    var onCallbackFrameData: (byteArray: ByteArray) -> Unit = {}

    var onKeyCallbackFrameData: (byteArray: ByteArray) -> Unit = {}

    //连接中被打断回调close
    var onCallbackNotifyState: (state: Int) -> Unit = {}

    //连接中被打断回调close
    var onCallbackCloseError: (actionState: Int, error: Int) -> Unit = {_,_->}

    /**
     * client 视频通讯
     */
    fun onCreate() {
        onCreateNet()
        nativeSetDataCallBack(apiNetID, object : FrameDataCallback {
            override fun onFrame(byteArray: ByteArray) {
                onCallbackFrameData.invoke(byteArray)
            }
        })
        nativeSetStateCallBack(apiNetID, object : ApiNetStateCallback {
            override fun notifyState(state: Int, actionState: Int, error: Int) {
                Log.e(
                    TAG, "JAVA层回调 notifyState state=$state actionState=$actionState  error=$error"
                )
                currentState = state
                if (state == ApiNetState.StateClose.state) {
                    onCallbackCloseError.invoke(actionState, error)
                } else {
                    onCallbackNotifyState.invoke(state)
                }
            }
        })
    }

    /**
     * 回收视频连接
     */
    fun onDestroy(clearCallback: Boolean = true) {
        stopKey()
        Log.e(TAG, "销毁前 id=$apiNetID")
        nativeDestroy(apiNetID)
        Log.e(TAG, "销毁后 id=$apiNetID")
        if (clearCallback) {
            onCallbackNotifyState = {}
            onCallbackFrameData = {}
            onCallbackCloseError = {_,_->}
        }
    }

    private fun onCreateNet() {
        if (apiNetID != 0L) {
            Log.e(TAG, "回收之前的指针  id=$apiNetID")
            nativeDestroy(apiNetID)
        }
        val createID = nativeCreate()
        Log.e(TAG, "apiNetID=$apiNetID createID=$createID")
    }

    //这里会阻塞线程 syncWaitTime 秒
    @WorkerThread
    fun onConnect(ipAddress: String, port: Int, syncWaitTime: Int = 15): Boolean {

        val state = onConnectIP(ipAddress, port)
        Log.e(TAG, "state:$state")
        return if (state && nativeIsConnected(apiNetID, syncWaitTime * 10, 100)) {

            onCallbackNotifyState.invoke(ApiNetState.StateSuccess.state)
            true
        } else {
            Log.e(TAG, "$syncWaitTime 秒内收无法连接成功 中断连接")
            onStop()
            if(currentState!=ApiNetState.StateClose.state){//close没有回调时就主动回调失败状态
                onCallbackNotifyState.invoke(ApiNetState.StateFail.state)
            }
            false
        }
    }

    //这里会阻塞线程 syncWaitTime 秒
    @WorkerThread
    fun isSendOK(syncWaitTimeData: Int = 10) {
        onStartCmd()
        //通信成功后 5秒内收无法收到画面数据就视为无效连接
        if (nativeIsReceiveData(apiNetID, syncWaitTimeData * 100, 10)) {
            onCallbackNotifyState.invoke(ApiNetState.StateConnectSuccess.state)
        } else {
            Log.e(TAG, "$syncWaitTimeData 秒内收无法收到画面数据 中断连接")
            onStop()
            if(currentState!=ApiNetState.StateClose.state){//close没有回调时就主动回调失败状态
                onCallbackNotifyState.invoke(ApiNetState.StateFail.state)
            }
        }
    }

    //已经建立了连接
    fun isConnected() = nativeIsConnected(apiNetID, 0, 0)

    //已经接收到数据了
    fun isReceived() = nativeIsReceiveData(apiNetID, 0, 0)

    fun onStop(): Boolean {
        return nativeStop(apiNetID)
    }

    //每次只能连接一个IP  内部会清空上次的连接记录
    private fun onConnectIP(ipAddress: String, port: Int): Boolean {
        return nativeConnect(apiNetID, ipAddress, port)
    }

    private fun onStartCmd(isNewDevice: Boolean = true): Boolean {
        return nativeStartCmd(apiNetID, isNewDevice)
    }

    /***************************视频client native层*****************************/
    private external fun nativeCreate(): Long
    private external fun nativeDestroy(apiNetID: Long): Boolean

    private external fun nativeConnect(
        apiNetID: Long,
        ip: String,
        port: Int
    ): Boolean

    private external fun nativeStop(apiNetID: Long): Boolean

    private external fun nativeStartCmd(apiNetID: Long, isNewDevice: Boolean): Boolean

    //同步连接状态需要在子线程中阻塞运行   0=不同步 及时返回状态  10=等待10秒
    private external fun nativeIsConnected(
        apiNetID: Long,
        syncWaitTime: Int,
        milliseconds: Int
    ): Boolean

    private external fun nativeIsReceiveData(
        apiNetID: Long,
        syncWaitTime: Int,
        milliseconds: Int
    ): Boolean

    private external fun nativeSetDataCallBack(apiNetID: Long, callback: FrameDataCallback): Boolean
    private external fun nativeSetStateCallBack(
        apiNetID: Long,
        callback: ApiNetStateCallback
    ): Boolean

    /***************************按键服务native层*****************************/
    fun startKey() {
        if (apiNetServerID != 0L) {
            Log.e(TAG, "回收之前的指针  id=$apiNetServerID")
            nativeServerDestroy(apiNetServerID)
        }
        apiNetServerID = nativeCreateServerNet()
        nativeKeyDataCallBack(apiNetServerID, object : FrameDataCallback {
            override fun onFrame(byteArray: ByteArray) {
                onKeyCallbackFrameData.invoke(byteArray)
            }
        })

        Log.e(TAG, "apiNetServerID=$apiNetServerID createID=$apiNetServerID")
    }


    private fun stopKey() {

        if (apiNetServerID != 0L) {
            nativeServerDestroy(apiNetServerID)
            apiNetServerID = 0L
        }
        Log.e(TAG, "stopKey 回收之前的指针  id=$apiNetServerID")
    }

    private external fun nativeKeyDataCallBack(
        apiNetServerID: Long,
        frameDataCallback: FrameDataCallback
    )

    private external fun nativeCreateServerNet(): Long

    private external fun nativeServerDestroy(apiNetID: Long)


}