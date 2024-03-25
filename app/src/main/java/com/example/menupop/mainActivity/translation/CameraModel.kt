package com.example.menupop.mainActivity.translation

import android.graphics.Bitmap
import android.util.Log
import com.example.menupop.BuildConfig
import com.example.menupop.RetrofitService
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
import okhttp3.OkHttpClient
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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

    fun requestTranslation(text: String, language: String, callback: (String) -> Unit) {
        service.requestTranslation(text, language).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {

//                    val jsonArray = JSONArray(response.body())
//                    var decode = jsonArray.getString(0)
//
//                    val decodeList = decode.split("%")
//                    Log.d(TAG, "onResponse: $decodeList")

                    callback(response.body()!!)
                } else {
                    Log.d(TAG, "is not Successful: ${response.body()}")
                    callback("failed")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
                callback("failed")
            }
        })
    }

    fun recognizedText(image: InputImage, country: String, callback: (Text) -> Unit) {

        var recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        if (country == "japan") {
            recognizer = TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())

        } else if (country == "china" || country == "hongkong" || country == "taiwan") {
            recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
        }


        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                Log.d(TAG, "recognizedText Success: ${visionText.text}")
                callback(visionText)
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "recognizedText e: ")
            }


    }


    fun checkLanguage(text: String, callback: (String) -> Unit) {

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

        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                if (languageCode != "und") {
                    Log.d(TAG, "checkLanguage: $languageCode")
                    callback(languageCode)

                } else {
                    Log.d(TAG, "checkLanguage und")
                    callback("und")

                }
            }
            .addOnFailureListener {
                Log.d(TAG, "checkLanguage e: ${it.message}")
                callback("failed")
            }

    }
}