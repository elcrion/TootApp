package MastodonTypes

/*
  Created by gsora on 4/21/17.
  <p>
  A Mention present in a Mastodon Status object.
 */

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

open class Mention : RealmObject() {

    @SerializedName("url")
    @Expose
    var url: String? = null
    @SerializedName("username")
    @Expose
    var username: String? = null
    @SerializedName("acct")
    @Expose
    var acct: String? = null
    @SerializedName("id")
    @Expose
    var id: String? = null

}