package MastodonTypes

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.apache.commons.lang3.builder.ToStringBuilder

import java.io.Serializable

open class Application : RealmObject(), Serializable {
    @SerializedName("name")
    @Expose
    var name: String? = null
    @SerializedName("website")
    @Expose
    var website: String? = null

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    override fun hashCode(): Int {
        return HashCodeBuilder().append(name).append(website).toHashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other !is Application) {
            return false
        }
        val rhs = other as Application?
        return EqualsBuilder().append(name, rhs?.name).append(website, rhs?.website).isEquals
    }

    companion object {

        private const val serialVersionUID = -6057618037440799795L
    }

}
