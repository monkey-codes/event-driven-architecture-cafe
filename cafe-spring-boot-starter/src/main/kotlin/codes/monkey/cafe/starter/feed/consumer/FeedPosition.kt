package codes.monkey.cafe.starter.feed.consumer

import java.time.OffsetDateTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "feed_position")
data class FeedPosition(
    @Id
    @GeneratedValue(strategy = IDENTITY)
    val id: Long = 0,
    val url: String,
    var lastProcessedEntryId: String? = null,
    internal var lastProcessedEntryPageUrl: String? = null,
    var updated: OffsetDateTime = OffsetDateTime.now()
)
