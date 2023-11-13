package com.example.menupop

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.regex.Pattern
import kotlin.math.log


class SignupViewModel : ViewModel() {

    val TAG = "SignupViewModel"

    // 아이디, 비밀번호, 이메일 등 회원가입과 관련된 데이터를 저장할 MutableLiveData 정의
    val userId = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val confirmPassword = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    val certificationCode = MutableLiveData<String>()

    // 각 입력란에 대한 경고 메시지를 나타낼 MutableLiveData 정의
    val userIdWarning = MutableLiveData<String>()
    val passwordWarning = MutableLiveData<String>()
    val confirmPasswordWarning = MutableLiveData<String>()
    val emailWarning = MutableLiveData<String>()
    val certificationCodeWarning = MutableLiveData<String>()



    // Retrofit 인스턴스
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://3.135.51.201/") // 실제 서버의 기본 URL로 대체해야 합니다.
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(RetrofitService::class.java)




    // 아이디 중복 확인
    suspend fun checkUserIdDuplication() {
        val userIdValue = userId.value ?: ""

        val response = withContext(Dispatchers.IO){
            service.checkUserIdDuplication(userIdValue)
        }

        if(response.isSuccessful){
            val result = response.body()
            Log.d(TAG, "checkUserIdDuplication: ${result}")

            if(result?.isDuplicate == true){
                userIdWarning.value = "이미 사용 중인 아이디 입니다"
            }else{
                userIdWarning.value
            }
        }else{
            userIdWarning.value = "서버 응답 실패"
        }

    }

    // 아이디 유효성 검사
    private fun isUserIdValid(userId: String): Boolean {
        // 여기에 아이디 중복 확인 로직을 구현합니다.
        // 예제로서 단순히 길이가 6자 이상인지 확인하는 예제 코드를 작성했습니다.
        return userId.length >= 6
    }

    // 비밀번호 규칙 확인
    fun checkPasswordRule() {
        val passwordValue = password.value ?: ""
        // 여기에 비밀번호 규칙 확인 로직을 구현합니다.
        // 규칙에 맞지 않을 경우 경고 메시지를 업데이트합니다.
        if (isPasswordValid(passwordValue)) {
            passwordWarning.value = "올바른 비밀번호 형식입니다."
        } else {
            passwordWarning.value = "비밀번호 규칙에 맞지 않습니다."
        }
    }

    // 비밀번호 유효성 검사
    private fun isPasswordValid(password: String): Boolean {
        // 여기에 비밀번호 규칙 확인 로직을 구현합니다.
        // 예제로서 단순히 길이가 8자 이상이고 영문자와 숫자를 포함하는지 확인하는 예제 코드를 작성했습니다.
        val pattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}\$")
        return pattern.matcher(password).matches()
    }


    // 이메일 형식 확인
    fun checkEmailFormat() {
        val emailValue = email.value ?: ""
        // 여기에 이메일 형식 확인 로직을 구현합니다.
        // 형식에 맞지 않을 경우 경고 메시지를 업데이트합니다.
        if (isEmailValid(emailValue)) {
            emailWarning.value = "올바른 이메일 형식입니다."
        } else {
            emailWarning.value = "이메일 형식에 맞지 않습니다."
        }
    }

    // 이메일 유효성 검사
    private fun isEmailValid(email: String): Boolean {
        // 여기에 이메일 형식 확인 로직을 구현합니다.
        // 예제로서 단순히 안드로이드 SDK에서 제공하는 이메일 패턴을 사용하는 예제 코드를 작성했습니다.
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // 회원가입 버튼 클릭 시 동작할 메서드 정의
    fun signUp() {
//        checkUserIdDuplication()
        checkPasswordRule()
        checkEmailFormat()
    }


}