package com.example.menupop.mainActivity.profile

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.example.menupop.R

class ProfileAdapter(private val drawableList: List<Drawable>) : RecyclerView.Adapter<ProfileAdapter.ProfileAdapterViewHolder>() {
    private val imageList = ArrayList<ProfileSelectionDTO>()

    class ProfileAdapterViewHolder(itemView : View) :  RecyclerView.ViewHolder(itemView){
        val imageView : ImageView = itemView.findViewById(R.id.profile_selection_item)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int): ProfileAdapter.ProfileAdapterViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_profile_selection, parent, false)
        return ProfileAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileAdapterViewHolder, position: Int) {
        val drawable = drawableList[position]
        holder.imageView.setImageDrawable(drawable)
    }

    override fun getItemCount(): Int {
        return drawableList.size
    }




}