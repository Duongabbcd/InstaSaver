package com.ezt.video.instasaver.screen.view

import android.app.Dialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.ezt.video.instasaver.base.BaseActivity
import com.ezt.video.instasaver.R
import com.ezt.video.instasaver.databinding.ActivityViewDpBinding
import com.ezt.video.instasaver.local.Post
import com.ezt.video.instasaver.utils.Constants
import com.ezt.video.instasaver.viewmodel.DPViewerViewModel
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewDPActivity : BaseActivity<ActivityViewDpBinding>(ActivityViewDpBinding::inflate){
    private val dpViewerViewModel: DPViewerViewModel by viewModels()

    private lateinit var username:String
    private lateinit var profilePicUrl: String
    private var downloadId: Long=0
    private lateinit var loadingDialog: Dialog
    private lateinit var onComplete: BroadcastReceiver
    private var downloaded= false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        username = intent.getStringExtra("username") ?: ""
        val cookies = intent.getStringExtra("cookies") ?: ""
        val userId = intent.getLongExtra("userId", 0)
        binding.instaAce.text = username
        dpViewerViewModel.getDP(userId, cookies)
        dpViewerViewModel.profilePicUrl.observe(this) {
            profilePicUrl = it
            Picasso.get().load(profilePicUrl).into(binding.imageView, object : Callback {
                override fun onSuccess() {
                    binding.progressBar.visibility = View.GONE
                }

                override fun onError(e: Exception?) {
                    e?.printStackTrace()
                }

            })
        }

        binding.backButton.setOnClickListener{
            finish()
        }

        binding.downloadButton.setOnClickListener{
            if(downloaded){
                Toast.makeText(this,"Already Downloaded", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this,"Start download", Toast.LENGTH_SHORT).show()
                loadingDialog= Dialog(this)
                loadingDialog.setContentView(R.layout.download_loading_dialog)
                loadingDialog.setCancelable(false)
                loadingDialog.show()
                download()
                loadingDialog.dismiss()
            }
        }
    }

    fun download(){
        val uri: Uri = Uri.parse(profilePicUrl)
        val request: DownloadManager.Request = DownloadManager.Request(uri)
        val title=username+"_"+System.currentTimeMillis()+".jpg"
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
        request.setTitle(title)
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            Constants.IMAGE_FOLDER_NAME + title
        )
        downloadId=(getSystemService(DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
        dpViewerViewModel.insertDP(Post(0,1,username,profilePicUrl,profilePicUrl,null,"$username's DP",null,profilePicUrl,".jpg",title,null,false))
    }



}