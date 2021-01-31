package io.github.fvrodas.jaml.ui

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.databinding.ActivityMainBinding
import io.github.fvrodas.jaml.ui.fragments.AppsFragment
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>
    private val fragment = AppsFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.activity_main, null, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root
        ) { _, insets ->
            val statusBar = insets?.stableInsetTop ?: 0
            val navBar = insets?.stableInsetBottom ?: 0

            binding.root.setPadding(0, statusBar, 0, navBar)

            insets
        }

        setContentView(binding.root)

        initBottomSheet()
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
        showBottomSheet(show = false)
    }
}