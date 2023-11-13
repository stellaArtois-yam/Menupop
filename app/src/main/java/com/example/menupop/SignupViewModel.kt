package com.example.menupop

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SignupViewModel : ViewModel() {
    private val _password = MutableLiveData<String>()
    private val _confirmPassword = MutableLiveData<String>()
    private val _passwordsMatch = MutableLiveData<Boolean>()

    val password: LiveData<String>
        get() = _password

    val confirmPassword: LiveData<String>
        get() = _confirmPassword

    val passwordsMatch: LiveData<Boolean>
        get() = _passwordsMatch

    fun checkPasswords() {
        val passwordValue = _password.value
        val confirmPasswordValue = _confirmPassword.value

        if (!passwordValue.isNullOrBlank() && !confirmPasswordValue.isNullOrBlank()) {
            _passwordsMatch.value = passwordValue == confirmPasswordValue
        } else {
            _passwordsMatch.value = false
        }
    }


}