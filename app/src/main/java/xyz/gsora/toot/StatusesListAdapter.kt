package xyz.gsora.toot

import MastodonTypes.MediaAttachment
import MastodonTypes.Status
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import io.realm.RealmList
import io.realm.RealmRecyclerViewAdapter
import io.realm.RealmResults
import xyz.gsora.toot.Mastodon.CustomTabsHelper
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*



/**
 * Created by gsora on 4/9/17.
 *
 *
 * Custom adapter, useful for listing some statuses.
 */
class StatusesListAdapter(data: RealmResults<Status>, locale: String, private val parentCtx: Context, private val timelineContent: Timeline.TimelineContent
) : RealmRecyclerViewAdapter<Status, RowViewHolder>(data, true) {

    private val TOOT = 0
    private val TOOT_CW = 1
    private val TOOT_BOOST = 2
    private val TOOT_BOOST_CW = 3

    init {
        val systemLocale = locale
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder? {
        var viewHolder: RowViewHolder? = null
        val inflater = LayoutInflater.from(parent.context)

        when (viewType) {
            TOOT -> {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onCreateViewHolder: type found: $TOOT")
                }
                val toot = inflater.inflate(R.layout.status_toot, parent, false)
                viewHolder = RowViewHolder(toot, TOOT, parentCtx, timelineContent)
            }
            TOOT_BOOST -> {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onCreateViewHolder: type found: $TOOT_BOOST")
                }
                val tootBoost = inflater.inflate(R.layout.status_boosted_toot, parent, false)
                viewHolder = RowViewHolder(tootBoost, TOOT_BOOST, parentCtx, timelineContent)
            }
            TOOT_BOOST_CW -> {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onCreateViewHolder: type found: $TOOT_BOOST_CW")
                }
                val tootBoostCw = inflater.inflate(R.layout.status_boosted_toot_cw, parent, false)
                viewHolder = RowViewHolder(tootBoostCw, TOOT_BOOST_CW, parentCtx, timelineContent)
            }
            TOOT_CW -> {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onCreateViewHolder: type found: $TOOT_CW")
                }
                val tootCw = inflater.inflate(R.layout.status_toot_cw, parent, false)
                viewHolder = RowViewHolder(tootCw, TOOT_CW, parentCtx, timelineContent)
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
        holder.bindData(getItem(holder.adapterPosition)!!)
        val s = holder.data
        val sb = s!!.reblog

        if (BuildConfig.DEBUG) {
            if (s.mediaAttachments!!.size > 0) {
                Log.d(TAG, "onBindViewHolder: found status with media attachments!")
                s.mediaAttachments!!.forEach { mediaAttachment -> Log.d(TAG, "media: \t" + mediaAttachment.previewUrl!!) }
            }

            if (sb != null && sb.mediaAttachments!!.size > 0) {
                Log.d(TAG, "onBindViewHolder: found status with boost, and media attachments!")
                s.mediaAttachments!!.forEach { mediaAttachment -> Log.d(TAG, "media: \t" + mediaAttachment.previewUrl!!) }
            }
        }

        if (s.getThisIsABoost()!!) { // this is a boost
            setStatusViewTo(sb!!.account!!.displayName, sb.content, sb.account!!.avatar, s.account!!.displayName, sb.createdAt, holder, sb.spoilerText, sb.mediaAttachments, sb.getSensitive()!!)
        } else {
            setStatusViewTo(s.account!!.displayName, s.content, s.account!!.avatar, null, s.createdAt, holder, s.spoilerText, s.mediaAttachments, s.getSensitive()!!)
        }

    }

    override fun getItemViewType(position: Int): Int {
        val toot = getItem(position)
        // if getThisIsABoost(), cw == null, it's a boost
        if (toot!!.reblog != null) {
            Log.d(TAG, "getItemViewType: got boost, spoilerText len:" + toot.spoilerText!!.length)
            return if (toot.spoilerText!!.isNotEmpty()) {
                TOOT_BOOST_CW
            } else TOOT_BOOST

        }

        return if (toot.spoilerText!!.isNotEmpty()) {
            // cw != null, it's a toot+cw
            TOOT_CW
        } else TOOT

        // Otherwise, it's just a toot
    }

    private fun setStatusViewTo(author: String?, content: String?, avatar: String?, booster: String?, timestamp: String?, holder: RowViewHolder, spoilerText: String?, mediaAttachment: RealmList<MediaAttachment>?, sensitiveContent: Boolean) {

        // Standard setup: timestamp and avatar
        Glide
                .with(parentCtx)
                .load(avatar)
                .crossFade()
                .into(holder.avatar)

        holder.avatar.setOnClickListener {Timeline.newInstance(Timeline.TimelineContent.HASHCODE) }

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
            d = fmt.parse(timestamp)
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

        // status author, and status
        holder.statusAuthor?.text = CoolHtml.html(author!!)

        // if holder.boostAuthor != null, set it
        if (holder.boostAuthor != null) {
            if (booster != null) {
                if (BuildConfig.DEBUG) {
                    holder.timestamp?.text = holder.timestamp?.text.toString() + " - boosted by " + booster
                }
                holder.boostAuthor?.text = String.format(parentCtx.getString(R.string.boostedBy), booster)
            }
        }

        // if holder.contentWarningText != null, set it
        holder.contentWarningText?.let {
            if (spoilerText!!.isNotEmpty()) {
                it.text = spoilerText
            }
        }

        // if there are boost and star button, use the correct available data
        if (holder.star != null && holder.boost != null) {
            var reblogged = false
            var faved = false

            if (holder.data!!.reblog != null) {
                reblogged = holder.data!!.reblog!!.getReblogged()!!
            } else {
                reblogged = holder.data!!.getReblogged()!!
            }

            if (holder.data!!.reblog != null) {
                faved = holder.data!!.reblog!!.getFavourited()!!
            } else {
                faved = holder.data!!.getFavourited()!!
            }

            if (reblogged) {
                holder.boost?.setImageDrawable(ContextCompat.getDrawable(parentCtx, R.drawable.ic_autorenew_blue_500_24dp))
            } else {
                holder.boost?.setImageDrawable(ContextCompat.getDrawable(parentCtx, R.drawable.ic_autorenew_black_24dp))
            }

            if (faved) {
                holder.star?.setImageDrawable(ContextCompat.getDrawable(parentCtx, R.drawable.ic_stars_yellow_600_24dp))
            } else {
                holder.star?.setImageDrawable(ContextCompat.getDrawable(parentCtx, R.drawable.ic_stars_black_24dp))
            }
        }


        // set media if any
        if (mediaAttachment != null && mediaAttachment.size > 0) {
            holder.masterImageContainer?.visibility = View.VISIBLE

            holder.imageContainerFirst?.visibility = View.GONE
            holder.imageContainerSecond?.visibility = View.GONE

            holder.firstImage?.visibility = View.GONE
            holder.secondImage?.visibility = View.GONE
            holder.thirdImage?.visibility = View.GONE
            holder.fourthImage?.visibility = View.GONE

            for (i in mediaAttachment.indices) {
                putImageInContainer(mediaAttachment[i].previewUrl,mediaAttachment[i].url, i, holder, sensitiveContent)

            }
            when (mediaAttachment.size - 1) {
                0, 1 -> holder.imageContainerFirst?.visibility = View.VISIBLE
                2, 3 -> {
                    holder.imageContainerFirst?.visibility = View.VISIBLE
                    holder.imageContainerSecond?.visibility = View.VISIBLE
                }
            }
        } else {
            holder.masterImageContainer?.visibility = View.GONE
        }

        holder.status.text = CoolHtml.html(content!!)


    }

    private fun hideFirstMediaContainer(holder: RowViewHolder) {
        val l = holder.imageContainerFirst?.layoutParams as LinearLayout.LayoutParams
        l.height = 0
        holder.imageContainerFirst?.layoutParams = l
    }

    private fun hideSecondMediaContainer(holder: RowViewHolder) {
        val l = holder.imageContainerSecond?.layoutParams as LinearLayout.LayoutParams
        l.height = 0
        holder.imageContainerSecond?.layoutParams = l
    }

    private fun setImageOrSensitive(previewUrl: String?,url: String?, imageView: ImageView, sensitiveContent: Boolean) {
        if (!sensitiveContent) {
            Glide
                    .with(parentCtx)
                    .load(previewUrl)
                    .crossFade()
                    .into(imageView)
        } else {
            imageView.setImageDrawable(ColorDrawable(Color.GRAY))
        }
        imageView.visibility = View.VISIBLE


        imageView.setOnClickListener {

            tabsIntent.launchUrl(parentCtx, Uri.parse(url))

        }

    }





    private val tabsIntent: CustomTabsIntent by lazy {
        CustomTabsHelper.createTabsIntent(parentCtx)
    }


    private fun putImageInContainer(previewUrl: String?,url: String?,index: Int, holder: RowViewHolder, sensitiveContent: Boolean) {
        when (index) {
            0 -> holder.firstImage?.let { setImageOrSensitive(previewUrl,url, it, sensitiveContent) }
            1 -> holder.secondImage?.let { setImageOrSensitive(previewUrl,url, it, sensitiveContent) }
            2 -> holder.thirdImage?.let { setImageOrSensitive(previewUrl,url, it, sensitiveContent) }
            3 -> holder.fourthImage?.let { setImageOrSensitive(previewUrl,url, it, sensitiveContent) }
        }
    }

    override fun getItemId(index: Int): Long {
        return index.toLong()
    }

    companion object {

        private val TAG = StatusesListAdapter::class.java.simpleName
    }

}
