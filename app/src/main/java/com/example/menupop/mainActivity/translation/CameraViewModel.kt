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
import org.json.JSONObject
import java.net.URLDecoder


class CameraViewModel : ViewModel() {

    val TAG = "CameraViewModel"
    private val _scannerResult = MutableLiveData<ScannerResults>()
    val scannerResult : LiveData<ScannerResults>
        get() = _scannerResult
    val cameraModel = CameraModel()

    private var callbackString : ((String) -> Unit) ?= null
    private var callbackText: ((Text) -> Unit)? = null

    fun successScanning(scannerResults: ScannerResults){
        _scannerResult.value = scannerResults


    }

    private val _textPosition = MutableLiveData<String>()
    private val _image = MutableLiveData<Drawable>()
    val image : LiveData<Drawable>
        get() = _image

    fun getRecognizedText(image: InputImage) {

        callbackText = { visionText ->

            if (visionText != null) {

                _textPosition.value = getPosition(visionText,"position")

                val resultText = getPosition(visionText,"request")
                Log.d(TAG, "resultText: $resultText")

                checkLanguage(resultText)

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
            }
        }

        cameraModel.checkLanguage(text, callbackString!!)
    }

    private val _isDecode = MutableLiveData<Boolean>()
    val isDecode : LiveData<Boolean>
        get() = _isDecode

    private val _translatedText = MutableLiveData<String>()

    fun requestTranslation(text : String, langCode : String){
        callbackString = {
            if(it != null){
                val decode = decodeKorean(it)
                Log.d(TAG, "requestTranslation: $decode")
                if(decode != null){
                    _translatedText.value = decode
                    _isDecode.value = true
                }
            }
        }
        cameraModel.requestTranslation(text, langCode, callbackString!!)
    }

    fun getPosition(text: Text, type: String): String {
        var json = JsonObject()

        for (block in text.textBlocks) {

            for (line in block.lines) {
                val lineText = line.text.lowercase()
                val lineFrame = line.boundingBox

//                Log.d(TAG, "lineText: $lineText")
//                Log.d(TAG, "line 좌표: $lineFrame")

                if(type == "request"){
                    json.add(lineText, JsonPrimitive(lineText))
                }else{
                    json.add(lineText, JsonPrimitive(lineFrame.toString()))
                }

//                for (element in line.elements) {
//                    val elementText = element.text.lowercase()
//                    val elementFrame = element.boundingBox
//
//                    Log.d(TAG, "elementText: $elementText")
//                    Log.d(TAG, "element 좌표: $elementFrame")
//
//
//                if(type == "request"){
//                    json.add(elementText, JsonPrimitive(elementText))
//                }else{
//                    json.add(elementText, JsonPrimitive(elementFrame.toString()))
//                }
//             }


            }
        }
        return json.toString()
    }

    fun decodeKorean(jsonString: String): String {
        try {
            val jsonObject = JSONObject(jsonString)
            val decodedJson = JSONObject()

            // 받아온 JSON 데이터의 키와 값을 반복하여 디코딩
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = jsonObject.getString(key)
                val decodedValue = URLDecoder.decode(value, "UTF-8") // 값도 디코딩
                decodedJson.put(key, decodedValue)
            }

            return decodedJson.toString() // 디코딩된 JSON 형식의 문자열 반환

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "" // 예외 발생 시 빈 문자열 반환 혹은 에러 처리
    }

    fun drawTranslatedText(){

        val filePath = scannerResult.value!!.croppedImageFile!!.path
        Log.d(TAG, "filePath: $filePath")
        val bitmap = BitmapFactory.decodeFile(filePath)
        Log.d(TAG, "bitmap: $bitmap")

        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        val translatedText = JSONObject(_translatedText.value)
        val textPosition = JSONObject(_textPosition.value)


        val keys = translatedText.keys()
        while (keys.hasNext()) {

            val key = keys.next()
            val text = translatedText.getString(key)
            Log.d(TAG, "drawTranslatedText: $text")
            val position = textPosition.getString(key)
            val rect = stringToRect(position)


            val paintText = Paint().apply {
                color = Color.BLACK // 텍스트 색상
                textSize = 50f
                typeface = Typeface.DEFAULT_BOLD
            }

            val paintRect = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }

            val left = rect!!.left.toFloat()
            val top = rect!!.top.toFloat()
            val right = rect!!.right.toFloat()
            val bottom = rect!!.bottom.toFloat()

            canvas.drawRect(left, top, right, bottom, paintRect)
            canvas.drawText(text, left, bottom, paintText)
        }

        _image.value = BitmapDrawable(mutableBitmap)

    }

    fun stringToRect(input: String): Rect? {
        val rectPattern = Regex("Rect\\((-?\\d+), (-?\\d+) - (-?\\d+), (-?\\d+)\\)")
        val matchResult = rectPattern.find(input)

        return matchResult?.destructured?.let { (left, top, right, bottom) ->
            Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        }
    }




}