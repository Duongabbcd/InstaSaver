package com.ezt.video.instasaver.screen.home.story.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ezt.video.instasaver.databinding.ItemReelRayBinding
import com.ezt.video.instasaver.model.ReelTray
import com.ezt.video.instasaver.screen.view.story.WatchStoriesActivity
import com.squareup.picasso.Picasso

class ReelTrayViewAdapter(private val dataHolder: List<ReelTray>, private val cookie: String) : RecyclerView.Adapter<ReelTrayViewAdapter.MyViewHolder>() {

    private lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        context = parent.context
        val view: ItemReelRayBinding = ItemReelRayBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(position)

    }

    override fun getItemCount(): Int {
        return dataHolder.size
    }

    inner class MyViewHolder(private val binding: ItemReelRayBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int) {
             val tray = dataHolder[position].user
            binding.apply {
                Picasso.get().load(tray.profile_pic_url).into(profilePicView)
                usernameView.text = tray.username
                root.setOnClickListener {
                    val intent = Intent(context, WatchStoriesActivity::class.java)
                    intent.putExtra("reelId", tray.pk)
                    intent.putExtra("cookie", cookie)
                    context.startActivity(intent)
                }

            }
        }
    }
    

}