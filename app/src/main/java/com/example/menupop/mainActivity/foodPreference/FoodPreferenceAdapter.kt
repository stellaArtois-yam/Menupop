package com.example.menupop.mainActivity.foodPreference

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.menupop.R
import com.example.menupop.databinding.FoodPreferenceItemBinding

class FoodPreferenceAdapter(private val listener: FoodPreferenceClickListener) : RecyclerView.Adapter<FoodPreferenceAdapter.FoodPreferenceViewHolder>() {
    companion object{
        const val TAG ="FoodPreferenceFragment Adapter"
    }

    private var foodList : ArrayList<FoodPreference> = arrayListOf()
    class FoodPreferenceViewHolder(val binding: FoodPreferenceItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FoodPreferenceViewHolder {
        val binding = FoodPreferenceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        Log.d(TAG, "onCreateViewHolder: 호출")
        return FoodPreferenceViewHolder(binding)
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount: 호출")
        return foodList.size
    }

    override fun onBindViewHolder(holder: FoodPreferenceViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: ${holder.binding.items}")
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
        Log.d(TAG, "notifyDataSetChanged 호출 : $foodList")
    }

}