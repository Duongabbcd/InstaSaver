package com.ezt.video.instasaver.screen.download

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezt.video.instasaver.base.BaseActivity
import com.ezt.video.instasaver.databinding.ActivityDownloadStoryBinding
import com.ezt.video.instasaver.model.Story
import com.ezt.video.instasaver.R
import com.ezt.video.instasaver.screen.download.adapter.StoryHighlightViewAdapter
import com.ezt.video.instasaver.screen.download.adapter.StoryViewAdapter
import com.ezt.video.instasaver.viewmodel.StoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DownloadStoryActivity :
    BaseActivity<ActivityDownloadStoryBinding>(ActivityDownloadStoryBinding::inflate) {
    private val storyViewModel: StoryViewModel by viewModels()
    private lateinit var cookies: String
    private lateinit var username: String
    private lateinit var adapter: StoryViewAdapter
    private var selecting: Boolean = false
    private lateinit var stories: List<Story>
    private lateinit var onComplete: BroadcastReceiver
    private val downloadIds: MutableList<Long> = mutableListOf()
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        init()
        observeData(this)
        setOnClickListeners()
        onComplete= object : BroadcastReceiver() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                downloadIds.remove(id)
                if(downloadIds.isEmpty()  ){
                    adapter.isEnabled=false
                    val selectedStories: List<Int> = adapter.selectedStories
                    for(position in selectedStories){
                        adapter.dataHolder[position].downloaded=true
                    }
                    selecting=false
                    adapter.reset()
                    adapter.notifyDataSetChanged()
                    binding.downloadButton.visibility= View.GONE
                    Toast.makeText(context,resources.getString(R.string.download_complete), Toast.LENGTH_SHORT).show()
                }

            }
        }

//        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

    }

    private fun init(){
        cookies=intent.getStringExtra("cookie") ?: ""
        username=intent.getStringExtra("username") ?: ""
        val showHighlights=intent.getBooleanExtra("showHighlights",true)
        val userId=intent.getLongExtra("userId",0)
        binding.usernameView.text=username
        binding.progressBar.visibility=View.VISIBLE
        if(showHighlights) {
            storyViewModel.fetchStoryHighlights(userId, cookies)
            storyViewModel.fetchStory(userId,cookies)
        }else{
            val highlightId=intent.getStringExtra("highlightId")?:""
            storyViewModel.fetchStoryHighlightsStories(highlightId,cookies)
        }
        binding.downloadView.layoutManager= GridLayoutManager(this,3)
        binding.storyHighlightView.layoutManager= LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intent: Intent? = result.data
                val downloaded=intent?.getBooleanExtra("downloaded",true) ?: true
                val position=intent?.getIntExtra("position",-1) ?: -1
                if(downloaded && position!=-1){
                    adapter.dataHolder[position].downloaded=true
                    adapter.notifyItemChanged(position)
                }
            }
        }
    }

    private fun observeData(context: Context?){
        storyViewModel.stories.observe(this){
            adapter=StoryViewAdapter(this@DownloadStoryActivity, it,resultLauncher){
                showDownload()
            }
            binding.downloadView.adapter=adapter
            stories=it
            binding.progressBar.visibility=View.GONE
//            if(it.isEmpty()){
//                if(binding.noStoriesFoundViewStub.parent!=null){
//                    binding.noStoriesFoundViewStub.inflate()
//                }else{
//                    binding.noStoriesFoundViewStub.visibility=View.VISIBLE
//                }
//            }else{
//                binding.noStoriesFoundViewStub.visibility=View.GONE
//            }

        }

        storyViewModel.storyHighlights.observe(this){
            binding.storyHighlightView.adapter = StoryHighlightViewAdapter(this@DownloadStoryActivity,it,cookies)
        }

        storyViewModel.downloadId.observe(this){
            downloadIds.add(it)
        }

    }

    private fun setOnClickListeners() {
        binding.downloadButton.setOnClickListener {
            adapter.loading=true
            val selectedStories: List<Int> = adapter.selectedStories
            for(position in selectedStories){
                adapter.notifyItemChanged(position)
                storyViewModel.downloadStory(stories[position])
            }
        }

        binding.backButton.setOnClickListener{
            finish()

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showDownload(){
        adapter.notifyDataSetChanged()
        selecting=true
        binding.downloadButton.visibility=View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onComplete)
    }
}