package io.github.fvrodas.jaml.features.launcher.presentation.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.databinding.ActivityMainBinding
import io.github.fvrodas.jaml.framework.services.JAMLNotificationService
import io.github.fvrodas.jaml.features.launcher.presentation.fragments.FragmentApps
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>
    private lateinit var notificationReceiver: NotificationReceiver
    private val fragment = FragmentApps.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.activity_main, null, false)
        ViewCompat.setOnApplyWindowInsetsListener(
            binding.root
        ) { _, insets ->
            val barsInsets = insets.getInsets(systemBars())

            val statusBar = barsInsets.top
            val navBar = barsInsets.bottom

            binding.root.setPadding(0, statusBar, 0, navBar)

            insets
        }
        setContentView(binding.root)
        initBottomSheet()
        notificationReceiver = NotificationReceiver(fragment)
        registerReceiver(notificationReceiver, NotificationReceiver.provideIntentFilter())
    }

    override fun onResume() {
        super.onResume()
        val calendar = Calendar.getInstance()
        val dateFormatter = SimpleDateFormat("E, MMMM dd yyyy", Locale.getDefault())
        binding.dateTextView.text = dateFormatter.format(calendar.time)
    }

    private fun initBottomSheet() {

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.bottomSheet, fragment)
            .commit()

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet).apply {
            peekHeight = resources.getDimensionPixelSize(R.dimen.peekHeight)

            addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    fragment.changeArrowState(newState)
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {

                }
            })
        }
    }

    fun showBottomSheet(show: Boolean = true) {
        if (show && bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        } else if (!show && bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            showBottomSheet(show = false)
        }
    }

    override fun onDestroy() {
        unregisterReceiver(notificationReceiver)
        super.onDestroy()
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