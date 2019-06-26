package xyz.gsora.toot

/**
 * Created by gsora on 4/20/17.
 *
 *
 * A class to represent an Username/Instance URI tuple.
 */
class UserTuple {
    var user: String? = null
    var instanceURI: String? = null

    constructor() {
        this.user = ""
        this.instanceURI = ""
    }

    constructor(user: String, instanceURI: String) {
        this.user = user
        this.instanceURI = instanceURI
    }

    override fun toString(): String {
        return "UserTuple{" +
                "user='" + user + '\''.toString() +
                ", instanceURI='" + instanceURI + '\''.toString() +
                '}'.toString()
    }
}
