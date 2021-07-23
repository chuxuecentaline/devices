package com.yaxiu.devices

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yaxiu.devices.databinding.ActivityMainBinding
import com.yaxiu.devices.utils.ImageUtils
import com.yaxiu.devices.widget.listener.ICameraKeyListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import java.io.File

class MainActivity : AppCompatActivity(), ICameraKeyListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //binding.sampleText.addKeyListener(this)
        binding.image.setOnClickListener {
        //    binding.sampleText.takePhoto()

        }


    }

    override fun connectState(state: Boolean) {
        Toast.makeText(this, "wifi 连接 $state", Toast.LENGTH_SHORT).show()
    }

    override fun onLeft() {
        Toast.makeText(this, "左键", Toast.LENGTH_SHORT).show()
    }

    override fun onRight() {
        Toast.makeText(this, "右键", Toast.LENGTH_SHORT).show()
    }

    override fun deleteCurrentPic() {
        Toast.makeText(this, "删除键", Toast.LENGTH_SHORT).show()
        binding.image.setImageBitmap(null)
    }

    override fun postPath(path: String) {
        val decodeFile = BitmapFactory.decodeFile(path)
        val file2Uri = ImageUtils.file2Uri(this, File(path))
        println("path = [${path}]  = [${decodeFile}] file2Uri=[$file2Uri]")
        binding.image.setImageBitmap(decodeFile)
    }

    override fun showWifi() {
        Toast.makeText(this, "wifi", Toast.LENGTH_SHORT).show()
    }


}