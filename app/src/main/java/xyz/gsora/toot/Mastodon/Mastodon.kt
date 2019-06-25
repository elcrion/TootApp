package xyz.gsora.toot.Mastodon

import MastodonTypes.*
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import xyz.gsora.toot.BuildConfig
import xyz.gsora.toot.Toot
import java.util.*

/**
 * Created by gsora on 4/20/17.
 *
 *
 * This is the Mastodon API implementation class.
 */
class Mastodon private constructor() {

    /**
     * Returns information about current logged user
     *
     * @return [<] containing information about the current logged user
     */
    val loggedUserInfo: Observable<Response<Account>>
        get() = buildRxRetrofit().create(API::class.java).getLoggedUserInfo(
                Toot.buildBearer()
        )

    /**
     * Returns the first page of the user's home
     *
     * @return an array of Status containing the user's home first page
     */
    val homeTimeline: Observable<Response<Array<Status>>>
        get() = buildRxRetrofit().create(API::class.java).getHomeTimeline(
                Toot.buildBearer()
        )


    /**
     * Get the public (federated) timeline
     *
     * @return an array of Status containing the newest federated statuses
     */
    val publicTimeline: Observable<Response<Array<Status>>>
        get() = buildRxRetrofit().create(API::class.java).getPublicTimeline(
                Toot.buildBearer(),
                null
        )

    /**
     * Get the local timeline
     *
     * @return an array of Status containing the newest local statuses
     */
    val localTimeline: Observable<Response<Array<Status>>>
        get() = buildRxRetrofit().create(API::class.java).getPublicTimeline(
                Toot.buildBearer(),
                "local"
        )

    /**
     * Returns the first page of the user's favorites
     *
     * @return an array of Status containing the user's favorites first page
     */
    val favorites: Observable<Response<Array<Status>>>
        get() = buildRxRetrofit().create(API::class.java).getFavorites(
                Toot.buildBearer()
        )

    /**
     * Returns the first page of user's notification.
     *
     * @return an array of Notification.
     */
    val notifications: Observable<Response<Array<Notification>>>
        get() = buildRxRetrofit().create(API::class.java).getNotification(
                Toot.buildBearer()
        )

    /**
     * An OkHttpClient with logging capabilities
     *
     * @return OkHttpClient which logs every step of the request
     */
    private fun logger(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder()

        return if (BuildConfig.DEBUG) {
            client.addInterceptor(interceptor).build()
        } else {
            client.build()
        }
    }

    /**
     * Build a Retrofit instance with JSON converter and a RxJava call adapter.
     *
     * @return Retrofit instance
     */
    private fun buildRxRetrofit(): Retrofit {
        return Retrofit.Builder()
                .baseUrl(Toot.getInstanceURL())
                .client(logger())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
    }

    /**
     * Generate an application name
     *
     * @return string containing the application name
     */
    private fun generateApplicationName(): String {
        return "TootApp-" + Toot.getUsername()
    }

    /**
     * Returns an Observable with the Mastodon API application process response.
     *
     * @return Observable with App creation response.
     */
    fun createMastodonApplication(): Observable<AppCreationResponse> {
        return buildRxRetrofit().create(API::class.java).createMastodonApplication(
                generateApplicationName(),
                REDIRECT_URI,
                SCOPES
        )
    }

    /**
     * Returns an Observable that exposes an OAuth token response.
     *
     * @param code authorization code received during the browser authentication section.
     * @return Observable with OAuth token response.
     */
    fun requestOAuthTokens(code: String): Observable<OAuthResponse> {
        return buildRxRetrofit().create(API::class.java).requestOAuthTokens(
                Toot.getClientID(),
                Toot.getClientSecret(),
                REDIRECT_URI,
                "authorization_code",
                code,
                SCOPES
        )
    }

    /**
     * Returns the page of the user's home located by the URL
     *
     * @param url page to retrieve
     * @return an array of Status containing the user's home at the given URL
     */
    fun getHomeTimeline(url: String): Observable<Response<Array<Status>>> {
        return buildRxRetrofit().create(API::class.java).getHomeTimeline(
                Toot.buildBearer(),
                url
        )
    }

