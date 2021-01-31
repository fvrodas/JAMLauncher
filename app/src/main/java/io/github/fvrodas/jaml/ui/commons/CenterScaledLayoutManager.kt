package io.github.fvrodas.jaml.ui.commons

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.min

class CenterScaledLayoutManager(context: Context?, orientation: Int, reverseLayout: Boolean) :
    LinearLayoutManager(context, orientation, reverseLayout) {

    override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
        lp?.let {
            it.height = height / 9
        }
        return true
    }

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        if (orientation == VERTICAL) {
            val scrolled = super.scrollVerticallyBy(dy, recycler, state)
            scaleCenterItem()
            return scrolled
        }
        return 0
    }

    override fun onLayoutCompleted(state: RecyclerView.State?) {
        super.onLayoutCompleted(state)
        scaleCenterItem()
    }

    private fun scaleCenterItem() {
        val screenMid = height / 2
        val d1  = 0.9f * screenMid
        for (i in 0..childCount) {
            val child = getChildAt(i) as ViewGroup?
            child?.let {
                val childMid = (getDecoratedTop(it) + getDecoratedBottom(it)) / 2f
                val d = min(d1, abs(screenMid - childMid))
                val scale =  1-0.3f * d/d1
                it.getChildAt(0).scaleX = scale
                it.getChildAt(0).scaleY = scale
                it.getChildAt(0).alpha = scale
            }
        }

    }
}