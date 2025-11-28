package com.ezt.video.instasaver.screen.home.profile.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezt.video.instasaver.R
import com.ezt.video.instasaver.databinding.ItemUserPostBinding
import com.ezt.video.instasaver.model.Items

class UserPostAdapter() : RecyclerView.Adapter<UserPostAdapter.UserPostViewHolder>() {
    private val allPosts = mutableListOf<Items>()
    private lateinit var context: Context
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserPostAdapter.UserPostViewHolder {
        context = parent.context
        val binding =
            ItemUserPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserPostViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: UserPostViewHolder,
        position: Int
    ) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = allPosts.size

    fun submitList(input: List<Items>) {
        allPosts.clear()
        allPosts.addAll(input)
        notifyDataSetChanged()
    }

    inner class UserPostViewHolder(private val binding: ItemUserPostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                val item = allPosts[position]

                val mediaType = item.media_type
                when (mediaType) {
                    8 -> mediaTypeIconView.setImageResource(R.drawable.ic_copy)
                    2 -> mediaTypeIconView.setImageResource(R.drawable.ic_play)
                    else -> mediaTypeIconView.visibility = View.GONE
                }


                Glide.with(context).load(item.image_versions2.candidates[0].url ).into(imageView)

            }
        }
    }
}