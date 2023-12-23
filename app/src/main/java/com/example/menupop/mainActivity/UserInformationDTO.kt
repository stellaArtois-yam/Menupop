package com.example.menupop.mainActivity

data class UserInformationDTO(
    var id : String,
    var email : String,
    var favoriteFood : String,
    var unFavoriteFood : String,
    var foodTicket : Int,
    var translationTicket : Int,
    var freeFoodTicket : Int
)
