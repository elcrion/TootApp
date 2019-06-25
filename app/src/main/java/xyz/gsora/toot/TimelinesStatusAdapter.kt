package xyz.gsora.toot

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

/**
 * Created by gsora on 4/29/17.
 *
 * Fragment adapter for the various timeline types.
 */
class TimelinesStatusAdapter internal constructor(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment? {
        when (position) {
            0 -> return Timeline.newInstance(Timeline.TimelineContent.TIMELINE_MAIN)
            1 -> return Timeline.newInstance(Timeline.TimelineContent.NOTIFICATIONS)
            2 -> return Timeline.newInstance(Timeline.TimelineContent.TIMELINE_LOCAL)
            3 -> return Timeline.newInstance(Timeline.TimelineContent.TIMELINE_FEDERATED)
            4 -> return Timeline.newInstance(Timeline.TimelineContent.FAVORITES)
            else -> return null
        }
    }

    override fun getCount(): Int {
        return 5
    }
}
