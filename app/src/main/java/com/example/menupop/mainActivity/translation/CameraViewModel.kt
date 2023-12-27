package com.example.menupop.mainActivity.translation

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.zynksoftware.documentscanner.model.ScannerResults
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLDecoder
import java.util.Collections.list


class CameraViewModel : ViewModel() {

    val TAG = "CameraViewModel"
    private val _scannerResult = MutableLiveData<ScannerResults>()
    val scannerResult: LiveData<ScannerResults>
        get() = _scannerResult
    val cameraModel = CameraModel()

    private var callbackString: ((String) -> Unit)? = null
    private var callbackText: ((Text) -> Unit)? = null

    fun successScanning(scannerResults: ScannerResults) {
        _scannerResult.value = scannerResults

    }

    private val _failed = MutableLiveData<Boolean>()
    val failed : LiveData<Boolean>
        get() = _failed

    private val _textPosition = MutableLiveData<ArrayList<Rect>>()
    private val _image = MutableLiveData<Drawable>()
    val image: LiveData<Drawable>
        get() = _image

    fun checkTranslationTicket(sharedPreferences: SharedPreferences){

    }


    fun getRecognizedText(image: InputImage) {

        callbackText = { visionText ->

            if (visionText != null) {

                Log.d(TAG, "getRecognizedText: ${visionText.text}")

                val resultText = getText(visionText)
                Log.d(TAG, "resultText: $resultText")
                Log.d(TAG, "textPosition: ${_textPosition.value}")

                checkLanguage(resultText)

            }else{
                _failed.value = true
            }
        }

        cameraModel.recognizedText(image, callbackText!!)
    }

    fun checkLanguage(text: String) {
        callbackString = { langCode ->
            if (langCode != "und") {
                requestTranslation(text, langCode)
            } else {
                Log.d(TAG, "checkLanguage else: $langCode")
                _failed.value = true
            }
        }

        cameraModel.checkLanguage(text, callbackString!!)
    }



    fun requestTranslation(text: String, langCode: String) {
        callbackString = {
            if (it != null) {
                val jsonArray = JSONArray(it)
                var decode = jsonArray.getString(0).replace("[", "")
                decode = decode.replace("]", "")
                val decodeList = decode.split(", ")


                Log.d(TAG, "requestTranslation: $decodeList")
                if (decode != null) {

                    drawTranslatedText(decodeList)
                }
            }else{
                _failed.value = true
            }
        }
        cameraModel.requestTranslation(text, langCode, callbackString!!)
    }

    fun getText(text: Text): String {
        var positionList = ArrayList<Rect>()
        var list = ArrayList<String>()

        for (block in text.textBlocks) {

            for (line in block.lines) {
                val lineText = line.text.lowercase()
                val lineFrame = line.boundingBox
                list.add(lineText)
                positionList.add(lineFrame!!)
            }

            _textPosition.value = positionList

        }

        Log.d(TAG, "getText text size: ${list.size}")
        Log.d(TAG, "getText position size: ${positionList.size}")
        return list.toString()
    }



    fun drawTranslatedText(textList: List<String>) {

        val filePath = scannerResult.value!!.croppedImageFile!!.path
        Log.d(TAG, "filePath: $filePath")
        val bitmap = BitmapFactory.decodeFile(filePath)
        Log.d(TAG, "bitmap: $bitmap")

        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        val paintText = Paint().apply {
            color = Color.BLACK // 텍스트 색상
            textSize = 50f
            typeface = Typeface.DEFAULT_BOLD
        }


        val paintRect = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        Log.d(TAG, "textList size: ${textList.size}")
        Log.d(TAG, "textPosition size: ${_textPosition.value!!.size}")


        for(i:Int in textList.indices){
            val left = _textPosition.value!![i].left.toFloat()
            val top = _textPosition.value!![i].top.toFloat()
            val right = _textPosition.value!![i].right.toFloat()
            val bottom = _textPosition.value!![i].bottom.toFloat()

            canvas.drawRect(left, top, right, bottom, paintRect)
            canvas.drawText(textList[i], left, bottom, paintText)
        }

        _image.value = BitmapDrawable(mutableBitmap)

    }



}

