package com.ezt.video.instasaver.screen.home.dpviewer.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ezt.video.instasaver.databinding.ItemStorySearchViewBinding
import com.ezt.video.instasaver.model.SearchUser
import com.ezt.video.instasaver.model.User
import com.squareup.picasso.Picasso

class StorySearchViewAdapter(
    private var dataHolder: List<SearchUser>,
    private val startIntent: (User) -> Unit
) : RecyclerView.Adapter<StorySearchViewAdapter.StorySearchViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StorySearchViewHolder {
        val binding = ItemStorySearchViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StorySearchViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: StorySearchViewHolder,
        position: Int
    ) {
       holder.bind(position)
    }

    override fun getItemCount(): Int = dataHolder.size

    @SuppressLint("NotifyDataSetChanged")
    fun clearData() {
        dataHolder = listOf()
        notifyDataSetChanged()
    }


    inner class StorySearchViewHolder(private val binding: ItemStorySearchViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val user = dataHolder[position].user
            binding.apply {
                Picasso.get().load(user.profile_pic_url).into(profilePicView)
                usernameView.text=user.username
                fullNameView.text=user.full_name
                root.setOnClickListener{
                    startIntent(user)
                }
            }
        }
    }
}