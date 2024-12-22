package com.example.menupop.mainActivity.profile

import com.google.gson.annotations.SerializedName

data class KakaoPayReadyResponseDTO(
    var tid: String = "N/A",
    @SerializedName("next_redirect_app_url") var nextRedirectAppUrl: String? = null,
    @SerializedName("next_redirect_mobile_url") var nextRedirectMobileUrl: String? = null,
    @SerializedName("next_redirect_pc_url") var nextRedirectPcUrl: String? = null,
    @SerializedName("android_app_scheme") var androidAppScheme: String? = null,
    @SerializedName("ios_app_scheme") var iosAppScheme: String? = null,
    @SerializedName("created_at") var createdAt: String? = null
)
