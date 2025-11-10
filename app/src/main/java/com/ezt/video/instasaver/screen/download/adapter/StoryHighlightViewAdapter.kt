package com.ezt.video.instasaver.screen.download.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ezt.video.instasaver.databinding.ItemReelRayBinding
import com.ezt.video.instasaver.model.StoryHighlight
import com.ezt.video.instasaver.screen.download.DownloadStoryActivity
import com.squareup.picasso.Picasso

class StoryHighlightViewAdapter(private val context:Context, private val dataHolder: List<StoryHighlight>, private val cookie: String) : RecyclerView.Adapter<StoryHighlightViewAdapter.StoryHighlightViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryHighlightViewHolder {
        val view: ItemReelRayBinding = ItemReelRayBinding.inflate( LayoutInflater.from(parent.context), parent, false)
        return StoryHighlightViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: StoryHighlightViewHolder,
        position: Int
    ) {
       holder.bind(position)
    }

    override fun getItemCount(): Int {
        return dataHolder.size
    }

    inner class StoryHighlightViewHolder(private val binding: ItemReelRayBinding) : RecyclerView.ViewHolder(binding.root){
       fun bind(position: Int) {
           val tray = dataHolder[position]
           binding.apply {
               Picasso.get().load(tray.cover_media.cropped_image_version.url).into(profilePicView)
               usernameView.text = tray.title
               root.setOnClickListener {
                   val intent = Intent(context, DownloadStoryActivity::class.java)
                   intent.putExtra("highlightId", tray.id)
                   intent.putExtra("cookie", cookie)
                   intent.putExtra("showHighlights", false)
                   intent.putExtra("username", tray.title)
                   context.startActivity(intent)
               }
           }
       }
    }



}