package io.github.fvrodas.jaml.features.launcher.presentation.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.ListPopupWindow
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.github.fvrodas.jaml.databinding.FragmentAppsBinding
import io.github.fvrodas.jaml.core.domain.entities.AppInfo
import io.github.fvrodas.jaml.core.domain.entities.AppShortcutInfo
import io.github.fvrodas.jaml.features.common.FragmentLifecycleObserver
import io.github.fvrodas.jaml.features.launcher.presentation.activities.MainActivity
import io.github.fvrodas.jaml.features.settings.presentation.activities.SettingsActivity
import io.github.fvrodas.jaml.features.launcher.adapters.AppInfoRecyclerAdapter
import io.github.fvrodas.jaml.features.launcher.adapters.IAppInfoListener
import io.github.fvrodas.jaml.features.launcher.adapters.IShortcutListener
import io.github.fvrodas.jaml.features.launcher.adapters.ShortcutsAdapter
import io.github.fvrodas.jaml.features.launcher.presentation.viewmodels.ApplicationsListUiState
import io.github.fvrodas.jaml.features.launcher.presentation.viewmodels.AppsViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class FragmentApps : MainActivity.Companion.INotificationEventListener, Fragment() {

    private lateinit var binding: FragmentAppsBinding
    private lateinit var appInfoRecyclerAdapter: AppInfoRecyclerAdapter
    private lateinit var vibrator: Vibrator

    private val viewModel: AppsViewModel by viewModel()
    private lateinit var shortcutsPopupWindow: ListPopupWindow
    private var shortcutsAdapter: ShortcutsAdapter? = null

    @SuppressLint("ServiceCast")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAppsBinding.inflate(inflater)
        requireActivity().lifecycle.addObserver(FragmentLifecycleObserver {

            shortcutsPopupWindow = ListPopupWindow(requireContext())

            binding.appsSearchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    (requireActivity() as MainActivity).showBottomSheet()
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    requireContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibrator = vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            appInfoRecyclerAdapter =
                AppInfoRecyclerAdapter(listener = object : IAppInfoListener {
                    override fun onItemPressed(appInfo: AppInfo) {
                        this@FragmentApps.openApp(appInfo)
                    }

                    override fun onItemLongPressed(appInfo: AppInfo, viewAnchor: View): Boolean {
                        this@FragmentApps.openShortcutsList(appInfo, viewAnchor)
                        return true
                    }
                })
            binding.appsRecyclerView.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            binding.appsRecyclerView.adapter = appInfoRecyclerAdapter

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.appsUiState.collect { state ->
                        when (state) {
                            is ApplicationsListUiState.Success -> appInfoRecyclerAdapter.submitList(
                                state.apps
                            )
                            is ApplicationsListUiState.Failure -> Log.d("", state.errorMessage)
                        }
                    }
                }
            }

            binding.appsSearchView.setOnClickListener {
                binding.appsSearchView.isIconified = false
                (requireActivity() as MainActivity).showBottomSheet()
            }

            binding.appsSearchView.setOnQueryTextListener(object :
                OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let {
                        viewModel.filterApplicationsList(it)
                    }
                    return true
                }

            })

            binding.settingsImageButton.isEnabled = false
            binding.settingsImageButton.setOnClickListener {
                openApp(AppInfo(packageName = SettingsActivity::class.java.name, label = ""))
            }

            viewModel.retrieveApplicationsList()
        })
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.retrieveApplicationsList()
    }


    fun onBottomSheetStateChange(bottomSheetState: Int) {
        binding.appsSearchView.clearFocus()
        when (bottomSheetState) {
            BottomSheetBehavior.STATE_DRAGGING -> {
                binding.arrowImageView.animate()
                    .rotation(-180f).setDuration(350).start()
            }
            BottomSheetBehavior.STATE_COLLAPSED -> {
                binding.arrowImageView.animate().rotation(0f).setDuration(250).start()
                binding.settingsImageButton.isEnabled = false
                binding.settingsImageButton.animate().apply {
                    alpha(0.0f)
                    duration = 250
                    start()
                }
                binding.appsSearchView.isIconified = true
                binding.appsRecyclerView.layoutManager?.scrollToPosition(0)
            }
            BottomSheetBehavior.STATE_EXPANDED -> {
                binding.settingsImageButton.isEnabled = true
                binding.settingsImageButton.animate().apply {
                    alpha(1.0f)
                    duration = 350
                    start()
                }
            }
        }
    }

    private fun openApp(appInfo: AppInfo) {
        Log.d("AppInfo", appInfo.packageName)
        (activity as MainActivity?)?.showBottomSheet(show = false)
        if (appInfo.packageName == SettingsActivity::class.java.name) {
            Intent(context, SettingsActivity::class.java).apply {
                binding.appsSearchView.isIconified = true
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                activity?.startActivity(this)
            }
        } else {
            activity?.packageManager?.getLaunchIntentForPackage(appInfo.packageName)?.apply {
                binding.appsSearchView.isIconified = true
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                activity?.startActivity(this)
            }
        }
    }

    private fun openShortcutsList(appInfo: AppInfo, viewAnchor: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    VIBRATION_DURATION_MILLIS,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(VIBRATION_DURATION_MILLIS)
        }
        lifecycleScope.launch {
            viewModel.retrieveShortcuts(appInfo.packageName).collect { shortcuts ->

                shortcutsAdapter?.updateAdapter(shortcuts) ?: run {
                    shortcutsAdapter = ShortcutsAdapter(shortcuts, object : IShortcutListener {
                        override fun onShortcutPressed(shortcut: AppShortcutInfo) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                                if (shortcut.packageName == Settings.ACTION_APPLICATION_DETAILS_SETTINGS) {
                                    (requireActivity() as MainActivity).showBottomSheet(show = false)
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        addCategory(Intent.CATEGORY_DEFAULT)
                                        data = Uri.parse(shortcut.id)
                                        requireActivity().startActivity(this)
                                    }
                                } else {
                                    viewModel.startShortcut(shortcut)
                                }
                            }
                            shortcutsPopupWindow.dismiss()
                        }
                    })
                }

                shortcutsPopupWindow.apply {
                    width = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        200.0f,
                        resources.displayMetrics
                    ).toInt()
                    setAdapter(shortcutsAdapter)
                    anchorView = viewAnchor
                }.show()
            }
        }
    }

    companion object {
        const val VIBRATION_DURATION_MILLIS: Long = 48

        @JvmStatic
        fun newInstance() = FragmentApps()
    }

    override fun onNotificationEvent(packageName: String?, hasNotification: Boolean) {
        viewModel.markNotification(packageName, hasNotification)
        // Test if works as intended
        binding.appsRecyclerView.invalidate()
    }
}

