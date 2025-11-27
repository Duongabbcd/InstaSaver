package com.ezt.video.instasaver.screen.login

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.ezt.video.instasaver.base.BaseActivity
import com.ezt.video.instasaver.databinding.ActivityRequestLoginBinding
import com.ezt.video.instasaver.screen.home.MainActivity

class RequestLoginActivity : BaseActivity<ActivityRequestLoginBinding>(ActivityRequestLoginBinding::inflate){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            loginButton.setOnClickListener{
                startActivity(Intent(this@RequestLoginActivity,InstagramLoginActivity::class.java))
                finish()
            }
            skip.setOnClickListener{
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clipData: ClipData = ClipData.newPlainText("link","")
                clipboard.setPrimaryClip(clipData)

                startActivity(Intent(this@RequestLoginActivity,MainActivity::class.java))
                finish()

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }

    override fun onPause() {
        super.onPause()
        finish()
    }

}