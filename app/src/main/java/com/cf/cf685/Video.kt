package com.cf.cf685

import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Environment
import android.util.Log
import android.view.Surface
import android.view.TextureView
import com.yaxiu.devices.utils.ImageUtils
import com.yaxiu.devices.widget.listener.ICameraKeyListener
import java.io.File
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.experimental.and

/**
 * @author meicet
 * @date 2021/7/19 13:14
 * @modify
 * @email
 */
class Video {

    var fpsFrameCount: Int = 0
    private var picPath: String = ""
    private lateinit var mIkeyCallback: ICameraKeyListener
    private var simpleDateFormat: SimpleDateFormat
    private var g_isbm999: Int = 0

    private var g_oil: Byte = 0
    private var g_water: Byte = 0
    private var isOpenCode: Boolean = false
    private lateinit var mCodec: MediaCodec
    var devip = "192.168.1.1"
    private var mSurfaceView: TextureView? = null
    private var videoWidth = 1280
    private var videoHeight = 720
    private var bytesRead = 0
    private var nalLen = 0
    private var sockBufUsed = 0
    private var nalBufUsed = 0
    private var bFirst = true
    private var bFindPPS = true
    var isRunState = false
    private var mTrans = 0x0F0F0F0F
    private var mCount = 0

    /**
     * 渲染视频
     */
    private var nalBuf = ByteArray(800000)//800000 1638400 * 128

    //开始预览
    var isPreview = AtomicBoolean(false)

    //点前帧率
    @Volatile
    private var fpsCount = 0

    var findiframe = false


    init {
        System.loadLibrary("ffmpeg")
        System.loadLibrary("iMVR")
        simpleDateFormat = SimpleDateFormat("yyyyMMddHHmmss")
    }

    companion object {

        const val TAG = "ApiNet_iMVR"
    }

    private external fun GetVideoPort(): Int
    private external fun SearchDevice(out: ByteArray): Int
    private external fun Setip(ip: String): Int

    fun init(cameraView: TextureView, ikeyCallback: ICameraKeyListener) {
        mIkeyCallback = ikeyCallback
        mSurfaceView = cameraView
        if (mSurfaceView == null) {
            Log.e(TAG, "mSurfaceView 异常")
            return
        }
        release()
        Setip(devip)
        isRunState = true
        mCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        val createVideoFormat =
            MediaFormat.createVideoFormat(
                MediaFormat.MIMETYPE_VIDEO_AVC,
                videoWidth,
                videoHeight
            )
        mCodec.configure(createVideoFormat, Surface(cameraView.surfaceTexture), null, 0)
        mCodec.start()
        isOpenCode = true
    }

