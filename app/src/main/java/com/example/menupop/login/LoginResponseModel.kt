package com.example.menupop.Login

import androidx.annotation.Keep

data class LoginResponseModel(
    var identifier: Int,
    var isNewUser: Int,
    var result: String
)