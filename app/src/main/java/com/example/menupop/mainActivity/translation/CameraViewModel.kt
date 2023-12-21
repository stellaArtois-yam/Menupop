package com.example.menupop.mainActivity.translation

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zynksoftware.documentscanner.model.ScannerResults


class CameraViewModel : ViewModel() {
    private var callbackResult : ((String) -> Unit) ?= null
    val TAG = "CameraViewModel"
    val scannerResult = MutableLiveData<ScannerResults>()
    val cameraModel = CameraModel()

    fun successScanning(scannerResults: ScannerResults){
        scannerResult.value = scannerResults
    }

}