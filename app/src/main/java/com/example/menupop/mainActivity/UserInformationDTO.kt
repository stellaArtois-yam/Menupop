package com.example.menupop.mainActivity

import com.example.menupop.mainActivity.foodPreference.FoodPreference

data class UserInformationDTO(
    var result : String,
    var id : String? = null,
    var email : String? = null,
    var foodPreference : ArrayList<FoodPreference>?,
    var foodTicket : Int = 0,
    var translationTicket : Int = 0,
    var freeTranslationTicket : Int = 0,
    var freeFoodTicket : Int = 0,
    var dailyReward : Int = 0, //받을 수 있는 리워드
    var availableReward : Int = 0 // 보유 리워드
)
