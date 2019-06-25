package xyz.gsora.toot

import MastodonTypes.Notification
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import io.realm.RealmRecyclerViewAdapter
import io.realm.RealmResults
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by gsora on 5/1/17.
 *
 *
 * RecyclerView adapter for Notifications.
 */
class NotificationListAdapter internal constructor(data: RealmResults<Notification>, locale: String, private val parentCtx: Context) : RealmRecyclerViewAdapter<Notification, RowViewHolder>(data, true) {

    private val FAV = 0
    private val MENTION = 1
    private val BOOST = 2
    private val FOLLOW = 3

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder? {
        var viewHolder: RowViewHolder? = null
        val inflater = LayoutInflater.from(parent.context)

        when (viewType) {
            FAV -> {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onCreateViewHolder: type found: $FAV")
                }
                val toot = inflater.inflate(R.layout.notification_star, parent, false)
                viewHolder = RowViewHolder(toot, FAV, parentCtx, Timeline.TimelineContent.NOTIFICATIONS)
            }
            MENTION -> {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onCreateViewHolder: type found: $MENTION")
                }
                val tootBoost = inflater.inflate(R.layout.status_toot, parent, false)
                viewHolder = RowViewHolder(tootBoost, MENTION, parentCtx, Timeline.TimelineContent.NOTIFICATIONS)
            }
            BOOST -> {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onCreateViewHolder: type found: $BOOST")
                }
                val tootBoostCw = inflater.inflate(R.layout.notification_boost, parent, false)
                viewHolder = RowViewHolder(tootBoostCw, BOOST, parentCtx, Timeline.TimelineContent.NOTIFICATIONS)
            }
            FOLLOW -> {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onCreateViewHolder: type found: $FOLLOW")
                }
                val tootCw = inflater.inflate(R.layout.notification_follow, parent, false)
                viewHolder = RowViewHolder(tootCw, FOLLOW, parentCtx, Timeline.TimelineContent.NOTIFICATIONS)
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
        holder.dataNotification = getItem(holder.adapterPosition)
        val s = holder.dataNotification!!.status

        setStatusViewTo(holder.dataNotification!!, holder)
    }

    override fun getItemViewType(position: Int): Int {
        val toot = getItem(position)

        when (toot!!.type) {
            "mention" -> return MENTION
            "reblog" -> return BOOST
            "favourite" -> return FAV
            "follow" -> return FOLLOW
            else -> return MENTION
        }
    }

    private fun setStatusViewTo(n: Notification, holder: RowViewHolder) {

        var whoSendsTheNotification: String? = null

        if (n.account!!.displayName!!.length > 0) {
            whoSendsTheNotification = n.account!!.displayName
        } else {
            whoSendsTheNotification = n.account!!.username
        }

        // Standard setup: timestamp and avatar
        if (n.status == null) {
            Glide
                    .with(parentCtx)
                    .load(n.account!!.avatar)
                    .crossFade()
                    .into(holder.avatar)
        } else {
            Glide
                    .with(parentCtx)
                    .load(n.status!!.account!!.avatar)
                    .crossFade()
                    .into(holder.avatar)
        }

        // format the timestamp according to the device's setting
        val fmt: SimpleDateFormat
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Toot.getAppContext().resources.configuration.locales.get(0))
        } else {
            fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Toot.getAppContext().resources.configuration.locale)
        }

        fmt.timeZone = TimeZone.getTimeZone("UTC")

        var d: Date? = null
        try {
            d = fmt.parse(n.createdAt)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val dateFormat = android.text.format.DateFormat.getLongDateFormat(parentCtx)
        val timeFormat = android.text.format.DateFormat.getTimeFormat(parentCtx)

        val finalTimestamp: String
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time = d
        cal2.time = Date()
        val sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)

        if (!sameDay) {
            finalTimestamp = timeFormat.format(d) + " - " + dateFormat.format(d)
        } else {
            finalTimestamp = timeFormat.format(d)
        }

        holder.timestamp?.text = finalTimestamp

        if (n.type == "favourite" || n.type == "reblog") {
            val s = n.status
            if (s!!.account!!.displayName!!.length > 0) {
                holder.statusAuthor?.text = CoolHtml.html(s.account!!.displayName!!)
            } else {
                holder.statusAuthor?.text = CoolHtml.html(s.account!!.username!!)
            }
        } else {
            holder.statusAuthor?.text = whoSendsTheNotification
        }

        if (n.type == "favourite" || n.type == "reblog" || n.type == "mention") {
            holder.status.text = CoolHtml.html(n.status!!.content!!)
        }

        when (n.type) {
            "reblog" -> holder.boostedByNotification?.text = String.format(parentCtx.getString(R.string.boostedByNotification), whoSendsTheNotification)
            "favourite" -> holder.followedBy?.text = String.format(parentCtx.getString(R.string.favouritedBy), whoSendsTheNotification)
            "follow" -> holder.followedBy?.text = String.format(parentCtx.getString(R.string.followedBy), n.account!!.displayName)
        }


    }

    override fun getItemId(index: Int): Long {
        return getItem(index)!!.id!!
    }

    companion object {

        private val TAG = StatusesListAdapter::class.java.simpleName
    }
}