    /**
     * Post a status as the current logged user
     *
     * @param statusContent    content of the status to post
     * @param inReplyToId      id of the status to reply to, optional
     * @param mediaIds         array of Base64-encoded images to post, max 4, optional
     * @param sensitive        mark the status as sensitive, optional
     * @param spoilerText      text to prepend to the status, actually creating a "Content warning", optional
     * @param statusVisibility type of the visibility the status needs to have, optional
     * @return the status posted
     */
    fun postPublicStatus(statusContent: String, inReplyToId: String?, mediaIds: List<String>, sensitive: Boolean?, spoilerText: String?, statusVisibility: StatusVisibility): Observable<Response<Status>> {
        val fields = HashMap<String, Any>()
        fields["status"] = statusContent
        val hasAttachments = mediaIds.size > 0
        if (inReplyToId != null) {
            fields["in_reply_to_id"] = inReplyToId
        }


        fields["sensitive"] = sensitive!! && hasAttachments

        if (spoilerText != null) {
            fields["spoiler_text"] = spoilerText
        }

        when (statusVisibility) {
            Mastodon.StatusVisibility.PUBLIC -> fields["visibility"] = "public"
            Mastodon.StatusVisibility.DIRECT -> fields["visibility"] = "direct"
            Mastodon.StatusVisibility.PRIVATE -> fields["visibility"] = "private"
            Mastodon.StatusVisibility.UNLISTED -> fields["visibility"] = "unlisted"
        }

        mediaIds.let {
            return buildRxRetrofit().create(API::class.java).postStatusWithMedia(
                    Toot.buildBearer(),
                    fields,
                    it
            )
        }

        return buildRxRetrofit().create(API::class.java).postStatus(
                Toot.buildBearer(),
                fields
        )
    }


    fun postAttachments(filePart: MultipartBody.Part): Observable<Response<MediaAttachment>> {


        return buildRxRetrofit().create(API::class.java).postMedia(Toot.buildBearer(), filePart)

    }

    /**
     * Get the public (federated) timeline
     *
     * @param url page to retrieve
     * @return an array of Status containing the newest federated statuses at the given url
     */
    fun getPublicTimeline(url: String): Observable<Response<Array<Status>>> {
        return buildRxRetrofit().create(API::class.java).getPublicTimeline(
                Toot.buildBearer(),
                url, null
        )
    }

    /**
     * Get the public local timeline
     *
     * @param url page to retrieve
     * @return an array of Status containing the newest local statuses at the given url
     */
    fun getLocalTimeline(url: String): Observable<Response<Array<Status>>> {
        return buildRxRetrofit().create(API::class.java).getPublicTimeline(
                Toot.buildBearer(),
                url,
                "local"
        )
    }

    /**
     * Returns the page of the user's favorites located by the URL
     *
     * @param url page to retrieve
     * @return an array of Status containing the user's favorites at the given URL
     */
    fun getFavorites(url: String): Observable<Response<Array<Status>>> {
        return buildRxRetrofit().create(API::class.java).getFavorites(
                Toot.buildBearer(),
                url
        )
    }

    /**
     * Returns the selected page of user's notification.
     *
     * @param url page to retrieve
     * @return an array of Notification.
     */
    fun getNotifications(url: String): Observable<Response<Array<Notification>>> {
        return buildRxRetrofit().create(API::class.java).getNotification(
                Toot.buildBearer(),
                url
        )
    }

    /**
     * Mark a status as favourited
     *
     * @param statusId the status to mark
     * @return the favourited [Status]
     */
    fun favourite(statusId: String): Observable<Response<Status>> {
        return buildRxRetrofit().create(API::class.java).favourite(
                Toot.buildBearer(),
                statusId
        )
    }

    /**
     * Mark a status as unfavourited
     *
     * @param statusId the status to mark
     * @return the unfavourited [Status]
     */
    fun unfavourite(statusId: String): Observable<Response<Status>> {
        return buildRxRetrofit().create(API::class.java).unfavourite(
                Toot.buildBearer(),
                statusId
        )
    }

    /**
     * Mark a status as reblogged
     *
     * @param statusId the status to mark
     * @return the reblogged [Status]
     */
    fun reblog(statusId: String): Observable<Response<Status>> {
        return buildRxRetrofit().create(API::class.java).reblog(
                Toot.buildBearer(),
                statusId
        )
    }

    /**
     * Mark a status as unreblogged
     *
     * @param statusId the status to mark
     * @return the unreblogged [Status]
     */
    fun unreblog(statusId: String): Observable<Response<Status>> {
        return buildRxRetrofit().create(API::class.java).unreblog(
                Toot.buildBearer(),
                statusId
        )
    }

    /**
     * Types of status visibility admitted by Mastodon instances
     */
    enum class StatusVisibility {
        PUBLIC,
        DIRECT,
        PRIVATE,
        UNLISTED
    }

    companion object {

        /**
         * The URI where the Mastodon instance will return OAuth codes.
         */
        val REDIRECT_URI = "https://xyz.gsora.toot/oauth"
        /**
         * Standard scopes.
         */
        val SCOPES = "read write follow"
        /**
         * Get an hold of the Mastodon.
         *
         * @return the Mastodon singleton.
         */
        val instance = Mastodon()
    }



}
