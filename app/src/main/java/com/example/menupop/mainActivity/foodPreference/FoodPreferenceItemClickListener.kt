package com.example.menupop.mainActivity.foodPreference

interface FoodPreferenceItemClickListener {
    fun favoriteItemClick(foodName : String)
    fun unFavoriteItemClick(foodName:  String)
}
interface FoodPreferenceClickListener {
    fun deleteBtnClick(foodPreference: FoodPreference,idx:Int)
}