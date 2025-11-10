package com.ezt.video.instasaver.screen.home.dpviewer.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezt.video.instasaver.R
import com.ezt.video.instasaver.databinding.ItemStorySearchViewBinding
import com.ezt.video.instasaver.local.DPRecent
import com.ezt.video.instasaver.screen.view.ViewDPActivity
import com.squareup.picasso.Picasso

class DPRecentViewAdapter( private val dataHolder: List<DPRecent>, private val cookies: String) : RecyclerView.Adapter<DPRecentViewAdapter.DPRecentViewHolder>() {
    private lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DPRecentViewHolder {
        context = parent.context
        val binding = ItemStorySearchViewBinding.inflate(LayoutInflater.from(context), parent, false)
        return DPRecentViewHolder(binding)
    }


    override fun onBindViewHolder(holder: DPRecentViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return dataHolder.size
    }

    inner class DPRecentViewHolder(private val binding : ItemStorySearchViewBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int) {
            val user = dataHolder[position]
            binding.apply {
                Glide.with(context).load(user.profile_pic_url).into(binding.profilePicView)
                usernameView.text=user.username
                fullNameView.text=user.full_name
                root.setOnClickListener {
                    val intent= Intent(context,ViewDPActivity::class.java)
                    intent.putExtra("username",user.username)
                    intent.putExtra("full_name",user.full_name)
                    intent.putExtra("profilePicUrl",user.profile_pic_url)
                    intent.putExtra("userId",user.pk)
                    intent.putExtra("cookies",cookies)
                    context.startActivity(intent)
                }
            }
        }
    }






}