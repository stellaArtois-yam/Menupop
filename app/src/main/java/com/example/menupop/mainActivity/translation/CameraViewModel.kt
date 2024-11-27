package com.example.menupop.mainActivity.translation

import android.app.Application
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
import androidx.lifecycle.viewModelScope
import com.example.menupop.mainActivity.foodPreference.FoodPreference
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.zynksoftware.documentscanner.model.ScannerResults
import kotlinx.coroutines.launch
import org.json.JSONArray

class CameraViewModel(val application: Application) : ViewModel() {

    companion object {
        const val TAG = "CameraViewModel"
    }

    private val cameraModel = CameraModel()

    private val _scannerResult = MutableLiveData<ScannerResults>()
    val scannerResult: LiveData<ScannerResults>
        get() = _scannerResult

    private val _failed = MutableLiveData<Boolean>()
    val failed: LiveData<Boolean>
        get() = _failed

    private val _textPosition = MutableLiveData<ArrayList<Rect>>()

    private val _image = MutableLiveData<Drawable>()
    val image: LiveData<Drawable>
        get() = _image


    fun successScanning(scannerResults: ScannerResults) {
        _scannerResult.value = scannerResults
    }

    private lateinit var likesFoodList: ArrayList<String>
    private lateinit var unLikesFoodList: ArrayList<String>



    fun setFoodPreference(foodPreference: ArrayList<FoodPreference>) {
        if (foodPreference.isNotEmpty()) {
            val (likesFoodList, unLikesFoodList) = splitFoodPreferenceList(foodPreference)
            this.likesFoodList = likesFoodList
            this.unLikesFoodList = unLikesFoodList
            Log.d(TAG, "setFoodPreference: $likesFoodList, $unLikesFoodList")
        }
    }


    fun getRecognizedText(image: InputImage, country: String) {
        viewModelScope.launch {
            val visionText = cameraModel.recognizedText(image, country)
            if (visionText.text.isNotEmpty()) {

                val resultText = getText(visionText)

                if (country == "taiwan" ) {
                    requestTranslation(resultText, "zh-TW")
                } else {
                    checkLanguage(resultText)
                }
            } else {
                _failed.value = true
            }
        }
    }

    private suspend fun checkLanguage(text: String) {
        viewModelScope.launch {
            val langCode = cameraModel.checkLanguage(text)
            if (langCode != "und" && langCode != "failed") {
                requestTranslation(text, langCode)
                Log.d(TAG, "checkLanguage: $langCode")
            } else {
                Log.d(TAG, "checkLanguage else: $langCode")
                _failed.value = true
            }
        }
    }

    private fun splitFoodPreferenceList(foodPreferenceList: List<FoodPreference>): Pair<ArrayList<String>, ArrayList<String>> {
        val likes = ArrayList<String>()
        val dislikes = ArrayList<String>()

        for (foodPreference in foodPreferenceList) {
            if (foodPreference.classification == "호") {
//                Log.d(TAG, "호: ${foodPreference.foodName}")
                likes.add(foodPreference.foodName)
            } else if (foodPreference.classification == "불호") {
//                Log.d(TAG, "불호: ${foodPreference.foodName}")
                dislikes.add(foodPreference.foodName)
            }
        }

        return Pair(likes, dislikes)
    }


    private suspend fun requestTranslation(text: String, langCode: String) {
        viewModelScope.launch {
            val result = cameraModel.requestTranslation(text, langCode)
            if (result != "failed") {
                Log.d(TAG, "translated Text: $result")

                val jsonArray = JSONArray(result)
                val decode = jsonArray.getString(0)

                val decodeList = decode.split("__")

                Log.d(TAG, "requestTranslation: ${decodeList}\nsize : ${decodeList.size}")
                if (decode != null) {
                    drawTranslatedText(decodeList)
                }
            } else {
                Log.d(TAG, "requestTranslation is Failed : $result")
                _failed.value = true
            }
        }
    }

    private fun getText(text: Text): String {
        val positionList = ArrayList<Rect>()
        var requestText = ""

        for (block in text.textBlocks) {

            for (line in block.lines) {
                val lineText = line.text.lowercase()
                val lineFrame = line.boundingBox

                requestText = requestText.plus(lineText).plus("__")
                positionList.add(lineFrame!!)
            }

            _textPosition.value = positionList

        }

        //마지막 '%' 잘라주기
        requestText = requestText
            .substring(0, requestText.length - 2)
            .replace(",", "").replace("|", "")
        Log.d(TAG, "text : $requestText")
        Log.d(TAG, "getText position size: ${positionList.size}")
        return requestText
    }


    private fun drawTranslatedText(textList: List<String>) {

        val filePath = scannerResult.value!!.croppedImageFile!!.path
        val bitmap = BitmapFactory.decodeFile(filePath)

        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)


        val paintRect = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }


        for (i: Int in textList.indices) {
            val (paintText, x, y) = getTextSize(_textPosition.value!![i], textList[i])

            val left = _textPosition.value!![i].left.toFloat()
            val top = _textPosition.value!![i].top.toFloat()
            val right = _textPosition.value!![i].right.toFloat()
            val bottom = _textPosition.value!![i].bottom.toFloat()

            var color = Color.BLACK // 기본 색상

            for (text in likesFoodList) {
                if (textList[i].contains(text)) {
                    color = Color.rgb(255, 127, 9)
                    break
                }
            }

            for (text in unLikesFoodList) {
                if (textList[i].contains(text)) {
                    color = Color.rgb(255, 173, 13)
                    break
                }
            }

            paintText.color = color // 마지막에 설정된 색상으로 텍스트 색상을 설정

            canvas.drawRect(left, top, right, bottom, paintRect)
            canvas.drawText(textList[i], x, y, paintText)
        }

        _image.value = BitmapDrawable(application.applicationContext.resources, mutableBitmap)
    }


    private fun getTextSize(rect: Rect, text: String): Triple<Paint, Float, Float> {

        var textSize = 50f // 텍스트 크기 초기값 설정 (임의의 크기)
        val paint = Paint()
        paint.textSize = textSize
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.color = Color.BLACK

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

        return Triple(paint, x.toFloat(), y.toFloat())

    }

    suspend fun useTranslationTicket(identifier : Int){
        viewModelScope.launch {
            when(cameraModel.useTranslationTicket(identifier)){
                "success" -> _failed.value = false
                "failed" -> _failed.value = true
            }
        }
    }

}


