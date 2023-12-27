package com.example.menupop.mainActivity.translation

import android.graphics.Bitmap
import android.util.Log
import com.example.menupop.RetrofitService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import okhttp3.OkHttpClient
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
//
//    private val okHttpClient = OkHttpClient.Builder()
//        .connectTimeout(30, TimeUnit.SECONDS)
//        .readTimeout(30, TimeUnit.SECONDS)
//        .writeTimeout(30, TimeUnit.SECONDS)
//        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://3.135.51.201:9876")
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(gson))
//        .client(okHttpClient)
        .build()

    private val service = retrofit.create(RetrofitService::class.java)

    fun  requestTranslation(text : String, language : String, callback: (String) -> Unit){
        service.requestTranslation(text, language).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.isSuccessful){
                    Log.d(TAG, "onResponse: ${response.body()}")
                    callback(response.body()!!)
                }else{
                    Log.d(TAG, "is not Successful: ${response.body()}")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

    fun recognizedText(image : InputImage, callback: (Text) -> Unit) {

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener {visionText ->
//                Log.d(TAG, "recognizedText Success: ${visionText.text}")
                callback(visionText)

            }
            .addOnFailureListener { e ->
                Log.d(TAG, "recognizedText: ${e.message}")
            }


    }

    fun checkLanguage(text: String, callback: (String) -> Unit) {

        val languageIdentifier = LanguageIdentification.getClient()

        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                if (languageCode != "und") {
                    Log.d(TAG, "checkLanguage: $languageCode")
                    callback(languageCode)

                } else {
                    Log.d(TAG, "checkLanguage: und")
                    callback("und")

                }
            }
            .addOnFailureListener {
                Log.d(TAG, "checkLanguage e: ${it.message}")
            }

    }
}