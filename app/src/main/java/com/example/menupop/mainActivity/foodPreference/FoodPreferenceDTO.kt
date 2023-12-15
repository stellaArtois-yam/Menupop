package com.example.menupop.mainActivity.foodPreference

data class FoodPreferenceDataClass(val result:String , val foodList : ArrayList<FoodPreference>)
data class FoodPreference(val foodName:String,val classification:String)