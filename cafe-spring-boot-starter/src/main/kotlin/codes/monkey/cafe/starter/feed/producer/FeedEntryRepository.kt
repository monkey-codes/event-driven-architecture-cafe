package codes.monkey.cafe.starter.feed.producer

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface FeedEntryRepository : PagingAndSortingRepository<FeedEntry, Long> {
    fun findByEntryId(entryId: String): Optional<FeedEntry>

    fun findByEntityId(entityId: String): Optional<FeedEntry>

    fun findByType(type: String, pageable: Pageable): Page<FeedEntry>

    fun findBySequenceNumberBetween(rangeStart: Long, rangeEnd: Long, sort: Sort): List<FeedEntry>

    @Query("select max(sequenceNumber) from FeedEntry")
    fun findMaxSequenceNumber(): Optional<Long>
}
