package io.github.fvrodas.jaml.ui.fragments

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.view.ContextThemeWrapper
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.databinding.FragmentShortcutsBinding
import io.github.fvrodas.jaml.model.AppInfo
import io.github.fvrodas.jaml.model.AppShortcutInfo
import io.github.fvrodas.jaml.ui.MainActivity
import io.github.fvrodas.jaml.ui.fragments.adapters.AppShortcutInfoRecyclerAdapter
import io.github.fvrodas.jaml.viewmodel.JAMLViewModelFactory
import io.github.fvrodas.jaml.viewmodel.ShortcutsViewModel

@RequiresApi(Build.VERSION_CODES.N_MR1)
class ShortcutsFragment : Fragment() {

    private lateinit var binding: FragmentShortcutsBinding
    private lateinit var viewModel: ShortcutsViewModel
    private lateinit var viewModelFactory: JAMLViewModelFactory
    private lateinit var adapter: AppShortcutInfoRecyclerAdapter
    private lateinit var launcherApps: LauncherApps
    private lateinit var appInfo: AppInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            appInfo = getSerializable(ARG_PKG) as AppInfo
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_shortcuts, null, false)
        launcherApps =
            requireContext().getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        viewModelFactory =
            JAMLViewModelFactory(
                requireActivity().application,
                launcherApps,
                appInfo.packageName,
                -1,
                requireActivity().packageManager
            )
        val color = deviceAccentColor(requireContext())
        adapter = AppShortcutInfoRecyclerAdapter(color = color)
        adapter.onItemPressed = this::openApp
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = viewModelFactory.create(ShortcutsViewModel::class.java)
        binding.lifecycleOwner = this
        binding.appsRecyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.appsRecyclerView.adapter = adapter
        viewModel.shortcutsList.observe(viewLifecycleOwner, {
            adapter.updateDataSet(it)
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.retrieveShortcuts(appInfo.packageName)
    }

    private fun deviceAccentColor(context: Context): Int {
        val typedValue = TypedValue()
        val contextThemeWrapper = ContextThemeWrapper(
            context,
            android.R.style.Theme_DeviceDefault
        )
        contextThemeWrapper.theme.resolveAttribute(
            android.R.attr.colorAccent,
            typedValue, true
        )
        return typedValue.data
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun openApp(appShortcutInfo: AppShortcutInfo) {
        requireActivity().onBackPressed()
        if (appShortcutInfo.packageName == "none") {
            openAppInfo(appInfo)
        } else {
            Log.d("AppInfo", appShortcutInfo.packageName)
            viewModel.startShortcut(appShortcutInfo)
        }
    }

    private fun openAppInfo(appInfo: AppInfo) {
        adapter.filterDataSet("")
        (activity as MainActivity?)?.showBottomSheet(show = false)
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            data = Uri.parse("package:${appInfo.packageName}")
            activity?.startActivity(this)
        }
    }

    companion object {
        private const val ARG_PKG: String = "argument_package"

        @JvmStatic
        fun newInstance(appInfo: AppInfo) = ShortcutsFragment().apply {
            arguments = Bundle().apply {
                appInfo.icon = null
                putSerializable(ARG_PKG, appInfo)
            }
        }
    }
}