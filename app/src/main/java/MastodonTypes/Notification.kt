package MastodonTypes

/*
 * Created by gsora on 5/1/17.
 *
 * A Mastodon Notification type.
 */

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Notification : RealmObject() {

    @PrimaryKey
    @SerializedName("id")
    @Expose
    var id: Long? = null
    @SerializedName("type")
    @Expose
    var type: String? = null
    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null
    @SerializedName("account")
    @Expose
    var account: Account? = null
    @SerializedName("status")
    @Expose
    var status: Status? = null

}