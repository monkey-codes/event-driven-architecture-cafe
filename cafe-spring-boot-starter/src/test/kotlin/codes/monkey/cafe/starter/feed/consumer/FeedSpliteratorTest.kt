package codes.monkey.cafe.starter.feed.consumer

import assertk.assertThat
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.client.RestOperations
import java.io.BufferedReader

@ExtendWith(MockKExtension::class)
internal class FeedSpliteratorTest {

    @RelaxedMockK
    private lateinit var restOperations: RestOperations

    @BeforeEach
    fun beforeEach() {
        clearAllMocks()
    }

    @Test
    fun `when loading a feed page from saved XML the entries should have their source properties set`() {
        val currentUrl = "https://example.com/feed/42"
        val xmlBlob = javaClass.classLoader.getResourceAsStream("waiter-service-sample-feed.xml").bufferedReader().use(BufferedReader::readText)
        every { restOperations.getForObject(currentUrl, String::class.java) } returns xmlBlob
        val feedSpliterator = FeedSpliterator(currentUrl, Direction.FORWARD, restOperations)

        val feed = feedSpliterator.fetchFeed(currentUrl)

        assertThat(feed.otherLinks).isNotEmpty()
        assertThat(feed.otherLinks.filter { it.rel == "self" }).isNotEmpty()

        assertThat(feed.entries).isNotEmpty()
        feed.entries
            .forEach { assertThat(it.source).isNotNull() }
    }
}
