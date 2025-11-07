package com.ezt.video.instasaver.screen.home

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.ezt.video.instasaver.base.BaseActivity
import com.ezt.video.instasaver.databinding.ActivityMainBinding
import com.ezt.video.instasaver.R
import com.ezt.video.instasaver.screen.home.fragment.HomeFragment
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate),HomeFragment.DownloadNavigation{
    private var readPermission= false
    private var writePermission= false
    private lateinit var permissionsLauncher:ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root) // <-- You missed this
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        binding.apply {
            setupWithNavController(binding.activityMainBottomNavigationView, navController)
        }

        onSharedIntent()
        permissionsLauncher= registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->
            readPermission= permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermission
            readPermission= permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: writePermission
        }
        updateOrRequestPermissions()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }


    private fun updateOrRequestPermissions(){
        val hasReadPermission= ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val minSdk29= Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        readPermission= hasReadPermission
        writePermission= hasWritePermission || minSdk29
        val permissionToRequest= mutableListOf<String>()
        if(!writePermission){
            permissionToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if(!readPermission){
            permissionToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if(permissionToRequest.isNotEmpty()){
            permissionsLauncher.launch(permissionToRequest.toTypedArray())
        }


    }

    private fun onSharedIntent(){
        val intent:Intent= intent
        if(intent.action.equals(Intent.ACTION_SEND)){
            val receivedLink= intent.getStringExtra(Intent.EXTRA_TEXT)
            if(receivedLink!=null){
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData:ClipData= ClipData.newPlainText("link",receivedLink)
                clipboard.setPrimaryClip(clipData)
            }
        }
    }

    override fun navigateToDownload() {
        binding.activityMainBottomNavigationView.selectedItemId= R.id.downloads
    }


    companion object {
        var isChangeTheme = false
    }
}