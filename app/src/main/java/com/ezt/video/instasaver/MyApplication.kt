package com.ezt.video.instasaver

import android.app.Activity
import android.app.Application
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import com.ezt.video.instasaver.utils.Common
import com.ezt.video.instasaver.utils.Constants.AVATAR_FOLDER_NAME
import com.ezt.video.instasaver.utils.Constants.IMAGE_FOLDER_NAME
import com.ezt.video.instasaver.utils.Constants.STORY_FOLDER_NAME
import com.ezt.video.instasaver.utils.Constants.VIDEO_FOLDER_NAME
import com.ezt.video.instasaver.utils.FileUtils
import com.google.android.material.internal.ContextUtils
import com.squareup.picasso.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class MyApplication : Application(), Application.ActivityLifecycleCallbacks {
    var currentActivity: Activity? = null
        private set

    private var isActivityChangingConfigurations = false
    private var activityReferences = 0
    private var isInBackground = true
    private var foregroundHandled = false // ✅ NEW
    private val activityStartTimes = mutableMapOf<String, Long>()

    override fun onCreate() {
        super.onCreate()
        VIDEO_FOLDER_NAME = FileUtils.getDefaultVideoPath(this)
        IMAGE_FOLDER_NAME = FileUtils.getDefaultImagePath(this)
        STORY_FOLDER_NAME = FileUtils.getDefaultStoryPath(this)
        AVATAR_FOLDER_NAME = FileUtils.getDefaultAvatarUserPath(this)

        instance = this
        val sharedPreferences =  PreferenceManager.getDefaultSharedPreferences(this@MyApplication)
        applicationScope = CoroutineScope(SupervisorJob())
        applicationScope.launch((Dispatchers.IO)) {
            try {
                //init js interp server
                //val jsServer = YTDLPWebview(this@MyApplication, 65953)

                val appVer = sharedPreferences.getString("version", "")!!
                if(appVer.isEmpty() || appVer != BuildConfig.VERSION_NAME){
                    sharedPreferences.edit(commit = true){
                        putString("version", BuildConfig.VERSION_NAME)
                    }
                }
            }catch (e: Exception){
                Looper.prepare().runCatching {
                    Toast.makeText(this@MyApplication, e.message, Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }

        registerActivityLifecycleCallbacks(this)
    }


    private val appStateListeners = mutableListOf<AppStateListener>()


    fun registerAppStateListener(listener: AppStateListener) {
        if (!appStateListeners.contains(listener)) {
            appStateListeners.add(listener)
        }
    }

    fun unregisterAppStateListener(listener:AppStateListener) {
        appStateListeners.remove(listener)
    }

    fun onAppForegrounded() {
        if (foregroundHandled) return // ✅ Prevent duplicate triggers
        foregroundHandled = true

        for (listener in appStateListeners) {
            listener.onAppReturnedToForeground()
        }
    }

    fun onAppBackgrounded() {
        foregroundHandled = false // ✅ Reset on background
        for (listener in appStateListeners) {
            listener.onAppWentToBackground()
        }
    }


    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?
    ) {
        val lang = Common.getPreLanguage(this)
        println("onActivityCreated: $lang")
        Common.setLocale(this, lang)
    }

    override fun onActivityStarted(activity: Activity) {
        activityReferences++

        println("onActivityStarted: $activityReferences and $isActivityChangingConfigurations")
        if (activityReferences == 1 && !isActivityChangingConfigurations) {
            // App enters foreground
            onAppForegrounded()
        }
    }

    override fun onActivityResumed(activity: Activity) {
        val name = activity::class.java.simpleName
        activityStartTimes[name] = System.currentTimeMillis()

        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }

    override fun onActivityStopped(activity: Activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations
        activityReferences--
        if (activityReferences == 0 && !isActivityChangingConfigurations) {
            // App enters background
            onAppBackgrounded()
        }
    }

    override fun onActivitySaveInstanceState(
        activity: Activity,
        outState: Bundle
    ) {
        //do nothing
    }

    override fun onActivityDestroyed(activity: Activity) {
        val startTime = activityStartTimes.remove(screenName)

//        if (startTime != null) {
//            val mode = analyticsLogger.getCurrentModeByScreen(screenName)
//            analyticsLogger.updateUserProperties(this, screenName, mode)
//
//            val durationMs = System.currentTimeMillis() - startTime
//            Log.d("onActivityDestroyed", "$screenName was active for ${durationMs}ms")
//            // Optionally, send to Firebase:
//            analyticsLogger.logScreenExit(screenName, durationMs)
//        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            TRIM_MEMORY_UI_HIDDEN -> {
                Log.w("MyApplication", "UI is hidden. Level: $level")
            }
            TRIM_MEMORY_COMPLETE -> {
                // System is critically low on memory.
                // This is a big warning — clear large caches or save important data here.
                Log.e("MyApplication", "Severe memory pressure. System may kill the app.")
                isUnderMemory = true
            }
            else -> {
                // System is low on memory, and might start killing background processes.
                Log.w("MyApplication", "Memory is low. Level: $level")
                isUnderMemory = true
            }
        }
    }

    companion object {
        var screenName = ""
        private const val TAG = "MyApplication"
//        lateinit var mFirebaseAnalytics: FirebaseAnalytics
        private lateinit var applicationScope: CoroutineScope
        lateinit var instance: MyApplication

        var isUnderMemory = false

        @JvmStatic
        fun initROAS(revenue: Long, currency: String) {
            try {
                val sharedPref = android.preference.PreferenceManager.getDefaultSharedPreferences(instance)
                val editor: SharedPreferences.Editor = sharedPref.edit()
                val currentImpressionRevenue = revenue / 1000000
                // make sure to divide by 10^6
                val previousTroasCache: Float = sharedPref.getFloat(
                    "TroasCache",
                    0F
                ) //Use App Local storage to store cache of tROAS
                val currentTroasCache = (previousTroasCache + currentImpressionRevenue).toFloat()
                //check whether to trigger  tROAS event
                if (currentTroasCache >= 0.01) {
                    logTroasFirebaseAdRevenueEvent(currentTroasCache, currency)
                    editor.putFloat("TroasCache", 0f) //reset TroasCache
                } else {
                    editor.putFloat("TroasCache", currentTroasCache)
                }
                editor.apply()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        private fun logTroasFirebaseAdRevenueEvent(tRoasCache: Float, currency: String) {
//            try {
//                val bundle = Bundle()
//                bundle.putDouble(
//                    FirebaseAnalytics.Param.VALUE,
//                    tRoasCache.toDouble()
//                ) //(Required)tROAS event must include Double Value
//                bundle.putString(
//                    FirebaseAnalytics.Param.CURRENCY,
//                    currency
//                ) //put in the correct currency
//                mFirebaseAnalytics.logEvent("Daily_Ads_Revenue", bundle)
//            } catch (e: java.lang.Exception) {
//                e.printStackTrace()
//            }
        }


//        @JvmStatic
//        fun trackingEvent(event: String) {
//            try {
//                val params = Bundle()
//                mFirebaseAnalytics.logEvent(event, params)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
    }
}


interface AppStateListener {
    fun onAppReturnedToForeground()
    fun onAppWentToBackground()
}
