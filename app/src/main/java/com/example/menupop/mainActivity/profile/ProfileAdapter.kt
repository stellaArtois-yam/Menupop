package com.example.menupop.mainActivity.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.example.menupop.databinding.ProfileSelectionItemBinding

class ProfileAdapter(val clickListener: ProfileSelectionClickListener) : RecyclerView.Adapter<ProfileAdapter.ProfileAdapterViewHolder>() {
    private var imageList = ArrayList<ProfileSelectionDTO>()

    class ProfileAdapterViewHolder(val binding : ProfileSelectionItemBinding) :  RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int): ProfileAdapterViewHolder{

       val binding = ProfileSelectionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProfileAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProfileAdapterViewHolder, position: Int) {
        holder.binding.items = imageList[position]

        holder.binding.profileSelectionItem.setOnClickListener{
            clickListener.onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    fun setProfileSelection(imageList : ArrayList<ProfileSelectionDTO>){
        this.imageList = imageList
        notifyDataSetChanged()
    }




}