package com.example.menupop.mainActivity

import com.example.menupop.mainActivity.foodPreference.FoodPreference

data class UserInformationDTO(
    var result : String?,
    var id : String?,
    var email : String?,
    var foodPreference : ArrayList<FoodPreference>?,
    var foodTicket : Int,
    var translationTicket : Int,
    var freeTranslationTicket : Int,
    var freeFoodTicket : Int,
    var dailyReward : Int, //받을 수 있는 리워드
    var availableReward : Int // 보유 리워드
)


//    var favoriteFood : String?,
//    var unFavoriteFood : String?,
