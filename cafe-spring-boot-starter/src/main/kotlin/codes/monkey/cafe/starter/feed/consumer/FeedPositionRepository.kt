package codes.monkey.cafe.starter.feed.consumer

import codes.monkey.cafe.starter.feed.consumer.FeedPosition
import java.util.Optional
import org.springframework.data.repository.PagingAndSortingRepository

interface FeedPositionRepository : PagingAndSortingRepository<FeedPosition, Long> {
    fun findFirstByUrl(url: String): Optional<FeedPosition>
}
