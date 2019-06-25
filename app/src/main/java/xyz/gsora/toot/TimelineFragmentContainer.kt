package xyz.gsora.toot

import MastodonTypes.Account
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.View
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.roughike.bottombar.BottomBar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import xyz.gsora.toot.Mastodon.Mastodon
import xyz.gsora.toot.Mastodon.ToastMaker

class TimelineFragmentContainer : AppCompatActivity() {

    @BindView(R.id.newToot)
    lateinit var newTootFAB: FloatingActionButton
    @BindView(R.id.BottomNavigation)
    lateinit var bottomBar: BottomBar
    @BindView(R.id.viewPager)
    lateinit var viewPager: LockableViewPager

    private var viewPagerAdapter: TimelinesStatusAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline_fragment_container)
        ButterKnife.bind(this)

        // setup the bottom navigation bar
        setupBottomBar()

        // Setup FAB action
        newTootFAB.setOnClickListener { v: View ->
            val i = Intent(this@TimelineFragmentContainer, SendToot::class.java)
            startActivity(i)
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, Toot.debugSettingsStorage())
        }

        // setup the viewpager
        viewPagerAdapter = TimelinesStatusAdapter(supportFragmentManager)
        viewPager.adapter = viewPagerAdapter
        viewPager.setSwipeable(false)

        // set the base name
        title = Timeline.TIMELINE_MAIN

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> title = Timeline.TIMELINE_MAIN
                    1 -> title = Timeline.NOTIFICATIONS
                    2 -> title = Timeline.TIMELINE_LOCAL
                    3 -> title = Timeline.TIMELINE_FEDERATED
                    4 -> title = Timeline.FAVORITES
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

    }

    // add the settings button
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings_menu_timeline, menu)
        val toot_settings_button = menu.findItem(R.id.toot_settings_button)


        Mastodon.instance.loggedUserInfo
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        Consumer<Response<Account> > {
                            Glide.with(this)
                                    .load(it.body().avatar)
                                    .asBitmap()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .placeholder(R.drawable.missingavatar)
                                    .into<SimpleTarget<Bitmap>>(object : SimpleTarget<Bitmap>(65, 65) {

                                        override fun onResourceReady(resource: Bitmap?, glideAnimation: GlideAnimation<in Bitmap>?) {
                                            val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(this@TimelineFragmentContainer.resources, resource)
                                            circularBitmapDrawable.isCircular = true
                                            toot_settings_button.icon = circularBitmapDrawable
                                        }
                                    })
                        },
                        Consumer<Throwable> { this.errorAccountInfo(it) }
                )

        return super.onCreateOptionsMenu(menu)
    }

    private fun setupBottomBar() {
        bottomBar.setOnTabSelectListener { tabId: Int ->
            viewPager.currentItem = bottomBar.currentTabPosition
            when (tabId) {
                R.id.timeline -> viewPager.setCurrentItem(0, false)
                R.id.notifications -> viewPager.setCurrentItem(1, false)
                R.id.local -> viewPager.setCurrentItem(2, false)
                R.id.federated -> viewPager.setCurrentItem(3, false)
                R.id.favorites -> viewPager.setCurrentItem(4, false)
            }
        }
    }

    private fun errorAccountInfo(error: Throwable) {
        ToastMaker.buildToasty(this, error.toString())
    }

    companion object {

        private val TAG = TimelineFragmentContainer::class.java.simpleName
    }

}
