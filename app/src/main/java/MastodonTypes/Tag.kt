package MastodonTypes

/*
  Created by gsora on 4/21/17.
  <p>
  A Tag present in a Mastodon Status object.
 */

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

open class Tag : RealmObject() {

    @SerializedName("name")
    @Expose
    var name: String? = null
    @SerializedName("url")
    @Expose
    var url: String? = null

}