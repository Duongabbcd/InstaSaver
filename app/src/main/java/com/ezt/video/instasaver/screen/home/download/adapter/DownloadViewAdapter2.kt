package com.ezt.video.instasaver.screen.home.download.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezt.video.instasaver.R
import com.ezt.video.instasaver.databinding.ItemDownloadViewBinding
import com.ezt.video.instasaver.local.Post
import com.ezt.video.instasaver.screen.view.post.ViewPostActivity
import com.ezt.video.instasaver.utils.Constants
import com.squareup.picasso.Picasso
import java.io.File

class DownloadViewAdapter2() : RecyclerView.Adapter<DownloadViewAdapter2.DownloadViewHolder2>(){
    private val allPosts = mutableListOf<Post>()
    private lateinit var context: Context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DownloadViewHolder2 {
        context = parent.context
        val binding = ItemDownloadViewBinding.inflate(LayoutInflater.from(context), parent, false)
        return DownloadViewHolder2(binding)
    }

    override fun onBindViewHolder(
        holder: DownloadViewHolder2,
        position: Int
    ) {
      holder.bind(position)
    }

    override fun getItemCount(): Int = allPosts.size

    fun submitList(posts: List<Post>) {
        allPosts.clear()
        allPosts.addAll(posts)
        notifyDataSetChanged()
    }

    inner class DownloadViewHolder2(private val binding: ItemDownloadViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val post = allPosts[position]
            println("DownloadViewHolder2: $post")
            binding.apply {
                val mediaType=post.media_type
                val postPath = post.path ?: Constants.STORY_FOLDER_NAME
                if (post.title.isNullOrEmpty()) {
                    return@apply
                }

                val input = File(postPath, post.title)
                val uri = Uri.fromFile(input)
                Glide.with(context).load(uri).into(imageView)

                when (mediaType){
                    8 -> mediaTypeIconView.setImageResource(R.drawable.ic_copy)
                    2 -> mediaTypeIconView.setImageResource(R.drawable.ic_play)
                    else -> mediaTypeIconView.visibility= View.GONE
                }

                val avatarFile = File(Constants.AVATAR_FOLDER_NAME, post.username.plus(".jpg"))
                println("DownloadViewHolder file path 1 ${post.username} $avatarFile")
                Glide.with(context).load(Uri.fromFile(avatarFile)).into(profilePicView)

                root.setOnClickListener {
                    val viewIntent = Intent(context, ViewPostActivity::class.java)
                    val postDetail= Bundle()
                    postDetail.putString("name",post.title)
                    postDetail.putInt("media_type",mediaType)
                    postDetail.putString("caption",post.caption)
                    postDetail.putString("username",post.username)
                    postDetail.putString("profilePicture",post.profile_pic_url)
                    postDetail.putString("instagram_url",post.link)
                    postDetail.putString("imageUrl",post.image_url)
                    postDetail.putString("videoUrl",post.video_url)
                    viewIntent.putExtras(postDetail)
                    context.startActivity(viewIntent)
                }

                usernameView.text = post.username
                captionView.text = post.caption

            }

        }
    }
}