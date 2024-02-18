package com.example.menupop.resetPassword

interface ResetPasswordFragmentEvent {
    fun existId()
    fun successVerifyEmail()
    fun successResetPassword()
    fun backBtnClick()
}