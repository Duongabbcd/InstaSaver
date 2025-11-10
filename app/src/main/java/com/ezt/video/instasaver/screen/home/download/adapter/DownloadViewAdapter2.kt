package com.ezt.video.instasaver.screen.home.download.adapter

import android.content.Context
import android.content.Intent
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

class DownloadViewAdapter2() : RecyclerView.Adapter<DownloadViewAdapter2.DownloadViewHolder>(){
    private val allPosts = mutableListOf<Post>()
    private lateinit var context: Context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DownloadViewHolder {
        context = parent.context
        val binding = ItemDownloadViewBinding.inflate(LayoutInflater.from(context), parent, false)
        return DownloadViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: DownloadViewHolder,
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

    inner class DownloadViewHolder(private val binding: ItemDownloadViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val post = allPosts[position]
            binding.apply {
                val mediaType=post.media_type
                when (mediaType){
                    8 -> mediaTypeIconView.setImageResource(R.drawable.ic_copy)
                    2 -> mediaTypeIconView.setImageResource(R.drawable.ic_play)
                    else -> mediaTypeIconView.visibility= View.GONE
                }

                Glide.with(context).load(post.image_url).into(imageView)
                Glide.with(context).load(post.profile_pic_url).into(profilePicView)

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