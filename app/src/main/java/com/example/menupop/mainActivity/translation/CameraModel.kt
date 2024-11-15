package com.example.menupop.mainActivity.translation

import android.util.Log
import com.example.menupop.BuildConfig
import com.example.menupop.RetrofitService
import com.google.android.gms.tasks.Tasks
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

class CameraModel {
    val TAG = "CameraModel"

    val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.TRANSLATION_SERVER_IP)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(okHttpClient)
        .build()

    private val service = retrofit.create(RetrofitService::class.java)

    suspend fun requestTranslation(text: String, language: String) : String {
        return withContext(Dispatchers.IO){
            try{
                val response = service.requestTranslation(text, language)
                if(response.isSuccessful){
                    response.body()!!
                }else{
                    response.code().toString()
                }
            }catch (e : Exception){
                "failed"
            }
        }
    }

    suspend fun recognizedText(image: InputImage, country: String) : Text{

        var recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        if (country == "japan") {
            recognizer = TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())

        } else if (country == "china" || country == "hongkong" || country == "taiwan") {
            recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
        }
        return withContext(Dispatchers.IO) {
            try {
                // Task를 suspend 함수로 변환
                val result = Tasks.await(recognizer.process(image))
                Log.d(TAG, "recognizedText Success: ${result.text}")
                return@withContext result
            } catch (e: Exception) {
                Log.d(TAG, "recognizedText failed: ${e.message}")
                throw e // 실패 시 예외를 던져서 호출한 곳에서 처리
            }
        }
    }


    suspend fun checkLanguage(text: String) : String {

        Log.d(TAG, "checkLanguage: $text")

        val languageIdentifier = LanguageIdentification
            .getClient(LanguageIdentificationOptions.Builder()
                .setConfidenceThreshold(0.2f)
                .build())

//        languageIdentifier.identifyPossibleLanguages(text)
//            .addOnSuccessListener { identifiedLanguages ->
//                for (identifiedLanguage in identifiedLanguages) {
//                    val language = identifiedLanguage.languageTag
//                    val confidence = identifiedLanguage.confidence
//                    Log.i(TAG, "$language $confidence")

//                    if (language != "und") {
//                    Log.d(TAG, "checkLanguage: $languageCode")
//                        callback(language)
//
//                    } else {
//                        Log.d(TAG, "checkLanguage und")
//                        callback("und")
//
//                    }
//                }
//            }
//            .addOnFailureListener {
//                Log.d(TAG, "checkLanguage e: ${it.message}")
//                callback("failed")
//            }

        return withContext(Dispatchers.IO) {
            try {
                // Task를 suspend 함수로 변환
                val result = Tasks.await(languageIdentifier.identifyLanguage(text))
                Log.d(TAG, "recognizedText Success: $result")
                return@withContext result
            } catch (e: Exception) {
                Log.d(TAG, "recognizedText failed: ${e.message}")
                throw e // 실패 시 예외를 던져서 호출한 곳에서 처리
            }
        }

    }
}