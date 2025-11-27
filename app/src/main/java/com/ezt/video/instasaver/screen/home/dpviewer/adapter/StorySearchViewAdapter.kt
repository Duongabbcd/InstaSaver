package com.ezt.video.instasaver.screen.home.dpviewer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezt.video.instasaver.databinding.ItemStorySearchViewBinding
import com.ezt.video.instasaver.model.SearchUser
import com.ezt.video.instasaver.model.User
import com.ezt.video.instasaver.utils.Constants
import com.squareup.picasso.Picasso
import java.io.File

class StorySearchViewAdapter(
    private var dataHolder: List<SearchUser>,
    private val startIntent: (User) -> Unit
) : RecyclerView.Adapter<StorySearchViewAdapter.StorySearchViewHolder>() {
    private lateinit var context: Context
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StorySearchViewHolder {
        context = parent.context
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

            println("StorySearchViewHolder: $user")
            binding.apply {
                val profilePicture = File(Constants.AVATAR_FOLDER_NAME, user.username.plus(".jpg"))
                println("DownloadViewHolder file path 1 ${user.username} $profilePicture")

                if (profilePicture.exists()) {
                    Glide.with(context).load(Uri.fromFile(profilePicture))
                        .into(binding.profilePicView)
                }

                usernameView.text=user.username
                fullNameView.text=user.full_name
                root.setOnClickListener{
                    startIntent(user)
                }
            }
        }
    }
}