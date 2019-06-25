package MastodonTypes

/*
  Created by gsora on 4/20/17.
  <p>
  POJO representation of a successful OAuth response.
 */

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class OAuthResponse {

    @SerializedName("access_token")
    @Expose
    var accessToken: String? = null
    @SerializedName("token_type")
    @Expose
    var tokenType: String? = null
    @SerializedName("expires_in")
    @Expose
    var expiresIn: Int? = null
    @SerializedName("refresh_token")
    @Expose
    var refreshToken: String? = null

}