    /**
     * 查找设备
     */
    fun findDevice(): Int {
        var findevice = 0
        val ip = ByteArray(16)
        findevice = SearchDevice(ip)
        if (findevice == 1) {
            try {
                devip = String(ip, Charset.defaultCharset())
                devip = devip.trim { it <= ' ' }
                Log.e(TAG, "devip:$devip")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            Setip(devip)
        }
        return findevice
    }


    fun onFrame(dataArray: ByteArray) {
        Log.e(TAG, "onFrameData  size=${dataArray.size} thread:${Thread.currentThread().name}")
        bytesRead = dataArray.size
        sockBufUsed = 0
        nalBuf.let {
            while (bytesRead - sockBufUsed > 0 && isRunState) {
                nalLen =
                    mergeBuffer(it, nalBufUsed, dataArray, sockBufUsed, bytesRead - sockBufUsed)
                nalBufUsed += nalLen
                sockBufUsed += nalLen
                if (mTrans == 1 && isRunState) {
                    mTrans = -0x1
                    if (bFirst) { // the first start flag
                        bFirst = false
                        Log.e(TAG, "跳出 第一帧")
                    } else { // a complete NAL data, include 0x00000001 trail.
                        if (bFindPPS) { // true 没看懂
                            if (it[4] and 0x1F == 7.toByte()) {//图像参数集
                                bFindPPS = false
                                Log.e(TAG, "跳出 FindPPS")
                            } else {
                                it[0] = 0
                                it[1] = 0
                                it[2] = 0
                                it[3] = 1
                                nalBufUsed = 4
                                fpsCount++
                                break
                            }
                        }

                        if (it[4] == 103.toByte()) {//这里没啥用
                            findiframe = true
                        }
                        if (findiframe && isRunState) {
                           /* if (it[4].toInt() == 101) {
                                fpsFrameCount = 0
                            } else {
                                fpsFrameCount++
                            }*/
                            loadFrame(it, 0, nalBufUsed - 4)
                            fpsCount++
                        }

                    }
                    it[0] = 0
                    it[1] = 0
                    it[2] = 0
                    it[3] = 1
                    nalBufUsed = 4
                }
            }


        }

    }

    private fun loadFrame(buf: ByteArray, offset: Int, length: Int) {
        if (!isRunState || !::mCodec.isInitialized) {
            return
        }
        Log.e(TAG, "loadFrame:${buf.size}")
        val wait = 0
        try {
            val inputBuffers = mCodec.inputBuffers
            //int inputBufferIndex = mCodec.dequeueInputBuffer(wait);
            //dequeueInputBuffer   -1表示一直等待 0表示不等待 大于0表示等待的时间(us)
            val inputBufferIndex = mCodec.dequeueInputBuffer(-1)
            if (inputBufferIndex >= 0) {
                val inputBuffer = inputBuffers[inputBufferIndex]
                inputBuffer.clear()
                inputBuffer.put(buf, offset, length)
                mCodec.queueInputBuffer(
                    inputBufferIndex,
                    0,
                    length,
                    (mCount * 1000000 / 20).toLong(),
                    0
                )
                mCount++
            } else {
                return
            }
            // Get output buffer index
            val bufferInfo = MediaCodec.BufferInfo()
            var outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, wait.toLong())
            while (outputBufferIndex >= 0) {
                mCodec.releaseOutputBuffer(outputBufferIndex, true)
                outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, wait.toLong())
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }


    private fun mergeBuffer(
        nalBuf: ByteArray,
        nalBufUsed: Int,
        SockBuf: ByteArray,
        SockBufUsed: Int,
        SockRemain: Int
    ): Int {
        var i = 0
        var Temp: Byte
        i = 0
        while (i < SockRemain) {
            Temp = SockBuf[i + SockBufUsed]
            nalBuf[i + nalBufUsed] = Temp
            mTrans = mTrans shl 8
            mTrans = mTrans or Temp.toInt()
            if (mTrans == 1) {
                i++
                break
            }
            i++
        }
        return i
    }

    fun initState() {
        isPreview.set(false)
        mTrans = 0x0F0F0F0F
        nalLen = 0
        sockBufUsed = 0
        nalBufUsed = 0
        bFirst = true
        bFindPPS = true
        findiframe = false

    }

    fun release() {
        if (::mCodec.isInitialized && isRunState) {
            mCodec.stop()
            mCodec.release()
            Log.d(
                TAG,
                "release mCodec"
            )
        }
        isPreview.set(false)
        isRunState = false

    }


    /**
     * 按键的回调
     */
    fun onKey(data: ByteArray) {
        if (!::mIkeyCallback.isInitialized) {
            Log.d(
                TAG,
                "Please initialize ICameraKeyListener "
            )
            return
        }

        Log.d(
            TAG,
            "onKey  size=${data.size} data[8]=${data[8]} currentThread:${Thread.currentThread().name}"
        )
        if (!isRunState) {
            return
        }
        if (data.size == 10) {
            when (data[8].toInt()) {
                1 -> {
                    g_oil = data[6]
                    g_water = data[7]
                    Log.i(TAG, "onKey 收到截屏事件--->bytesRead:$bytesRead")
                    filePath(mIkeyCallback)

                }
                0 -> {
                    if (g_isbm999 == 1) {
                        Log.e(TAG, "onKey 收到截屏事件---------------is999---------------")
                        if (g_oil > 1) {
                            Log.i(TAG, "g_oil:" + g_oil + "g_water:" + g_water)
                        }
                        g_oil = 0
                        g_water = 0
                    } else {
                        Log.e(TAG, "onKey 收到截屏事件---------------is558---------------")
                    }
                    filePath(mIkeyCallback)
                }
                5 -> {
                    Log.e(TAG, "onKey There is no JPG picture in the TF Card!")
                }
                4 -> {
                    Log.e(TAG, "onKey The current picture was deleted!")
                }
                6 -> {
                    Log.e(TAG, "onKey All the files are deleted in the TF Card!")
                }
                20 -> {
                    Log.e(TAG, "onKey 左边按键")
                    mIkeyCallback.onLeft()
                }
                21 -> {
                    Log.e(TAG, "onKey 右边按键")
                    mIkeyCallback.onRight()
                }
                22 -> {
                    Log.e(TAG, "onKey 删除按键")
                    mIkeyCallback.deleteCurrentPic()
                }

                else -> {
                    Log.e(TAG, "onKey invalid")
                }
            }
        }


    }

    /**
     * 截屏
     */
    private fun filePath(ikeyCallback: ICameraKeyListener) {
        val time = simpleDateFormat.format(Date(System.currentTimeMillis()))
        val file = if (picPath.isNullOrEmpty()) {
            val fileRoot = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                File(Environment.getExternalStorageDirectory(), "Video_")
            } else {
                File("Video_")
            }
            if (!fileRoot.exists()) {
                fileRoot.mkdirs()
            }
            File(fileRoot, "${time}.jpg")
        } else {
            File(picPath, "${time}.jpg")
        }

        mSurfaceView?.let {
            val bitmap = it.getBitmap(videoWidth, videoHeight)
            val save = ImageUtils.save(bitmap, file, Bitmap.CompressFormat.JPEG, 100, true)
            Log.e(TAG, "onKey 截屏 $save path:${file.absolutePath}")
            if (save) {
                ikeyCallback.postPath(file.absolutePath)
            } else {
                ikeyCallback.postPath(file.absolutePath)
            }
        }


    }

    fun setBM999(is999: Int) {
        g_isbm999 = is999
    }

    /**
     * 拍照
     */
    fun takePhoto() {
        if (::mIkeyCallback.isInitialized) {
            filePath(mIkeyCallback)
        }

    }

    fun initParams(width: Int, height: Int, file: File?) {
        videoWidth = width
        videoHeight = height
        file?.let {
            if (!file.exists()) {
                file.mkdirs()
            }
            if (file.exists()) {
                picPath = file.absolutePath
            }
        }


    }

    fun getFps(): Int {
        return fpsCount
    }

    fun reSetFps() {
        fpsCount = 0

    }

    fun getDevVideoPort(): Int {
        return GetVideoPort()
    }

}