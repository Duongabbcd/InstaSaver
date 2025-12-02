package com.ezt.video.instasaver.screen.view.post.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezt.video.instasaver.R
import com.ezt.video.instasaver.model.CarouselMedia

class CarouselViewPagerAdapter(private val context: Context, private val dataHolder: List<CarouselMedia>) : RecyclerView.Adapter<CarouselViewPagerAdapter.PageHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageHolder {
        val inflater= LayoutInflater.from(parent.context)
        val view: View= inflater.inflate(R.layout.carousel_item_layout,parent,false)
        return PageHolder(view)
    }

    override fun onBindViewHolder(holder: PageHolder, position: Int) {
        val media= dataHolder[position]
        val mediaType= media.mediaType
        val uri= media.uri
        if(mediaType==1) {
            println("onBindViewHolder 1: $uri")
            holder.videoView.visibility=View.GONE
            holder.imageView.visibility=View.VISIBLE
            Glide.with(context).load(uri).into(holder.imageView)
        }else{
            println("onBindViewHolder 2: $uri")
            holder.imageView.visibility=View.GONE
            holder.videoView.visibility=View.VISIBLE
            holder.videoView.setMediaController(MediaController(context))
            holder.videoView.setVideoURI(uri)
            holder.videoView.start()
            holder.videoView.setOnCompletionListener {
                it.seekTo(0)
                it.start()
            }
        }
    }

    override fun getItemCount(): Int {
        return dataHolder.size
    }

    class PageHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val imageView:ImageView= itemView.findViewById(R.id.imageView)
        val videoView:VideoView= itemView.findViewById(R.id.videoView)
    }

}