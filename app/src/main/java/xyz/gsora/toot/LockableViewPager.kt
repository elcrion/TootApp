package xyz.gsora.toot

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * Created by gsora on 4/30/17.
 *
 *
 * A ViewPager that doesn't scroll.
 * Credits: https://gist.github.com/nesquena/898db22a38747bd9bc19#file-lockableviewpager-java
 */
class LockableViewPager : ViewPager {

    private var swipeable: Boolean = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.swipeable = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return this.swipeable && super.onTouchEvent(event)

    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return this.swipeable && super.onInterceptTouchEvent(event)

    }

    fun setSwipeable(swipeable: Boolean) {
        this.swipeable = swipeable
    }
}