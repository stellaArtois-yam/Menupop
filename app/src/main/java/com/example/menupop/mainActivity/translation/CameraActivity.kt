package com.example.menupop.mainActivity.translation

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.menupop.R
import com.example.menupop.databinding.ActivityCameraBinding
import com.example.menupop.mainActivity.MainActivity
import com.example.menupop.mainActivity.foodPreference.FoodPreference
import com.google.mlkit.vision.common.InputImage
import com.zynksoftware.documentscanner.ScanActivity
import com.zynksoftware.documentscanner.model.DocumentScannerErrorModel
import com.zynksoftware.documentscanner.model.ScannerResults
import com.zynksoftware.documentscanner.ui.DocumentScanner
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class CameraActivity : ScanActivity() {

    lateinit var binding : ActivityCameraBinding
    private lateinit var cameraViewModel: CameraViewModel
    lateinit var image : InputImage
    private lateinit var foodPreferenceData : ArrayList<FoodPreference>
    private var isImageSuccess = false
    private lateinit var country : String

    var identifier = 0
    val TAG = "CameraActivityTAG"

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            moveToMain()
        }
    }

    fun moveToMain(){
        val intent = Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("checkedTranslation", isImageSuccess)
        intent.putExtra("identifier", identifier)
        startActivity(intent)
        finish()
    }


    fun init(){
        val intent = intent
        foodPreferenceData = if (intent.getSerializableExtra("foodPreference")!=null){
            intent.getSerializableExtra("foodPreference") as ArrayList<FoodPreference>
        }else{
            ArrayList()
        }
        country = intent.getStringExtra("country").toString()

        cameraViewModel.setFoodPreference(foodPreferenceData)

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
                showDialog()
                cameraViewModel.getRecognizedText(image,country)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        this.onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onClose() {
        finish()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera)
        addFragmentContentLayout()
        cameraViewModel = CameraViewModel(application)

        val sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE)
        identifier = sharedPreferences.getInt("identifier", 0)
        
        init()
    }

    override fun onError(error: DocumentScannerErrorModel) {
        Log.d(TAG, "onError: ${error.errorMessage}")
    }

    override fun onSuccess(scannerResults: ScannerResults) {
        cameraViewModel.successScanning(scannerResults)
        Log.d(TAG, "onSuccess: $scannerResults")
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


    private fun showDialog(){
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.progressbar)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.show()

        cameraViewModel.image.observe(this) {
            if(it != null){
                dialog.dismiss()
                binding.cropImage.setImageDrawable(it)
                lifecycleScope.launch {
                    cameraViewModel.useTranslationTicket(identifier)
                }
            }
        }

        cameraViewModel.failed.observe(this) {
            if(it){
                Log.d(TAG, "showDialog failed")
                dialog.dismiss()
                Toast.makeText(this, resources.getString(R.string.network_error), Toast.LENGTH_LONG).show()
                moveToMain()
            }
        }
    }
}