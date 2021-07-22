package com.yaxiu.devices.widget

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.util.Log
import android.view.TextureView
import com.cf.cf685.Video
import com.meicet.network_hp.ApiNet
import com.yaxiu.devices.widget.listener.ApiNetState
import com.yaxiu.devices.widget.listener.ICameraKeyListener
import com.yaxiu.devices.widget.listener.ICameraStateCallback
import kotlinx.coroutines.*

/**
 * @author meicet
 * @date 2021/7/19 10:33
 * @modify 画面显示区
 * @email
 */
class CameraView : TextureView, TextureView.SurfaceTextureListener, ICameraKeyListener {

    private var iICameraKeyListener: ICameraKeyListener? = null
    private var iICameraStateCallback: ICameraStateCallback? = null
    private val deviceHelper = ApiNet()
    private val video = Video()
    private val coroutine by lazy {
        CoroutineScope(Dispatchers.IO)

    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onFinishInflate() {
        super.onFinishInflate()

        deviceHelper.onCreate()

        surfaceTextureListener = this
        //2初始化 预览区 监听按键 拍照回调
        deviceHelper.onCallbackFrameData = { //视频流

            video.onFrame(it)
        }
        deviceHelper.onKeyCallbackFrameData = {//按键流
            if (iICameraKeyListener == null) {
                throw IllegalArgumentException("Please initialize iICameraKeyListener")
            }
            video.onKey(it, this)
        }
        /**
         *       StatePrepare(0),
         *       StateConnected(1),
         *       StateHandShake(2),
         *       StateOnSend(3),
         *       StateSuccess(4),//连接成功通信成功
         *       StateFail(5),//JAVA 层连接失败
         *       StateConnectSuccess(6),//socket连接成功通信成功
         *       StateClose(100)//连接断开或者被中断
         */
        deviceHelper.onCallbackNotifyState = { code ->
            if (iICameraStateCallback == null) {
                throw IllegalArgumentException("Please initialize iICameraStateCallback")
            }
            CoroutineScope(Dispatchers.Main).launch {
                Log.i("CameraView", "state=$code")
                when (code) {
                    ApiNetState.StatePrepare.state -> {

                    }
                    ApiNetState.StateConnected.state -> {

                    }
                    ApiNetState.StateHandShake.state -> {

                    }
                    ApiNetState.StateOnSend.state -> {

                        iICameraStateCallback?.onSuccess()
                    }
                    ApiNetState.StateSuccess.state -> {
                        deviceHelper.startKey()
                        iICameraStateCallback?.onSuccess()
                    }
                    ApiNetState.StateFail.state -> {
                        iICameraStateCallback?.onFail("IP 连接失败")
                    }
                    ApiNetState.StateConnectSuccess.state -> {

                    }
                    ApiNetState.StateClose.state -> {
                        iICameraStateCallback?.onFail("连接中断，请点击重试")
                    }
                }

            }


        }

    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        //1.连接设备的网络 返回结果 成功 失败
        println("onSurfaceTextureAvailable = [${Thread.currentThread().name}], width = [${width}], height = [${height}]")
        video.init(this@CameraView)
        coroutine.launch {
            println("onSurfaceTextureAvailable = [${Thread.currentThread().name}]")
            startConnect()
        }

    }

    /**
     * 开始连接设备，必须再子线程中执行
     */
    private suspend fun startConnect() {
        if (deviceHelper.onConnect("192.168.1.1", 40121)) {
            video.initState()
            deviceHelper.isSendOK()
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        iICameraStateCallback?.retry()
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        //3.release 释放资源
        video.release()
        deviceHelper.stopKey()
        deviceHelper.onDestroy()
        coroutine.cancel()
        return true

    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

    }

    fun addCameraCallback(
        cameraContainerLayout: ICameraStateCallback,
        keyListener: ICameraKeyListener?
    ) {
        iICameraStateCallback = cameraContainerLayout
        iICameraKeyListener = keyListener
    }

    fun removeCameraStateCallback() {
        iICameraStateCallback = null
        iICameraKeyListener = null
    }

    fun retryConnect() {
        video.isPreview = false
        coroutine.launch {
            deviceHelper.onStop()
            delay(1000)
            startConnect()
        }

    }

    override fun onLeft() {
        CoroutineScope(Dispatchers.Main).launch {
            iICameraKeyListener?.onLeft()
        }
    }

    override fun onRight() {
        CoroutineScope(Dispatchers.Main).launch {
            iICameraKeyListener?.onRight()
        }
    }

    override fun deleteCurrentPic() {
        CoroutineScope(Dispatchers.Main).launch {
            iICameraKeyListener?.deleteCurrentPic()
        }
    }

    override fun postPath(path: String) {
        CoroutineScope(Dispatchers.Main).launch {
            iICameraKeyListener?.postPath(path)
        }
    }
}