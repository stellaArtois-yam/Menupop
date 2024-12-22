package com.example.menupop.mainActivity.foodPreference

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.menupop.databinding.FoodPreferenceSearchItemBinding

class FoodPreferenceSearchAdapter(private val listener: FoodPreferenceItemClickListener) : RecyclerView.Adapter<FoodPreferenceSearchAdapter.FoodPreferenceSearchViewHolder>() {
    private var foodList = emptyList<String>()
    class FoodPreferenceSearchViewHolder(val binding: FoodPreferenceSearchItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FoodPreferenceSearchViewHolder {
        val binding = FoodPreferenceSearchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodPreferenceSearchViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return foodList.size
    }

    override fun onBindViewHolder(holder: FoodPreferenceSearchViewHolder, position: Int) {
        holder.binding.item = foodList[position]
        holder.binding.foodPreferenceSearchFavorite.setOnClickListener {
            listener.itemClick(foodList[position], true)
        }
        holder.binding.foodPreferenceSearchUnfavorite.setOnClickListener {
            listener.itemClick(foodList[position], false)
        }
    }

    fun setFoodList(foodList: ArrayList<String>) {
        this.foodList = foodList
        notifyDataSetChanged()
    }

}