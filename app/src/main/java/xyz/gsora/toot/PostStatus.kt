package xyz.gsora.toot

import MastodonTypes.Status
import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmConfiguration
import retrofit2.Response
import xyz.gsora.toot.Mastodon.Mastodon
import xyz.gsora.toot.Timeline.Companion.TIMELINE_MAIN
import java.util.*

/**
 * An [IntentService] subclass for handling asynchronous toot sending.
 *
 *
 */
class PostStatus : IntentService("PostStatus") {
    private var realm: Realm? = null
    private var nM: NotificationManager? = null
    internal lateinit var m: Mastodon

    private var notificationId: Int = 0

    override fun onHandleIntent(intent: Intent?) {
        m = Mastodon.instance
        realm = Realm.getInstance(RealmConfiguration.Builder().name(TIMELINE_MAIN).build())

        nM = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val r = Random()
        notificationId = r.nextInt()

        if (intent != null) {
            val status = intent.getStringExtra(STATUS)
            val replyid = intent.getStringExtra(REPLYID)
            val mediaids = intent.getStringArrayListExtra(MEDIAIDS)
            val sensitive = intent.getBooleanExtra(SENSITIVE, false)
            val spoilertext = intent.getStringExtra(SPOILERTEXT)
            val visibility = intent.getIntExtra(VISIBILITY, 0)

            var trueVisibility: Mastodon.StatusVisibility = Mastodon.StatusVisibility.PUBLIC

            when (visibility) {
                0 -> trueVisibility = Mastodon.StatusVisibility.PUBLIC
                1 -> trueVisibility = Mastodon.StatusVisibility.DIRECT
                2 -> trueVisibility = Mastodon.StatusVisibility.UNLISTED
                3 -> trueVisibility = Mastodon.StatusVisibility.PRIVATE
            }

            // create a new "toot sending" notification
            val mBuilder = buildNotification(
                    "Sending toot", null,
                    true
            )
            nM!!.notify(notificationId, mBuilder.build())

            val post = m.postPublicStatus(status, replyid, mediaids, sensitive, spoilertext, trueVisibility)
            post
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            Consumer<Response<Status>> { this.postSuccessful(it) },
                            Consumer<Throwable> { this.postError(it) }
                    )

        }
    }


    private fun postSuccessful(response: Response<Status>) {
        Log.d(TAG, "postSuccessful: post ok!")

        // toot sent, remove notification
        nM!!.cancel(notificationId)
    }

    private fun postError(error: Throwable) {
        Log.d(TAG, "postError: post error! --> $error")

        // cancel "sending" notification id
        nM!!.cancel(notificationId)

        // create a new "error" informative notification
        val mBuilder = buildNotification(
                "Failed to send toot",
                "We had some problems sending your toot, check your internet connection, or maybe the Mastodon instance you're using could be down.",
                false
        )

        nM!!.notify(notificationId + 1, mBuilder.build())
    }

    /**
     * Builds a [NotificationCompat.Builder] with some predefined properties
     *
     * @param title                notification title
     * @param text                 notification body, can be null
     * @param hasUndefinedProgress declare if the notification have to contain an undefined progressbar
     * @return a [NotificationCompat.Builder] with the properties passed as parameter.
     */
    private fun buildNotification(title: String, text: String?, hasUndefinedProgress: Boolean?): NotificationCompat.Builder {
        val mBuilder = NotificationCompat.Builder(this)
        mBuilder.setContentTitle(title)
        val style = NotificationCompat.BigTextStyle()
        if (text != null) {
            style.bigText(text)
        }
        mBuilder.setStyle(style)
        mBuilder.setSmallIcon(R.drawable.ic_reply_white_24dp)
        if (hasUndefinedProgress!!) {
            mBuilder.setProgress(0, 0, true)
        }
        return mBuilder
    }

    companion object {

        val STATUS = "xyz.gsora.toot.extra.status"
        val REPLYID = "xyz.gsora.toot.extra.replyid"
        val MEDIAIDS = "xyz.gsora.toot.extra.mediaids"
        val SENSITIVE = "xyz.gsora.toot.extra.sensitive"
        val SPOILERTEXT = "xyz.gsora.toot.extra.spoilertext"
        val VISIBILITY = "xyz.gsora.toot.extra.visibility"
        private val TAG = PostStatus::class.java.simpleName
    }
}
