package io.github.fvrodas.jaml.features.common

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class FragmentLifecycleObserver(val initFragment: () -> Unit) :
    DefaultLifecycleObserver {
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        owner.lifecycle.removeObserver(this)
        initFragment()
    }

}