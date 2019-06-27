package xyz.gsora.toot

import MastodonTypes.Notification
import MastodonTypes.Status
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import es.dmoral.toasty.Toasty
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.Sort
import retrofit2.Response
import xyz.gsora.toot.Mastodon.LinkParser
import xyz.gsora.toot.Mastodon.Mastodon
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [Timeline.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [Timeline.newInstance] factory method to
 * create an instance of this fragment.
 */
class Timeline : Fragment() {
    @BindView(R.id.statuses_list)
    lateinit var statusList: RecyclerView
    @BindView(R.id.userTimelineRefresh)
    lateinit var refresh: SwipeRefreshLayout
    private val mListener: OnFragmentInteractionListener? = null
    private var pastVisiblesItems: Int = 0
    private var visibleItemCount: Int = 0
    private var totalItemCount: Int = 0
    private var calls = 0
    private var llm: LinearLayoutManager? = null
    private var realm: Realm? = null
    private var m: Mastodon? = null
    private var nextPage: String? = null
    private var loading: Boolean? = true
    private var selectedTimeline: TimelineContent? = null
    private val firstLoad: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            selectedTimeline = arguments.getSerializable("selectedTimeline") as TimelineContent
            realm = RealmBuilder.getRealmForTimelineContent(selectedTimeline!!)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_main_timeline, container, false)
        ButterKnife.bind(this, view)
        m = Mastodon.instance
        // pull data as soon as we can
        pullData(false)
        return view
    }

    override fun onViewCreated(view: View?, savedInstance: Bundle?) {
        nextPage = null
        val systemLocale = Locale.getDefault().language
        setUpRecyclerView(systemLocale)


        // setup the refresh listener
        setupRefreshListener()

        if (BuildConfig.DEBUG) {
            Log.d(TAG, Toot.debugSettingsStorage())
        }
    }




    private fun setUpRecyclerView(locale: String) {
        if (selectedTimeline != TimelineContent.NOTIFICATIONS) {
            val statuses = realm!!.where(Status::class.java).findAllSorted("id", Sort.DESCENDING)
            val adapter = selectedTimeline?.let { StatusesListAdapter(statuses, locale, activity, it) }
            llm = LinearLayoutManager(activity)
            statusList.layoutManager = llm
            statusList.adapter = adapter
            statusList.setHasFixedSize(false)
        } else {
            val notifications = realm!!.where(Notification::class.java).findAllSorted("id", Sort.DESCENDING)
            val adapter = NotificationListAdapter(notifications, locale, activity)
            llm = LinearLayoutManager(activity)
            statusList.layoutManager = llm
            statusList.adapter = adapter
            statusList.setHasFixedSize(false)
        }




        statusList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override// TODO: replace me
            fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (dy > 0)
                //check for scroll down
                {
                    visibleItemCount = llm!!.childCount
                    totalItemCount = llm!!.itemCount
                    pastVisiblesItems = llm!!.findFirstVisibleItemPosition()

                    if (loading!!) {
                        if (visibleItemCount + pastVisiblesItems >= totalItemCount) {
                            loading = false
                            pullData(true)
                        }
                    }
                }
            }
        })
    }

    // TODO: remove me in favor of proper favorites and notifications
    private fun doodad(page: Boolean?): Observable<Response<Array<Status>>> {
        val statuses: Observable<Response<Array<Status>>>
        if (nextPage == null || page == false) {
            statuses = m!!.homeTimeline
        } else {
            statuses = m!!.getHomeTimeline(nextPage!!)
        }

        return statuses
    }

    private fun gimmeStatusObservable(page: Boolean?): Observable<Response<Array<Status>>>? {
        var statuses: Observable<Response<Array<Status>>>? = null
        when (selectedTimeline) {
            TimelineContent.TIMELINE_MAIN -> statuses = doodad(page)
            TimelineContent.TIMELINE_LOCAL -> statuses = if (nextPage == null || (page == false)) {
                m!!.localTimeline
            } else {
                m!!.getLocalTimeline(nextPage!!)
            }
            TimelineContent.TIMELINE_FEDERATED -> statuses = if (nextPage == null || ( page == false)) {
                m!!.publicTimeline
            } else {
                m!!.getPublicTimeline(nextPage!!)
            }
            TimelineContent.FAVORITES -> statuses = if (nextPage == null || ( page == false)) {
                m!!.favorites
            } else {
                m!!.getFavorites(nextPage!!)
            }
            TimelineContent.LIST -> statuses = if (nextPage == null || ( page == false)) {
                m!!.list
            } else {
                m!!.getList(nextPage!!)
            }


        }

        return statuses
    }

    private fun pullData(page: Boolean?) {
        refresh.isRefreshing = true

        if (selectedTimeline != TimelineContent.NOTIFICATIONS) {
            val statuses = gimmeStatusObservable(page)
            statuses!!
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            Consumer<Response<Array<Status>>> { this.updateData(it) },
                            Consumer<Throwable> { this.updateDataError(it) }
                    )
        } else {
            val notifications: Observable<Response<Array<Notification>>>
            if (nextPage == null || ( page == false)) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "no previous page, loading the first one")
                }
                notifications = m!!.notifications
            } else {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "got previous page, loading it!")
                }
                notifications = m!!.getNotifications(nextPage!!)
            }

            notifications
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            Consumer<Response<Array<Notification>>> { this.updateNotificationsData(it) },
                            Consumer<Throwable> { this.updateDataError(it) }
                    )
        }
    }

    private fun updateDataError(error: Throwable) {
        Log.d(TAG, error.toString())
        refresh.isRefreshing = false
        Toasty.error(context, "Something went wrong :(\n$error", Toast.LENGTH_SHORT, true).show()
        loading = true
    }

    private fun updateData(statuses: Response<Array<Status>>) {
        if (statuses.body().isNotEmpty()) { // check if there are statuses first
            debugCallNums()
            realm!!.executeTransaction { r: Realm ->
                for (s in statuses.body()) {
                    if (s.reblog == null) {
                        s.setThisIsABoost(false)
                    } else {
                        s.setThisIsABoost(true)
                    }
                    r.insertOrUpdate(s)
                }
            }

            val links = statuses.headers().get("Link")
            val next = LinkParser.parseNext(links)
            nextPage = next.url
        }
        refresh.isRefreshing = false
        loading = true
    }

    private fun updateNotificationsData(notifications: Response<Array<Notification>>) {
        if (notifications.body().isNotEmpty()) { // check if there are statuses first
            debugCallNums()
            realm!!.executeTransaction { r: Realm ->
                for (n in notifications.body()) {
                    if (realm!!.where(Notification::class.java).equalTo("id", n.id).count() <= 0) {
                        r.insertOrUpdate(n)
                    }
                }
            }

            val links = notifications.headers().get("Link")
            val next = LinkParser.parseNext(links)
            nextPage = next.url
        }
        refresh.isRefreshing = false
        loading = true
    }

    private fun debugCallNums() {
        Log.d(TAG, "number of calls: " + ++calls)
    }

    private fun setupRefreshListener() {
        refresh.setOnRefreshListener { pullData(false) }
    }

    enum class TimelineContent {
        TIMELINE_MAIN,
        TIMELINE_LOCAL,
        TIMELINE_FEDERATED,
        FAVORITES,
        NOTIFICATIONS,
        LIST,
        HASHCODE
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {

        val TIMELINE_MAIN = "Timeline"
        val TIMELINE_LOCAL = "Local timeline"
        val TIMELINE_FEDERATED = "Federated timeline"
        val NOTIFICATIONS = "Notifications"
        val FAVORITES = "Favorites"
        val Hashcode = "HASHCODE"
        val List = "List"
        private val TAG = TimelineFragmentContainer::class.java.simpleName

        internal fun newInstance(timelineContent: TimelineContent): Timeline {
            val fragment = Timeline()
            val bundle = Bundle()

            bundle.putSerializable("selectedTimeline", timelineContent)
            fragment.arguments = bundle
            return fragment
        }
    }
}
