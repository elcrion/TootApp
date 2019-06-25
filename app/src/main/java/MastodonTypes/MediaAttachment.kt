package MastodonTypes

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.apache.commons.lang3.builder.ToStringBuilder

import java.io.Serializable

open class MediaAttachment : RealmObject(), Serializable {
    @SerializedName("id")
    @Expose
    var id: Int? = null
    @SerializedName("remote_url")
    @Expose
    var remoteUrl: String? = null
    @SerializedName("type")
    @Expose
    var type: String? = null
    @SerializedName("url")
    @Expose
    var url: String? = null
    @SerializedName("preview_url")
    @Expose
    var previewUrl: String? = null
    @SerializedName("text_url")
    @Expose
    var textUrl: String? = null

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    override fun hashCode(): Int {
        return HashCodeBuilder().append(id).append(remoteUrl).append(type).append(url).append(previewUrl).append(textUrl).toHashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other !is MediaAttachment) {
            return false
        }
        val rhs = other as MediaAttachment?
        return EqualsBuilder().append(id, rhs?.id).append(remoteUrl, rhs?.remoteUrl).append(type, rhs?.type).append(url, rhs?.url).append(previewUrl, rhs?.previewUrl).append(textUrl, rhs?.textUrl).isEquals
    }

    companion object {

        private const val serialVersionUID = 8349948183094094758L
    }

}
