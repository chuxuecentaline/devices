package com.yaxiu.devices.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.yaxiu.devices.MainActivity
import com.yaxiu.devices.R
import com.yaxiu.devices.databinding.CameraLayoutBinding
import com.yaxiu.devices.widget.listener.ICameraKeyListener
import com.yaxiu.devices.widget.listener.ICameraStateCallback

/**
 * @author meicet
 * @date 2021/7/19 10:33
 * @modify 视频容器布局
 * @email
 */
class CameraContainerLayout : FrameLayout, ICameraStateCallback {

    private var bind: CameraLayoutBinding

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        inflate(context, R.layout.camera_layout, this)

        bind = CameraLayoutBinding.bind(this)

        bind.tvRetry.setOnClickListener {
            retry()
        }

    }

    override fun onSuccess() {
        bind.errorLayout.visibility = GONE
    }

    override fun fpsTip(fps: String) {
        bind.fpsText.text = fps
    }

    override fun onFail(error: String) {
        bind.errorLayout.visibility = VISIBLE
        bind.configLoading.visibility = GONE
        bind.configLayout.visibility = VISIBLE
        bind.configErrorTip.text = error

    }

    override fun retry() {
        bind.errorLayout.visibility = VISIBLE
        bind.configLoading.visibility = VISIBLE
        bind.configLayout.visibility = GONE
        bind.camera.retryConnect()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        bind.camera.removeCameraStateCallback()
    }

    fun addKeyListener(listener: ICameraKeyListener) {
        bind.camera.addCameraCallback(this, listener)
    }

}