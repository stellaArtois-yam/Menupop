package com.example.menupop.mainActivity.foodPreference

interface FoodPreferenceItemClickListener {
    fun itemClick(foodName : String, isFavorite : Boolean)
}
interface FoodPreferenceClickListener {
    fun deleteBtnClick(foodPreference: FoodPreference,idx:Int)
}