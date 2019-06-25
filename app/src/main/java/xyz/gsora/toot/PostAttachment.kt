package xyz.gsora.toot

import MastodonTypes.Status
import android.app.IntentService
import android.content.Intent
import android.util.Log
import retrofit2.Response
import xyz.gsora.toot.Mastodon.Mastodon

class PostAttachment : IntentService("PostAttachment"){
    internal lateinit var m: Mastodon
    private var attachments:ArrayList<String> ?= null
    private  var attachmentIds:ArrayList<String> = ArrayList()

    override fun onHandleIntent(intent: Intent?) {
        m = Mastodon.instance

        if (intent != null) {
             attachments = intent.getStringArrayListExtra(MEDIA)

        }

//        attachments?.forEach{ attachment ->
//
//            var postAttachment = m.postAttachments(attachment)
//
//            postAttachment.observeOn(AndroidSchedulers.mainThread())
//                    .subscribeOn(Schedulers.io())
//                    .subscribe(
//
//                                Consumer<Response<Status>> { this.postSuccessful(it) },
//                                Consumer<Throwable> { this.postError(it) }
//
//                    )
//
//        }



    }


    private fun postSuccessful(response: Response<Status> ) {
        Log.d(PostAttachment.TAG, "Post attachment Successful: post ok!")

        response.body().content?.let { attachmentIds.add(it) }

    }

    private fun postError(error: Throwable) {
        Log.d(PostAttachment.TAG, "attachment postError: post error! --> $error")



    }




    companion object {

        val MEDIA = "xyz.gsora.toot.extra.media"
        private val TAG = PostStatus::class.java.simpleName
    }


}