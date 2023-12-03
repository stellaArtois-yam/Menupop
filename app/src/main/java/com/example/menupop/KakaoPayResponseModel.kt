package com.example.menupop

data class KakaoPayResponseModel(
    var tid : String,
    var nextRedirectAppUrl: String,
    var nextRedirectMobileUrl : String,
    var nextRedirectPcUrl : String,
    var androidAppScheme : String,
    var iosAppScheme : String,
    var createdAt : String
)
