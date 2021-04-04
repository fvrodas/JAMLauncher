package io.github.fvrodas.jaml.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.databinding.FragmentAppsBinding
import io.github.fvrodas.jaml.model.AppInfo
import io.github.fvrodas.jaml.ui.MainActivity
import io.github.fvrodas.jaml.ui.SettingsActivity
import io.github.fvrodas.jaml.ui.fragments.adapters.AppInfoRecyclerAdapter
import io.github.fvrodas.jaml.viewmodel.AppsViewModel
import io.github.fvrodas.jaml.viewmodel.JAMLViewModelFactory


class AppsFragment : MainActivity.Companion.INotificationEventListener, Fragment() {

    private lateinit var binding: FragmentAppsBinding
    private lateinit var viewModel: AppsViewModel
    private lateinit var viewModelFactory: JAMLViewModelFactory
    private lateinit var adapter: AppInfoRecyclerAdapter
    private lateinit var vibrator: Vibrator

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_apps, null, false)
        viewModelFactory =
                JAMLViewModelFactory(
                        requireActivity().application,
                        null,
                        null,
                        -1,
                        requireActivity().packageManager
                )
        vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator;
        val color = deviceAccentColor(requireContext())
        adapter = AppInfoRecyclerAdapter(color = color)
        adapter.onItemPressed = this::openApp
        adapter.onItemLongPressed = this::openShortcutsList
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = viewModelFactory.create(AppsViewModel::class.java)
        binding.lifecycleOwner = this
        binding.appsRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.appsRecyclerView.adapter = adapter
        viewModel.filteredApplicationsList.observe(viewLifecycleOwner, {
            adapter.updateDataSet(it)
        })

        binding.appsSearchView.setOnClickListener {
            binding.appsSearchView.isIconified = false
            (activity as MainActivity?)?.showBottomSheet()
        }

        binding.appsSearchView.setOnQueryTextListener(object :
                androidx.appcompat.widget.SearchView.OnQueryTextListener {
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

        binding.appsSearchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                (activity as MainActivity?)?.showBottomSheet()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.retrieveApplicationsList()
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

    fun changeArrowState(bottomSheetState: Int) {
        if (bottomSheetState == BottomSheetBehavior.STATE_DRAGGING) {
            binding.arrowImageView.animate().rotation(-180f).setDuration(350).start()
        }
        if (bottomSheetState == BottomSheetBehavior.STATE_COLLAPSED) {
            binding.arrowImageView.animate().rotation(0f).setDuration(250).start()
            viewModel.filterApplicationsList("")
            binding.appsSearchView.isIconified = true
        }
    }

    private fun openApp(appInfo: AppInfo) {
        Log.d("AppInfo", appInfo.packageName)
        viewModel.filterApplicationsList("")
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

    private fun openShortcutsList(appInfo: AppInfo) {
        viewModel.filterApplicationsList("")
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(48, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(48)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            parentFragmentManager.beginTransaction()
                    .add(R.id.bottomSheet, ShortcutsFragment.newInstance(appInfo.packageName))
                    .addToBackStack(null)
                    .commit()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = AppsFragment()
    }

    override fun onNotificationEvent(packageName: String?, hasNotification: Boolean) {
        viewModel.markNotification(packageName, hasNotification)
    }
}