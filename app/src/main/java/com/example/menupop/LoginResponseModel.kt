package com.example.menupop

import androidx.annotation.Keep

@Keep
data class LoginResponseModel(
    var identifier: Int,
    var isNewUser: Int,
    var result: String
)