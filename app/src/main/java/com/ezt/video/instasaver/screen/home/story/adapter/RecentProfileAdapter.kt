package com.ezt.video.instasaver.screen.home.story.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ezt.video.instasaver.databinding.ItemStorySearchViewBinding
import com.ezt.video.instasaver.local.ProfileRecent
import com.ezt.video.instasaver.local.StoryRecent
import com.ezt.video.instasaver.model.User
import com.ezt.video.instasaver.screen.download.DownloadStoryActivity
import com.ezt.video.instasaver.screen.home.profile.ViewProfileActivity
import com.ezt.video.instasaver.utils.Common.visible
import com.ezt.video.instasaver.utils.Constants
import com.squareup.picasso.Picasso
import java.io.File

class RecentProfileAdapter(
    private val dataHolder: List<ProfileRecent>,
    private val cookies: String,
    private val onDeleteItem: (ProfileRecent) -> Unit
) :
    RecyclerView.Adapter<RecentProfileAdapter.RecentProfileViewHolder>() {
    private lateinit var context: Context
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecentProfileViewHolder {
        context = parent.context
        val binding =
            ItemStorySearchViewBinding.inflate(LayoutInflater.from(context), parent, false)
        return RecentProfileViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RecentProfileViewHolder,
        position: Int
    ) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = dataHolder.size

    inner class RecentProfileViewHolder(private val binding: ItemStorySearchViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val profileRecent: ProfileRecent = dataHolder[position]
            binding.apply {
                println("RecentProfileViewHolder: $profileRecent")
                val profilePicture =
                    File(Constants.AVATAR_FOLDER_NAME, profileRecent.username.plus(".jpg"))
                Picasso.get().load(profilePicture).into(profilePicView)
                usernameView.text = profileRecent.username
                fullNameView.text = profileRecent.full_name

                iconDelete.visible()
                iconDelete.setOnClickListener {
                    onDeleteItem(profileRecent)
                }

                root.setOnClickListener {

                val intent = Intent(context, ViewProfileActivity::class.java)
                    intent.putExtra("username", profileRecent.username)
                    intent.putExtra("fullName", profileRecent.full_name)
                    intent.putExtra("cookies", cookies)
                    intent.putExtra("userId", profileRecent.pk)
                    context.startActivity(intent)
                }
            }
        }
    }
}