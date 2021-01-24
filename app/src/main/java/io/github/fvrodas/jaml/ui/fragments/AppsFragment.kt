package io.github.fvrodas.jaml.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.databinding.FragmentAppsBinding
import io.github.fvrodas.jaml.model.AppInfo
import io.github.fvrodas.jaml.ui.commons.CenterScaledLayoutManager
import io.github.fvrodas.jaml.ui.fragments.adapters.AppInfoRecyclerAdapter
import io.github.fvrodas.jaml.viewmodel.AppsViewModel
import io.github.fvrodas.jaml.viewmodel.JAMLViewModelFactory





class AppsFragment : Fragment() {

    private lateinit var binding: FragmentAppsBinding
    private lateinit var viewModelFactory: JAMLViewModelFactory
    private lateinit var adapter: AppInfoRecyclerAdapter
    //private lateinit var snapHelper: PagerSnapHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_apps, null, false)
        viewModelFactory = JAMLViewModelFactory(activity!!.application, activity!!.packageManager)
        adapter = AppInfoRecyclerAdapter()
        adapter.onItemPressed = this::openApp
        adapter.onItemLongPressed = this::openAppInfo
        //snapHelper = PagerSnapHelper()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel = viewModelFactory.create(AppsViewModel::class.java)
        binding.lifecycleOwner = this
        binding.appsRecyclerView.layoutManager = CenterScaledLayoutManager(
            context,
            LinearLayoutManager.VERTICAL,
            false
        )
//        snapHelper.attachToRecyclerView(binding.appsRecyclerView)
        binding.appsRecyclerView.adapter = adapter
        viewModel.applicationsList.observe(viewLifecycleOwner, {
            adapter.updateDataSet(it)
        })
    }

    fun changeArrowState(bottomSheetState: Int) {
        if (bottomSheetState == BottomSheetBehavior.STATE_DRAGGING) {
            binding.arrowImageView.animate().rotation(-180f).setDuration(350).start()
        }
        if (bottomSheetState == BottomSheetBehavior.STATE_COLLAPSED) {
            binding.arrowImageView.animate().rotation(0f).setDuration(250).start()
        }
    }

    private fun openApp(appInfo: AppInfo) {
        activity?.packageManager?.getLaunchIntentForPackage(appInfo.packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            activity?.startActivity(this)
        }
    }

    private fun openAppInfo(appInfo: AppInfo) {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            data = Uri.parse("package:${appInfo.packageName}")
            activity?.startActivity(this)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = AppsFragment()
    }
}