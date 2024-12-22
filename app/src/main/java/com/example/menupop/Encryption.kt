package com.example.menupop

import java.security.MessageDigest
import java.security.SecureRandom

class Encryption {

    // Salt 생성 (16바이트 랜덤 데이터)
    fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return salt
    }

    // SHA-256 해싱 (Salt 포함)
    fun hashWithSalt(input: String, salt: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")

        // Salt와 입력 데이터 결합
        digest.update(salt)
        val hashBytes = digest.digest(input.toByteArray())

        // 해시 결과를 16진수 문자열로 변환
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    // Salt를 문자열로 변환 (저장용)
    fun saltToString(salt: ByteArray): String {
        return salt.joinToString("") { "%02x".format(it) }
    }

    // 문자열을 Salt로 변환 (복구용)
    fun stringToSalt(saltString: String): ByteArray {
        return ByteArray(saltString.length / 2) {
            saltString.substring(it * 2, it * 2 + 2).toInt(16).toByte()
        }
    }
}