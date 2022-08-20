package io.github.fvrodas.jaml.features.launcher.presentation.activities

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.databinding.ActivityMainBinding
import io.github.fvrodas.jaml.features.common.ThemedActivity
import io.github.fvrodas.jaml.framework.services.JAMLNotificationService
import io.github.fvrodas.jaml.features.launcher.presentation.fragments.FragmentApps
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ThemedActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>
    private lateinit var notificationReceiver: NotificationReceiver
    private val fragment = FragmentApps.newInstance()

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        window.setBackgroundDrawable(ColorDrawable(android.R.color.transparent))

        binding = ActivityMainBinding.inflate(layoutInflater, null, false)
        ViewCompat.setOnApplyWindowInsetsListener(
            binding.root
        ) { _, insets ->
            insets.getInsets(systemBars()).apply {
                binding.root.setPadding(0, top, 0, bottom)
            }
            insets
        }
        setContentView(binding.root)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.bottomSheet, fragment)
            .commit()

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet).apply {
            peekHeight = resources.getDimensionPixelSize(R.dimen.peekHeight)

            addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    fragment.onBottomSheetStateChange(newState)
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {

                }
            })
        }

        notificationReceiver = NotificationReceiver(fragment)
        registerReceiver(notificationReceiver, NotificationReceiver.provideIntentFilter())
    }

    override fun onResume() {
        super.onResume()
        Calendar.getInstance().apply {
            val dateFormatter = SimpleDateFormat("E, MMMM dd yyyy", Locale.getDefault())
            binding.dateTextView.text = dateFormatter.format(time)
        }
    }

    override fun onPause() {
        super.onPause()
        showBottomSheet(false)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) supportFragmentManager.popBackStack()
        else showBottomSheet(show = false)
    }

    override fun onDestroy() {
        unregisterReceiver(notificationReceiver)
        super.onDestroy()
    }

    fun showBottomSheet(show: Boolean = true) {
        if (show && bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        } else if (!show && bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    companion object {
        interface INotificationEventListener {
            fun onNotificationEvent(packageName: String?, hasNotification: Boolean = false)
        }

        class NotificationReceiver(private val listener: INotificationEventListener? = null) :
            BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                try {
                    intent?.let {
                        Log.d("NOTIFICATION_EVENT", "${it.getStringExtra("package_name")}")
                        listener?.onNotificationEvent(
                            packageName = it.getStringExtra("package_name"),
                            hasNotification = intent.getBooleanExtra("has_notification", false)
                        )
                    }
                } catch (e: Exception) {
                    Log.d(MainActivity::class.java.name, "${e.message}")
                }
            }

            companion object {
                fun provideIntentFilter(): IntentFilter =
                    IntentFilter(JAMLNotificationService.NOTIFICATION_ACTION)
            }
        }
    }
}