package com.example.menupop.mainActivity

interface FoodPreferenceItemClickListener {
    fun favoriteItemClick(foodName : String)
    fun unFavoriteItemClick(foodName:  String)
}