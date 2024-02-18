package com.example.menupop.mainActivity.foodPreference

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.menupop.R
import com.example.menupop.databinding.FoodPreferenceItemBinding

class FoodPreferenceAdapter(private val listener: FoodPreferenceClickListener) : RecyclerView.Adapter<FoodPreferenceAdapter.FoodPreferenceViewHolder>() {
    private var foodList = ArrayList<FoodPreference>()
    class FoodPreferenceViewHolder(val binding: FoodPreferenceItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FoodPreferenceViewHolder {
        val binding = FoodPreferenceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodPreferenceViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return foodList.size
    }

    override fun onBindViewHolder(holder: FoodPreferenceViewHolder, position: Int) {
        holder.binding.items = foodList[position]
        val backgroundDrawable = ContextCompat.getDrawable(holder.itemView.context, R.drawable.circle_empty)
        var color = ContextCompat.getColor(holder.itemView.context, R.color.yellow)
        if (foodList[position].classification == "호"){
            color = ContextCompat.getColor(holder.itemView.context, R.color.orange) // 색상 리소스 가져오기
        }
        backgroundDrawable?.setTint(color)

        holder.binding.foodPreferenceItemFavorite.background = backgroundDrawable
        holder.binding.foodPreferenceItemDeleteButton.setOnClickListener {
            listener.deleteBtnClick(foodList[position],position)
//            listener.favoriteItemClick(foodList[position])
        }
    }


    fun setFoodList(foodList: ArrayList<FoodPreference>) {
        this.foodList = foodList
        notifyDataSetChanged()
    }

}