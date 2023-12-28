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
import com.airbnb.lottie.L
import com.example.menupop.R
import com.example.menupop.mainActivity.foodPreference.FoodPreference
import com.example.menupop.mainActivity.foodPreference.FoodPreferenceDataClass
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.zynksoftware.documentscanner.model.ScannerResults
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLDecoder
import java.util.Collections.list
import kotlin.math.log


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
    val failed: LiveData<Boolean>
        get() = _failed

    private val _textPosition = MutableLiveData<ArrayList<Rect>>()
    private val _image = MutableLiveData<Drawable>()

    lateinit var likesFoodList: ArrayList<String>
    lateinit var unLikesFoodList: ArrayList<String>
    val image: LiveData<Drawable>
        get() = _image

    fun checkTranslationTicket(sharedPreferences: SharedPreferences) {

    }

    fun setFoodPreference(foodPreference: ArrayList<FoodPreference>) {
        if (foodPreference != null) {
            val (likesFoodList, unLikesFoodList) = splitFoodPreferenceList(foodPreference)
            this.likesFoodList = likesFoodList
            this.unLikesFoodList = unLikesFoodList
//            Log.d(TAG, "setFoodPreference: ${likesFoodList}, ${unLikesFoodList}")
        }
    }


    fun getRecognizedText(image: InputImage) {

        callbackText = { visionText ->

            if (visionText != null) {

//                Log.d(TAG, "getRecognizedText: ${visionText.text}")

                val resultText = getText(visionText)
//                Log.d(TAG, "resultText: $resultText")
//                Log.d(TAG, "textPosition: ${_textPosition.value}")

                checkLanguage(resultText)

            } else {
                _failed.value = true
            }
        }

        cameraModel.recognizedText(image, callbackText!!)
    }

    fun checkLanguage(text: String) {
        callbackString = { langCode ->
            if (langCode != "und" && langCode != "failed") {
                requestTranslation(text, langCode)
            } else {
                Log.d(TAG, "checkLanguage else: $langCode")
                _failed.value = true
            }
        }

        cameraModel.checkLanguage(text, callbackString!!)
    }

    fun splitFoodPreferenceList(foodPreferenceList: List<FoodPreference>): Pair<ArrayList<String>, ArrayList<String>> {
        val likes = ArrayList<String>()
        val dislikes = ArrayList<String>()

        for (foodPreference in foodPreferenceList) {
            if (foodPreference.classification == "호") {
                Log.d(TAG, "호: ${foodPreference.foodName}")
                likes.add(foodPreference.foodName)
            } else if (foodPreference.classification == "불호") {
                Log.d(TAG, "불호: ${foodPreference.foodName}")
                dislikes.add(foodPreference.foodName)
            }
        }

        return Pair(likes, dislikes)
    }


    fun requestTranslation(text: String, langCode: String) {
        callbackString = {
            if (it != "failed") {
                val jsonArray = JSONArray(it)
//                Log.d(TAG, "requestTranslation response: $jsonArray")
                var decode = jsonArray.getString(0).replace("[", "")
                decode = decode.replace("]", "")
                val decodeList = decode.split(", ")


                Log.d(TAG, "requestTranslation: $decodeList")
                if (decode != null) {

                    drawTranslatedText(decodeList)
                }
            } else {
                _failed.value = true
            }
        }
        cameraModel.requestTranslation(text, langCode, callbackString!!)
    }

    fun getText(text: Text): String {
        var positionList = ArrayList<Rect>()
        var textList = ArrayList<String>()

        for (block in text.textBlocks) {

            for (line in block.lines) {
                val lineText = line.text.lowercase()
                val lineFrame = line.boundingBox

                textList.add(lineText)
                positionList.add(lineFrame!!)
            }

            _textPosition.value = positionList

        }

        if(textList.size != positionList.size){
            _failed.value = true
        }



        Log.d(TAG, "getText text size: ${textList.size}")
        Log.d(TAG, "getText position size: ${positionList.size}")
        return textList.toString()
    }


    fun drawTranslatedText(textList: List<String>) {

        val filePath = scannerResult.value!!.croppedImageFile!!.path
        val bitmap = BitmapFactory.decodeFile(filePath)

        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        var paintText = Paint().apply {
            color = Color.BLACK // 텍스트 색상
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
        }


        var paintRect = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

//        if(textList.size != _textPosition.value!!.size){
//            Log.d(TAG, "drawTranslatedText list size unMatch:")
//            _failed.value = true
//        }


        for (i: Int in textList.indices) {
            val left = _textPosition.value!![i].left.toFloat()
            val top = _textPosition.value!![i].top.toFloat()
            val right = _textPosition.value!![i].right.toFloat()
            val bottom = _textPosition.value!![i].bottom.toFloat()

//            val getTextSize = getTextSize(_textPosition.value!![i], textList[i])


            for (text in likesFoodList) {
                if (textList[i].contains(text)) {
                    Log.d(TAG, "like: ${text}")
                    paintText = Paint().apply {
                        color = Color.rgb(255, 127, 9) // 텍스트 색상
                        textSize = 30f
                        typeface = Typeface.DEFAULT_BOLD
                    }
                }else {
                    paintText = Paint().apply {
                        color = Color.BLACK // 텍스트 색상
                        textSize = 30f
                        typeface = Typeface.DEFAULT_BOLD
                    }
                }
            }

            for (text in unLikesFoodList) {
                if (textList[i].contains(text)) {
                    Log.d(TAG, "dislike: ${text}")
                    paintText = Paint().apply {
                        color = Color.rgb(255, 173, 13) // 텍스트 색상
                        textSize = 30f
                        typeface = Typeface.DEFAULT_BOLD
                    }
                }
            }


            canvas.drawRect(left, top, right, bottom, paintRect)
            canvas.drawText(textList[i], left, bottom, paintText)
        }

        _image.value = BitmapDrawable(mutableBitmap)

    }



    fun getTextSize(rect: Rect, text: String) : Paint {

        var textSize = 20f // 텍스트 크기 초기값 설정 (임의의 크기)
        var paint = Paint()
        paint.textSize = textSize

        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)

        // 사각형에 맞게 텍스트 크기를 조정
        while (bounds.width() > rect.width() || bounds.height() > rect.height()) {
            textSize -= 1
            paint.textSize = textSize
            paint.getTextBounds(text, 0, text.length, bounds)
        }

        // 텍스트를 사각형 내에 가운데 정렬하여 그리기
        val x = rect.left + (rect.width() - bounds.width()) / 2
        val y = rect.top + (rect.height() + bounds.height()) / 2

        return paint

//        canvas.drawText(text, x.toFloat(), y.toFloat(), paint)
    }



}


