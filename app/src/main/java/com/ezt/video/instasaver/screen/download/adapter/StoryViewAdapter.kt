package com.ezt.video.instasaver.screen.download.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import com.ezt.video.instasaver.R
import com.ezt.video.instasaver.databinding.ItemStoryBinding
import com.ezt.video.instasaver.model.Story
import com.ezt.video.instasaver.screen.view.story.ViewStoryActivity
import com.squareup.picasso.Picasso

class StoryViewAdapter(private val context:Context, val dataHolder: List<Story>, private val resultLauncher: ActivityResultLauncher<Intent>,private val showDownload: (Boolean) -> Unit) : RecyclerView.Adapter<StoryViewAdapter.StoryViewHolder>() {
    var isEnabled= false
    var loading=false
    var selectedStories= mutableListOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return StoryViewHolder(binding)
    }


    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(position)
    }


    override fun getItemCount(): Int {
        return dataHolder.size
    }

    inner class StoryViewHolder(private val binding : ItemStoryBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int) {
            val story = dataHolder[position]
            val picasso= Picasso.get()
            val mediaType=story.mediaType
            binding.apply {
                picasso.load(story.imageUrl).into(imageView)
                selector.setImageResource(R.drawable.story_not_selected)
                selector.visibility=if(isEnabled) View.VISIBLE else View.GONE

                if(story.downloaded){
                    selector.visibility=View.GONE
                }
                root.setOnClickListener {
                    if(isEnabled){
                        if(selectedStories.contains(position)){
                            selector.setImageResource(R.drawable.story_not_selected)
                            selectedStories.remove(position)
                            story.isSelected=false
                        }else{
                            selectedStories.add(position)
                            selector.setImageResource(R.drawable.story_selected)
                            story.isSelected=true
                        }
                    }else {
                        val viewIntent = Intent(context, ViewStoryActivity::class.java)
                        val storyDetail = Bundle()
                        storyDetail.putString("code",story.code)
                        storyDetail.putInt("media_type", mediaType)
                        storyDetail.putString("username", story.username)
                        storyDetail.putString("profilePicture", story.profilePicUrl)
                        storyDetail.putString("imageUrl", story.imageUrl)
                        storyDetail.putString("videoUrl", story.videoUrl)
                        storyDetail.putInt("position",position)
                        storyDetail.putBoolean("alreadyDownloaded",story.downloaded)
                        storyDetail.putString("name",story.name)
                        viewIntent.putExtras(storyDetail)
                        resultLauncher.launch(viewIntent)
                    }
                }
                if(loading){
                    loadingViewStub.inflate()
                    selector.visibility= View.GONE
                }else{
                    loadingViewStub.visibility=View.GONE
                }

                if(story.downloaded){
                    downloaded.visibility= View.VISIBLE
                }else{
                    downloaded.visibility=View.GONE
                }

                root.setOnLongClickListener {
                    isEnabled=!isEnabled
                    showDownload(true)
                    true
                }

            }
        }
    }

    fun reset(){
        isEnabled= false
        loading=false
        selectedStories.clear()
    }





}