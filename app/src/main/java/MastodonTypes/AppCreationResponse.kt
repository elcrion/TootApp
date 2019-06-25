package MastodonTypes

/*
 * Created by gsora on 4/20/17.
 *
 * This class represents what the Mastodon API gives us after a succesful POST request to /api/v1/apps.
 */

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class AppCreationResponse {

    @SerializedName("id")
    @Expose
    var id: String? = null
    @SerializedName("client_id")
    @Expose
    var clientId: String? = null
    @SerializedName("client_secret")
    @Expose
    var clientSecret: String? = null

}
