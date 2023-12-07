package com.example.menupop.mainActivity

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.menupop.R
import com.example.menupop.databinding.FoodPreferenceItemBinding
import com.example.menupop.databinding.FoodPreferenceSearchItemBinding

class FoodPreferenceAdapter(private val listener: FoodPreferenceClickListener) : RecyclerView.Adapter<FoodPreferenceAdapter.FoodPreferenceViewHolder>() {
    private var foodList = emptyList<FoodPreference>()
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
        val backgroundDrawable = ContextCompat.getDrawable(holder.itemView.context, R.drawable.radius_30_stroke_light_gray)
        var color = ContextCompat.getColor(holder.itemView.context, R.color.yellow)
        if (foodList[position].classification == "호"){
            color = ContextCompat.getColor(holder.itemView.context, R.color.orange) // 색상 리소스 가져오기
        }
        backgroundDrawable?.setTint(color)

        holder.binding.foodPreferenceItemFavorite.background = backgroundDrawable
        holder.binding.foodPreferenceItemDeleteButton.setOnClickListener {
            listener.deleteBtnClick(foodList[position])
//            listener.favoriteItemClick(foodList[position])
        }
    }

    fun setFoodList(foodList: ArrayList<FoodPreference>) {
        this.foodList = foodList
        notifyDataSetChanged()
    }

}