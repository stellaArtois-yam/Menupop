package com.example.menupop.login

import android.content.Context
import android.util.Log
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SocialLoginManager(private val context : Context) {

    companion object{
        const val TAG ="SocialLoginManager"
    }

    suspend fun requestKakaoSocialLogin(): OAuthToken = suspendCoroutine { continuation ->
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(
                context,
                callback = { token, error ->
                    if (error != null) {
                        Log.d(TAG, "requestKakaoSocialLogin error up: $error")
                        continuation.resumeWithException(error)  // 실패 시 예외 던짐
                    } else {
                        token?.let {
                            Log.d(TAG, "requestKakaoSocialLogin token up: $it.ㅅ")
                            continuation.resume(it)  // 성공 시 토큰 반환
                        } ?: continuation.resumeWithException(NullPointerException("OAuthToken is null"))  // 토큰이 null일 경우 예외 던짐
                    }
                }
            )
        } else {
            UserApiClient.instance.loginWithKakaoAccount(
                context,
                callback = { token, error ->
                    if (error != null) {
                        Log.d(TAG, "requestKakaoSocialLogin error down: $error")
                        continuation.resumeWithException(error)  // 실패 시 예외 던짐
                    } else {
                        token?.let {
                            Log.d(TAG, "requestKakaoSocialLogin token down: $it")
                            continuation.resume(it)  // 성공 시 토큰 반환
                        } ?: continuation.resumeWithException(NullPointerException("OAuthToken is null"))  // 토큰이 null일 경우 예외 던짐
                    }
                }
            )
        }
    }
}