package com.ezt.video.instasaver.screen.home.story.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezt.video.instasaver.databinding.ItemStorySearchViewBinding
import com.ezt.video.instasaver.local.StoryRecent
import com.ezt.video.instasaver.screen.download.DownloadStoryActivity
import com.ezt.video.instasaver.utils.Constants
import java.io.File

class RecentViewAdapter(private val dataHolder: List<StoryRecent>, private val cookies: String) :
    RecyclerView.Adapter<RecentViewAdapter.RecentViewHolder>() {
    private lateinit var context: Context
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecentViewHolder {
        context = parent.context
        val binding =
            ItemStorySearchViewBinding.inflate(LayoutInflater.from(context), parent, false)
        return RecentViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RecentViewHolder,
        position: Int
    ) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = dataHolder.size

    inner class RecentViewHolder(private val binding: ItemStorySearchViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val storyRecent = dataHolder[position]
            binding.apply {
                val profilePicture =
                    File(Constants.AVATAR_FOLDER_NAME, storyRecent.username.plus(".jpg"))
                Glide.with(context).load(profilePicture).into(profilePicView)


                usernameView.text = storyRecent.username
                fullNameView.text = storyRecent.full_name
                root.setOnClickListener {
                    val intent = Intent(context, DownloadStoryActivity::class.java)
                    intent.putExtra("username", storyRecent.username)
                    intent.putExtra("full_name", storyRecent.full_name)
                    intent.putExtra("profilePicUrl", storyRecent.profile_pic_url)
                    intent.putExtra("userId", storyRecent.pk)
                    intent.putExtra("cookie", cookies)
                    context.startActivity(intent)
                }
            }
        }
    }
}