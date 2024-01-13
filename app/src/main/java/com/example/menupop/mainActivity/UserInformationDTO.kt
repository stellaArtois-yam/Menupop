package com.example.menupop.mainActivity

import android.graphics.drawable.Drawable

data class UserInformationDTO(
    var id : String,
    var email : String,
    var favoriteFood : String,
    var unFavoriteFood : String,
    var foodTicket : Int,
    var translationTicket : Int,
    var freeTranslationTicket : Int,
    var freeFoodTicket : Int,
    var dailyReward : Int,
    var haveRewarded : Int
)
