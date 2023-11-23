package com.example.menupop.mainActivity

data class UserInformationData(
    var id : String,
    var email : String,
    var favoriteFood : List<String>,
    var unFavoriteFood : List<String>,
    var foodTicket : Int,
    var translationTicket : Int
)
