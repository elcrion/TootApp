package xyz.gsora.toot

import MastodonTypes.Notification
import MastodonTypes.Status
import android.content.Context
import android.content.Intent
import android.support.annotation.Nullable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import retrofit2.Response
import xyz.gsora.toot.Mastodon.Mastodon
import xyz.gsora.toot.Mastodon.ToastMaker
import java.util.*

/**
 * Created by Gianguido Sorà on 28/04/2017.
 *
 *
 * Status row view holder implementation
 */
class RowViewHolder internal constructor(v: View, var type: Int, private val parentCtx: Context, var timelineContent: Timeline.TimelineContent) : RecyclerView.ViewHolder(v) {

    var data: Status? = null
    var dataNotification: Notification? = null
    @BindView(R.id.status_text)
    lateinit var status: TextView
    @BindView(R.id.avatar)
    lateinit var avatar: CircleImageView


    @BindView(R.id.timestamp) @Nullable @JvmField var timestamp: TextView? = null
    @BindView(R.id.status_author) @Nullable @JvmField var statusAuthor: TextView? = null
    @BindView(R.id.boost_author) @Nullable @JvmField var boostAuthor: TextView? = null
    @BindView(R.id.contentWarningText) @Nullable @JvmField var contentWarningText: TextView? = null
    @BindView(R.id.showContentWarning) @Nullable @JvmField var showContentWarning: Button? = null
    @BindView(R.id.reply) @Nullable @JvmField var replyButton: ImageButton? = null
    @BindView(R.id.boostedByNotification) @Nullable @JvmField var boostedByNotification: TextView? = null
    @BindView(R.id.favouritedBy) @Nullable @JvmField var favouritedBy: TextView? = null
    @BindView(R.id.followedBy) @Nullable @JvmField var followedBy: TextView? = null
    @BindView(R.id.star) @Nullable @JvmField var star: ImageButton? = null
    @BindView(R.id.boost) @Nullable @JvmField var boost: ImageButton? = null

    @BindView(R.id.mainContentLayout) @Nullable @JvmField var mainContentLayout: LinearLayout? = null

    @BindView(R.id.masterImageContainer) @Nullable @JvmField var masterImageContainer: LinearLayout? = null
    @BindView(R.id.imageContainerFirst) @Nullable @JvmField var imageContainerFirst: LinearLayout? = null
    @BindView(R.id.imageContainerSecond) @Nullable @JvmField var imageContainerSecond: LinearLayout? = null

    @BindView(R.id.firstImage) @Nullable @JvmField var firstImage: ImageView? = null
    @BindView(R.id.secondImage) @Nullable @JvmField var secondImage: ImageView? = null
    @BindView(R.id.thirdImage) @Nullable @JvmField var thirdImage: ImageView? = null
    @BindView(R.id.fourthImage) @Nullable @JvmField var fourthImage: ImageView? = null


    private val m: Mastodon
    private val bottomStatus: Int? = null
    private val topStatus: Int? = null
    private val leftStatus: Int? = null
    private val rightStatus: Int? = null

    init {
        m = Mastodon.instance
        data = null
        dataNotification = null
        ButterKnife.bind(this, v)
        if (status != null) {
            status.movementMethod = LinkMovementMethod.getInstance()
        }

        // if showContentWarning has been bind
        if (showContentWarning != null) {
            showContentWarning!!.setOnClickListener { button: View ->
                if (status.textSize <= 0.0f) {
                    status.textSize = 16.0f
                } else {
                    status.textSize = 0.0f
                }
            }
        }

        // Bind boost and star buttons
        // we know for sure that if one of the button is null, no button has to be bound
        if (star != null && boost != null) {
            star!!.setOnClickListener { button: View ->
                if (data!!.getFavourited()!!) {
                    m.unfavourite(data!!.id.toString())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    Consumer<Response<Status>> { s: Response<Status> ->
                                        val r = RealmBuilder.getRealmForTimelineContent(timelineContent)
                                        r.executeTransaction { re: Realm -> data!!.setFavourited(false) }
                                        r.close()
                                        star!!.setImageDrawable(ContextCompat.getDrawable(parentCtx, R.drawable.ic_stars_black_24dp))
                                    },
                                    Consumer<Throwable> { this.HandleBadStar(it) }
                            )
                } else {
                    m.favourite(data!!.id.toString())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    Consumer<Response<Status>> { s: Response<Status> ->
                                        val r = RealmBuilder.getRealmForTimelineContent(timelineContent)
                                        r.executeTransaction { re: Realm -> data!!.setFavourited(true) }
                                        r.close()
                                        star!!.setImageDrawable(ContextCompat.getDrawable(parentCtx, R.drawable.ic_stars_yellow_600_24dp))
                                    },
                                    Consumer<Throwable> { this.HandleBadStar(it) }
                            )
                }
            }

