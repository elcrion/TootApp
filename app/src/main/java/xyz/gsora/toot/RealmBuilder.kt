package xyz.gsora.toot

import io.realm.Realm
import io.realm.RealmConfiguration

import xyz.gsora.toot.Timeline.Companion

/**
 * Created by gsora on 5/5/17.
 *
 *
 * Build a Realm configuration based on the destination timeline content
 */
object RealmBuilder {

    fun getRealmForTimelineContent(timelineContent: Timeline.TimelineContent): Realm {
        var config: RealmConfiguration? = null

        when (timelineContent) {
            Timeline.TimelineContent.TIMELINE_MAIN -> config = RealmConfiguration.Builder().name(Companion.TIMELINE_MAIN).build()
            Timeline.TimelineContent.TIMELINE_LOCAL -> config = RealmConfiguration.Builder().name(Companion.TIMELINE_LOCAL).build()
            Timeline.TimelineContent.TIMELINE_FEDERATED -> config = RealmConfiguration.Builder().name(Companion.TIMELINE_FEDERATED).build()
            Timeline.TimelineContent.FAVORITES -> config = RealmConfiguration.Builder().name(Companion.FAVORITES).build()
            Timeline.TimelineContent.NOTIFICATIONS -> config = RealmConfiguration.Builder().name(Companion.NOTIFICATIONS).build()
            Timeline.TimelineContent.List -> config = RealmConfiguration.Builder().name(Companion.List).build()
            Timeline.TimelineContent.HASHCODE -> config = RealmConfiguration.Builder().name(Companion.Hashcode).build()
        }

        return Realm.getInstance(config!!)
    }
}
