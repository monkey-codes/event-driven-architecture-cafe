package codes.monkey.cafe.starter.feed.producer

import assertk.assertThat
import assertk.assertions.isEqualTo
import codes.monkey.cafe.starter.feed.producer.FeedController.Companion.currentPage
import org.junit.jupiter.api.Test

internal class FeedControllerTest {

    @Test
    fun `it should calculate implied page correctly`() {
        assertThat(currentPage(0, 2)).isEqualTo(0L)
        assertThat(currentPage(1, 2)).isEqualTo(0L)
        assertThat(currentPage(2, 2)).isEqualTo(1L)
        assertThat(currentPage(3, 2)).isEqualTo(1L)
        assertThat(currentPage(4, 2)).isEqualTo(2L)
        assertThat(currentPage(5, 2)).isEqualTo(2L)
    }
}
