package MastodonTypes

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.apache.commons.lang3.builder.ToStringBuilder

import java.io.Serializable

open class Account : RealmObject(), Serializable {
    @SerializedName("id")
    @Expose
    var id: Int? = null
    @SerializedName("username")
    @Expose
    var username: String? = null
    @SerializedName("acct")
    @Expose
    var acct: String? = null
    @SerializedName("display_name")
    @Expose
    var displayName: String? = null
    @SerializedName("locked")
    @Expose
    var locked: Boolean? = null
    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null
    @SerializedName("note")
    @Expose
    var note: String? = null
    @SerializedName("url")
    @Expose
    var url: String? = null
    @SerializedName("avatar")
    @Expose
    var avatar: String? = null
    @SerializedName("header")
    @Expose
    var header: String? = null
    @SerializedName("followers_count")
    @Expose
    var followersCount: Int? = null
    @SerializedName("following_count")
    @Expose
    var followingCount: Int? = null
    @SerializedName("statuses_count")
    @Expose
    var statusesCount: Int? = null

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    override fun hashCode(): Int {
        return HashCodeBuilder().append(id).append(username).append(acct).append(displayName).append(locked).append(createdAt).append(note).append(url).append(avatar).append(header).append(followersCount).append(followingCount).append(statusesCount).toHashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other !is Account) {
            return false
        }
        val rhs = other as Account?
        return EqualsBuilder().append(id, rhs?.id).append(username, rhs?.username).append(acct, rhs?.acct).append(displayName, rhs?.displayName).append(locked, rhs?.locked).append(createdAt, rhs?.createdAt).append(note, rhs?.note).append(url, rhs?.url).append(avatar, rhs?.avatar).append(header, rhs?.header).append(followersCount, rhs?.followersCount).append(followingCount, rhs?.followingCount).append(statusesCount, rhs?.statusesCount).isEquals
    }

    companion object {

        private const val serialVersionUID = -7201177137522198661L
    }

}
