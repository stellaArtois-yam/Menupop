package com.example.menupop.mainActivity.foodPreference

import java.io.Serializable

data class FoodPreferenceDataClass(val result:String , val foodList : ArrayList<FoodPreference>)
data class FoodPreference(val foodName:String,val classification:String):
    Serializable