            boost!!.setOnClickListener { button: View ->
                if (data!!.getReblogged()!!) {
                    m.unreblog(data!!.id.toString())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    Consumer<Response<Status>> { s: Response<Status> ->
                                        val r = RealmBuilder.getRealmForTimelineContent(timelineContent)
                                        r.executeTransaction { re: Realm ->
                                            val statusToUpdate = re.where(Status::class.java).equalTo("id", s.body().id).findFirst()
                                            if (statusToUpdate != null) {
                                                data!!.setReblogged(false)
                                            }
                                        }
                                        r.close()
                                        boost!!.setImageDrawable(ContextCompat.getDrawable(parentCtx, R.drawable.ic_autorenew_black_24dp))
                                    },
                                    Consumer<Throwable> { this.HandleBadBoost(it) }
                            )
                } else {
                    m.reblog(data!!.id.toString())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    Consumer<Response<Status>> { s: Response<Status> ->
                                        val r = RealmBuilder.getRealmForTimelineContent(timelineContent)
                                        r.executeTransaction { re: Realm ->
                                            val statusToUpdate = re.where(Status::class.java).equalTo("id", s.body().id).findFirst()
                                            if (statusToUpdate != null) {
                                                data!!.setReblogged(true)
                                            }
                                        }
                                        r.close()
                                        boost!!.setImageDrawable(ContextCompat.getDrawable(parentCtx, R.drawable.ic_autorenew_blue_500_24dp))
                                    },
                                    Consumer<Throwable> { this.HandleBadBoost(it) }
                            )
                }
            }
        }

        if (replyButton != null) {
            replyButton!!.setOnClickListener { button: View ->
                val reply = Intent(Toot.getAppContext(), SendToot::class.java)
                val handles = ArrayList<String>()

                if (dataNotification == null) {
                    data!!.account!!.acct?.let { handles.add(it) }
                    if (data!!.mentions!!.size > 0) {
                        for (mention in data!!.mentions!!) {
                            val acct = mention.acct
                            if (!acct!!.contains(Toot.getUsername())) {
                                handles.add(mention.acct!!)
                            }
                        }
                    }
                } else {
                    dataNotification!!.status!!.account!!.acct?.let { handles.add(it) }
                    if (dataNotification!!.status!!.mentions!!.size > 0) {
                        for (mention in dataNotification!!.status!!.mentions!!) {
                            val acct = mention.acct
                            if (!acct!!.contains(Toot.getUsername())) {
                                handles.add(mention.acct!!)
                            }
                        }
                    }
                }

                reply.action = SendToot.REPLY_ACTION
                reply.putStringArrayListExtra(SendToot.REPLY_TO, handles)
                if (dataNotification == null) {
                    reply.putExtra(SendToot.REPLY_TO_ID, java.lang.Long.toString(data!!.id))
                } else {
                    reply.putExtra(SendToot.REPLY_TO_ID, dataNotification!!.status!!.id)
                }

                reply.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                v.context.startActivity(reply)

            }
        }


    }

    private fun HandleBadStar(error: Throwable) {
        ToastMaker.buildToasty(parentCtx, error.toString())
    }

    private fun HandleBadBoost(error: Throwable) {
        ToastMaker.buildToasty(parentCtx, error.toString())
    }

    fun bindData(data: Status) {
        this.data = data
    }

    companion object {

        val TAG = RowViewHolder::class.java.simpleName
    }
}