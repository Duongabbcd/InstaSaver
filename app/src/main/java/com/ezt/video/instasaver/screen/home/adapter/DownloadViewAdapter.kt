package com.ezt.video.instasaver.screen.home.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezt.video.instasaver.R
import com.ezt.video.instasaver.databinding.ItemDownloadViewBinding
import com.ezt.video.instasaver.local.Post
import com.ezt.video.instasaver.screen.view.post.ViewPostActivity
import com.ezt.video.instasaver.utils.Common.gone
import com.ezt.video.instasaver.utils.Constants
import com.squareup.picasso.Picasso
import java.io.File

class DownloadViewAdapter(
    private val load: Boolean,
    private val allPosts: List<Post>,
    private val onDeleteItem: (Post) -> Unit
) :
    RecyclerView.Adapter<DownloadViewAdapter.DownloadViewHolder>() {
    private lateinit var context: Context
//    private var load: Boolean = false
//    private val allPosts = mutableListOf<Post>()

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

//    fun submitList(input: List<Post>, isLoaded : Boolean = false) {
//        load = isLoaded
//        allPosts.clear()
//        allPosts.addAll(input)
//        notifyDataSetChanged()
//    }

    inner class DownloadViewHolder(private val binding: ItemDownloadViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val layout: ConstraintLayout = itemView.findViewById(R.id.constraintLayout)
        val mediaTypeIconView: ImageView = itemView.findViewById(R.id.mediaTypeIconView)
        val loadingViewStub: ViewStub = itemView.findViewById(R.id.loadingViewStub)


        fun bind(position: Int) {
            val post = allPosts[position]
            binding.apply {
                val mediaType = post.media_type
                val postPath = post.path ?: Constants.STORY_FOLDER_NAME
                if (post.title.isNullOrEmpty()) {
                    return@apply
                }
                val input = File(postPath, post.title)
                val uri = Uri.fromFile(input)
                Glide.with(context).load(uri).into(imageView)

                when (mediaType) {
                    8 -> mediaTypeIconView.setImageResource(R.drawable.ic_copy)
                    2 -> mediaTypeIconView.setImageResource(R.drawable.ic_play)
                    else -> mediaTypeIconView.visibility = View.GONE
                }

                val avatarFile = File(Constants.AVATAR_FOLDER_NAME, post.username.plus(".jpg"))
                println("DownloadViewHolder file path 1 ${post.username} $avatarFile")
                println("DownloadViewHolder file path 2 ${avatarFile.exists()}")
                println("DownloadViewHolder file path 3 ${post.link}")
                if(avatarFile.exists()) {
                    Glide.with(context).load(Uri.fromFile(avatarFile)).into(profilePicView)
                }
                root.setOnClickListener {
                    val viewIntent = Intent(context, ViewPostActivity::class.java)
                    val postDetail = Bundle()
                    postDetail.putString("name", post.title)
                    postDetail.putInt("media_type", mediaType)
                    postDetail.putString("caption", post.caption)
                    postDetail.putString("username", post.username)
                    postDetail.putString("profilePicture", post.profile_pic_url)
                    postDetail.putString("instagram_url", post.link)
                    postDetail.putString("imageUrl", post.image_url)
                    postDetail.putString("videoUrl", post.video_url)
                    postDetail.putBoolean("isStory", post.isStory)
                    viewIntent.putExtras(postDetail)
                    context.startActivity(viewIntent)
                }

                iconDelete.setOnClickListener {
                    onDeleteItem(post)
                }

                usernameView.text = post.username
                captionView.text = post.caption
//                if (load && position == 0) {
//                    loadingViewStub.inflate()
//                    root.isClickable = false
//                    profilePicView.elevation = 0F
//                }
            }
        }

    }
}