package com.example.menupop.mainActivity.profile

data class KakaoPayReadyResponseDTO(
    var tid : String,
    var next_redirect_app_url: String,
    var next_redirect_mobile_url : String,
    var next_redirect_pc_url : String,
    var android_app_scheme : String,
    var ios_app_scheme : String,
    var created_at : String
)
