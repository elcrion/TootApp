package MastodonTypes

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.apache.commons.lang3.builder.ToStringBuilder

import java.io.Serializable

open class Status : RealmObject(), Serializable {
    @PrimaryKey
    @SerializedName("id")
    @Expose
    var id: Long = 0

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("in_reply_to_id")
    @Expose
    var inReplyToId: String? = null

    @SerializedName("in_reply_to_account_id")
    @Expose
    var inReplyToAccountId: String? = null

    @SerializedName("sensitive")
    @Expose
    private var sensitive: Boolean? = null

    @SerializedName("spoiler_text")
    @Expose
    var spoilerText: String? = null

    @SerializedName("visibility")
    @Expose
    var visibility: String? = null

    @SerializedName("application")
    @Expose
    var application: Application? = null

    @SerializedName("account")
    @Expose
    var account: Account? = null

    @SerializedName("media_attachments")
    @Expose
    var mediaAttachments: RealmList<MediaAttachment>? = null

    @SerializedName("mentions")
    @Expose
    var mentions: RealmList<Mention>? = null

    @SerializedName("tags")
    @Expose
    var tags: RealmList<Tag>? = null

    @SerializedName("uri")
    @Expose
    var uri: String? = null

    @SerializedName("content")
    @Expose
    var content: String? = null

    @SerializedName("url")
    @Expose
    var url: String? = null

    @SerializedName("reblogs_count")
    @Expose
    var reblogsCount: Int? = null

    @SerializedName("favourites_count")
    @Expose
    var favouritesCount: Int? = null

    @SerializedName("reblog")
    @Expose
    var reblog: Boost? = null

    @SerializedName("favourited")
    @Expose
    private var favourited: Boolean? = null

    @SerializedName("reblogged")
    @Expose
    private var reblogged: Boolean? = null

    private var thisIsABoost: Boolean = false

    fun getThisIsABoost(): Boolean? {
        return thisIsABoost
    }

    fun setThisIsABoost(thisIsABoost: Boolean?) {
        this.thisIsABoost = thisIsABoost!!
    }

    fun getSensitive(): Boolean? {
        return if (sensitive == null) {
            false
        } else sensitive
    }

    fun setSensitive(sensitive: Boolean?) {
        this.sensitive = sensitive
    }

    fun getFavourited(): Boolean? {
        return if (favourited == null) {
            false
        } else favourited
    }

    fun setFavourited(favourited: Boolean?) {
        this.favourited = favourited
    }

    fun getReblogged(): Boolean? {
        return if (reblogged == null) {
            false
        } else reblogged
    }

    fun setReblogged(reblogged: Boolean?) {
        this.reblogged = reblogged
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    override fun hashCode(): Int {
        return HashCodeBuilder().append(id).append(createdAt).append(inReplyToId).append(inReplyToAccountId).append(sensitive).append(spoilerText).append(visibility).append(application).append(account).append(mediaAttachments).append(mentions).append(tags).append(uri).append(content).append(url).append(reblogsCount).append(favouritesCount).append(reblog).append(favourited).append(reblogged).toHashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other !is Status) {
            return false
        }
        val rhs = other as Status?
        return EqualsBuilder().append(id, rhs?.id).append(createdAt, rhs?.createdAt).append(inReplyToId, rhs?.inReplyToId).append(inReplyToAccountId, rhs?.inReplyToAccountId).append(sensitive, rhs?.sensitive).append(spoilerText, rhs?.spoilerText).append(visibility, rhs?.visibility).append(application, rhs?.application).append(account, rhs?.account).append(mediaAttachments, rhs?.mediaAttachments).append(mentions, rhs?.mentions).append(tags, rhs?.tags).append(uri, rhs?.uri).append(content, rhs?.content).append(url, rhs?.url).append(reblogsCount, rhs?.reblogsCount).append(favouritesCount, rhs?.favouritesCount).append(reblog, rhs?.reblog).append(favourited, rhs?.favourited).append(reblogged, rhs?.reblogged).isEquals
    }

    companion object {

        private const val serialVersionUID = 4983372382391510544L
    }

}
