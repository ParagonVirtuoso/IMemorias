package com.github.ParagonVirtuoso.memorias

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.github.ParagonVirtuoso.memorias.databinding.ActivityMainBinding
import com.github.ParagonVirtuoso.memorias.util.ThemePreferences
import com.github.ParagonVirtuoso.memorias.worker.MemoryNotificationWorker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    @Inject
    lateinit var themePreferences: ThemePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        themePreferences.applyTheme(themePreferences.isDarkMode())
        setupViewBinding()
        setupNavigation()
        
        if (savedInstanceState == null) {
            handleIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun setupViewBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.homeFragment) {
                setIntent(null)
            }
        }
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            val videoId = it.getStringExtra(MemoryNotificationWorker.KEY_VIDEO_ID)
            val videoTitle = it.getStringExtra(MemoryNotificationWorker.KEY_VIDEO_TITLE)
            val videoThumbnail = it.getStringExtra(MemoryNotificationWorker.KEY_VIDEO_THUMBNAIL)
            val fromNotification = it.getBooleanExtra("from_notification", false)

            if (videoId != null && videoTitle != null && videoThumbnail != null && fromNotification) {
                binding.root.postDelayed({
                    try {
                        navController.navigate(
                            NavGraphDirections.actionGlobalVideoDetails(
                                videoId = videoId,
                                videoTitle = videoTitle,
                                videoThumbnail = videoThumbnail,
                                fromNotification = true
                            )
                        )
                    } catch (e: Exception) {
                        navController.navigate(R.id.action_global_home)
                    }
                }, 100)
            }
        }
    }
}