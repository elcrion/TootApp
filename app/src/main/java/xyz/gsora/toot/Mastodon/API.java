package xyz.gsora.toot.Mastodon;

import java.util.List;
import java.util.Map;

import MastodonTypes.Account;
import MastodonTypes.AppCreationResponse;
import MastodonTypes.MediaAttachment;
import MastodonTypes.Notification;
import MastodonTypes.OAuthResponse;
import MastodonTypes.Status;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by gsora on 4/20/17.
 * <p>
 * Mastodon API interface.
 */
@SuppressWarnings({"SameParameterValue", "DefaultAnnotationParam", "WeakerAccess"})
public interface API {

    @FormUrlEncoded
    @POST("api/v1/apps")
    Observable<AppCreationResponse> createMastodonApplication(
            @Field("client_name") String clientName,
            @Field("redirect_uris") String redirectUri,
            @Field("scopes") String scopes
    );

    @FormUrlEncoded
    @POST("oauth/token")
    Observable<OAuthResponse> requestOAuthTokens(
            @Field("client_id") String clientId,
            @Field("client_secret") String clientSecret,
            @Field("redirect_uri") String redirectUri,
            @Field("grant_type") String grantType,
            @Field("code") String code,
            @Field("scope") String scope
    );

    @FormUrlEncoded
    @GET("api/v1/statuses")
    Observable<Response<Status>> getStatus(
                @Header("Authorization") String authBearer,
                @FieldMap(encoded = false) Map<String, Object> fields
    );


    @FormUrlEncoded
    @POST("api/v1/statuses")
    Observable<Response<Status>> postStatusWithMedia(
            @Header("Authorization") String authBearer,
            @FieldMap(encoded = false) Map<String, Object> fields,
            @Field("media_ids[]") List<String> media_ids
    );

    @FormUrlEncoded
    @POST("api/v1/statuses")
    Observable<Response<Status>> postStatus(
            @Header("Authorization") String authBearer,
            @FieldMap(encoded = false) Map<String, Object> fields
    );



    @GET("api/v1/timelines/home")
    Observable<Response<Status[]>> getHomeTimeline(
            @Header("Authorization") String authBearer
    );

    @Multipart
    @POST("api/v1/media")
    Observable<Response<MediaAttachment>> postMedia(
            @Header("Authorization") String authBearer,
            @Part MultipartBody.Part filePart
    );

    @GET
        // to use with Links forward page
    Observable<Response<Status[]>> getHomeTimeline(
            @Header("Authorization") String authBearer,
            @Url String url
    );

    @GET("api/v1/timelines/public")
    Observable<Response<Status[]>> getPublicTimeline(
            @Header("Authorization") String authBearer,
            @Query("local") String local
    );

    @GET
    Observable<Response<Status[]>> getPublicTimeline(
            @Header("Authorization") String authBearer,
            @Url String url,
            @Query("local") String local
    );

    @GET("api/v1/favourites")
    Observable<Response<Status[]>> getFavorites(
            @Header("Authorization") String authBearer
    );

    @GET
    Observable<Response<Status[]>> getFavorites(
            @Header("Authorization") String authBearer,
            @Url String url
    );

    @GET("api/v1/notifications")
    Observable<Response<Notification[]>> getNotification(
            @Header("Authorization") String authBearer
    );

    @GET
    Observable<Response<Notification[]>> getNotification(
            @Header("Authorization") String authBearer,
            @Url String url
    );

    @POST("api/v1/statuses/{statusId}/favourite")
    Observable<Response<Status>> favourite(
            @Header("Authorization") String authBearer,
            @Path("statusId") String statusId
    );

    @POST("api/v1/statuses/{statusId}/unfavourite")
    Observable<Response<Status>> unfavourite(
            @Header("Authorization") String authBearer,
            @Path("statusId") String statusId
    );

    @POST("api/v1/statuses/{statusId}/reblog")
    Observable<Response<Status>> reblog(
            @Header("Authorization") String authBearer,
            @Path("statusId") String statusId
    );

    @POST("api/v1/statuses/{statusId}/unreblog")
    Observable<Response<Status>> unreblog(
            @Header("Authorization") String authBearer,
            @Path("statusId") String statusId
    );

    @GET("api/v1/accounts/verify_credentials")
    Observable<Response<Account>> getLoggedUserInfo(
            @Header("Authorization") String authBearer
    );
}

