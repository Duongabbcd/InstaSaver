package com.ezt.video.instasaver.screen.home.profile.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezt.video.instasaver.R
import com.ezt.video.instasaver.databinding.ItemUserPostBinding
import com.ezt.video.instasaver.model.Items
import com.ezt.video.instasaver.screen.view.post.ViewPostActivity
import com.google.gson.Gson

class UserPostAdapter(private val onSelectItemListener: (Items) -> Unit) :
    PagingDataAdapter<Items, UserPostAdapter.UserPostViewHolder>(DIFF_CALLBACK) {
    private val allSelectedPosts = mutableListOf<Items>()


    private lateinit var context: Context
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserPostViewHolder {
        context = parent.context
        val binding =
            ItemUserPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserPostViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: UserPostViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        holder.bind(item)
    }

    fun getSelectedPosts(): List<Items> = allSelectedPosts.toList()
    fun clearSelectedPosts() {
        allSelectedPosts.clear()
    }

    inner class UserPostViewHolder(private val binding: ItemUserPostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Items?) {
            if (item == null) return
            binding.apply {

                val mediaType = item.media_type
                when (mediaType) {
                    8 -> mediaTypeIconView.setImageResource(R.drawable.ic_copy)
                    2 -> mediaTypeIconView.setImageResource(R.drawable.ic_play)
                    else -> mediaTypeIconView.visibility = View.GONE
                }
                // Update selection UI
                if (allSelectedPosts.contains(item)) {
                    binding.selectPost.setImageResource(R.drawable.icon_selected)
                } else {
                    binding.selectPost.setImageResource(R.drawable.icon_unselected)
                }

                Glide.with(context).load(item.image_versions2.candidates[0].url)
                    .placeholder(R.drawable.item_wallpaper_default)
                    .error(R.drawable.item_wallpaper_default).into(imageView)

                selectPost.setOnClickListener {
                    if (allSelectedPosts.contains(item)) {
                        allSelectedPosts.remove(item)
                    } else {
                        allSelectedPosts.add(item)
                    }

                    // Only refresh this item
                    notifyItemChanged(position)

                    // Call listener to inform activity/fragment
                    onSelectItemListener(item)
                }

                root.setOnClickListener {
                    val viewIntent = Intent(context, ViewPostActivity::class.java)
                    val postDetail = Bundle()
                    val carousel_media = Gson().toJson(item.carousel_media)
                    postDetail.putString("name", item.caption?.text ?: "")
                    postDetail.putInt("media_type", mediaType)
                    postDetail.putString("caption", item.caption?.text ?: "")
                    postDetail.putString("username", item.user.username)
                    postDetail.putString("profilePicture", item.user.profile_pic_url)
                    postDetail.putString("instagram_url", "")
                    postDetail.putString("carousel_media", carousel_media)
                    postDetail.putString("imageUrl", item.image_versions2.candidates[0].url)
                    postDetail.putString("videoUrl",if(mediaType == 2) item.video_versions[0].url else "")
                    postDetail.putBoolean("isStory", false)
                    viewIntent.putExtras(postDetail)
                    context.startActivity(viewIntent)
                }
            }
        }
    }


    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Items>() {

            override fun areItemsTheSame(oldItem: Items, newItem: Items): Boolean {
                return oldItem.user == newItem.user   // Must be unique ID from API
            }

            override fun areContentsTheSame(oldItem: Items, newItem: Items): Boolean {
                return oldItem == newItem         // Requires Items to be a data class!
            }
        }
    }
}