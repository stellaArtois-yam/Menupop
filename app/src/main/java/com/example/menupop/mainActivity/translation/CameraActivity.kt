package com.example.menupop.mainActivity.translation

import android.app.Activity
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.menupop.R
import com.example.menupop.databinding.ActivityCameraBinding
import com.google.mlkit.vision.common.InputImage
import com.zynksoftware.documentscanner.ScanActivity
import com.zynksoftware.documentscanner.model.DocumentScannerErrorModel
import com.zynksoftware.documentscanner.model.ScannerResults
import com.zynksoftware.documentscanner.ui.DocumentScanner
import java.io.File
import java.io.IOException
import kotlin.math.log

class CameraActivity : ScanActivity() {
    lateinit var binding : ActivityCameraBinding
    lateinit var cameraViewModel: CameraViewModel
    lateinit var image : InputImage
    val TAG = "CameraActivity"
    fun init(){
        val configuration = DocumentScanner.Configuration()
        configuration.imageQuality = 100
        configuration.imageSize = 1000000 // 1 MB
        configuration.imageType = Bitmap.CompressFormat.JPEG
        DocumentScanner.init(this, configuration) // or simply DocumentScanner.init(this)

        cameraViewModel.scannerResult.observe(this){ result ->
            Log.d(TAG, "scannerResult: 호출됨")

            val uri = Uri.fromFile(result.croppedImageFile)
            Log.d(TAG, "uri: $uri")
            try {
                image = InputImage.fromFilePath(applicationContext, uri)
                cameraViewModel.getRecognizedText(image)
                showDialog()
            } catch (e: IOException) {
                e.printStackTrace()
            }


//            result.croppedImageFile?.let {  binding.cropImage.setImageFile(it) }
        }

        cameraViewModel.isDecode.observe(this, Observer {
            if(it){
                cameraViewModel.drawTranslatedText()
            }
        })


    }
    override fun onClose() {
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera)
        addFragmentContentLayout()
        cameraViewModel = CameraViewModel()

        init()
    }

    override fun onError(error: DocumentScannerErrorModel) {
        Log.d(TAG, "onError: ")
    }

    override fun onSuccess(scannerResults: ScannerResults) {
        cameraViewModel.successScanning(scannerResults)
        Log.d(TAG, "onSuccess: ${scannerResults}")


    }

    val File.size get() = if (!exists()) 0.0 else length().toDouble()
    val File.sizeInKb get() = size / 1024
    val File.sizeInMb get() = sizeInKb / 1024
    private fun ImageView.setImageFile(image: File) {
        Glide.with(this).load(image)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(this)
    }


    fun showDialog(){
        var dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.progressbar)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.show()

        cameraViewModel.image.observe(this, Observer {
            if(it != null){
                dialog.dismiss()
                binding.cropImage.setImageDrawable(it)
            }
        })

    }
}