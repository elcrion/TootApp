package xyz.gsora.toot

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.util.AttributeSet
import android.view.View
import com.roughike.bottombar.BottomBar

/**
 * Created by gsora on 4/26/17.
 *
 * FAB behavior that will stick it to any BottomBar
 */
class FABBehavior(context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<FloatingActionButton>(context, attrs) {

    override fun layoutDependsOn(parent: CoordinatorLayout?, child: FloatingActionButton?, dependency: View?): Boolean {
        return dependency is BottomBar
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout?, child: FloatingActionButton?, dependency: View?): Boolean {
        val translationY = Math.min(0f, dependency!!.translationY - dependency.height)
        child!!.translationY = translationY
        return true
    }
}
