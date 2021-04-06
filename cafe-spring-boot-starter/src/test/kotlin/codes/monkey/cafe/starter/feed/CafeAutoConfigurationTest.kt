package codes.monkey.cafe.starter.feed

import codes.monkey.cafe.starter.CafeAutoConfiguration
import codes.monkey.cafe.starter.EnableFeedConsumer
import codes.monkey.cafe.starter.EnableFeedProducer
import codes.monkey.cafe.starter.EnableLeaderElection
import codes.monkey.cafe.starter.feed.consumer.*
import codes.monkey.cafe.starter.feed.producer.FeedBuilder
import codes.monkey.cafe.starter.feed.producer.FeedController
import codes.monkey.cafe.starter.feed.producer.FeedEntryRepository
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import javax.sql.DataSource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.WebApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.integration.jdbc.lock.DefaultLockRepository
import org.springframework.integration.leader.Candidate
import org.springframework.integration.support.leader.LockRegistryLeaderInitiator
import org.springframework.integration.support.locks.LockRegistry
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
import org.springframework.jdbc.datasource.init.ScriptStatementFailedException
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.test.context.jdbc.Sql
import org.springframework.web.client.RestOperations

@ExtendWith(MockKExtension::class)
internal class CafeAutoConfigurationTest {

    private val contextRunner = WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CafeAutoConfiguration::class.java))

    companion object {
        fun memoryDatasource(): DataSource =
                DriverManagerDataSource().apply {
                    setDriverClassName("org.h2.Driver")
                    url = "jdbc:h2:mem:db;DB_CLOSE_DELAY=-1"
                    username = "sa"
                    password = "sa"
                }
    }

    @EnableFeedProducer
    @Configuration
    class SampleFeedProducerConfiguration {
        @Bean
        fun dataSource(): DataSource = memoryDatasource()
    }

    @Configuration
    @EnableScheduling
    @EnableFeedConsumer
    class SampleFeedConsumerConfiguration {
        @Bean
        fun dataSource(): DataSource = memoryDatasource()

        @Bean
        fun restOperations(): RestOperations = mockk(relaxed = true)

        @Bean
        fun feedListener(): FeedListener = mockk(relaxed = true)
    }

    @Configuration
    @EnableLeaderElection
    @EnableScheduling
    @EnableFeedConsumer
    class SampleFeedConsumerWithLeaderElectionConfiguration {
        @Bean
        fun dataSource(): DataSource = memoryDatasource()

        @Bean
        fun restOperations(): RestOperations = mockk(relaxed = true)

        @Bean
        fun feedListener(): FeedListener = mockk(relaxed = true)
    }

    @Configuration
    @EnableScheduling
    @EnableFeedConsumer
    @EnableFeedProducer
    class SampleFeedConsumerAndProducerConfiguration {
        @Bean
        fun dataSource(): DataSource = memoryDatasource()

        @Bean
        fun restOperations(): RestOperations = mockk(relaxed = true)

        @Bean
        fun feedListener(): FeedListener = mockk(relaxed = true)
    }

    @EnableLeaderElection
    @Configuration
    class SampleLeaderElectionConfiguration {
        @Bean
        fun dataSource(): DataSource = memoryDatasource()
    }

    @Test
    fun `it should not load feed producer beans`() {
        contextRunner.run { context ->
            assertThat(context).doesNotHaveBean(FeedBuilder::class.java)
            assertThat(context).doesNotHaveBean(FeedController::class.java)
            assertThat(context).doesNotHaveBean(FeedEntryRepository::class.java)
        }
    }


    @Test
    fun `it should not load feed consumer beans`() {
        contextRunner.run { context ->
            assertThat(context).doesNotHaveBean(FeedConsumer::class.java)
            assertThat(context).doesNotHaveBean(FeedConsumerScheduler::class.java)
            assertThat(context).doesNotHaveBean(FeedPositionRepository::class.java)
        }
    }

    @Test
    fun `it should load feed producer beans if enabled`() {
        contextRunner
                .withUserConfiguration(SampleFeedProducerConfiguration::class.java)
                .withPropertyValues("atomfeed.server.page-size=2")
                .run { context ->
                    assertThat(context).hasSingleBean(FeedBuilder::class.java)
                    assertThat(context).hasSingleBean(FeedController::class.java)
                    assertThat(context).hasSingleBean(FeedEntryRepository::class.java)
                    assertThat(context).doesNotHaveBean(FeedConsumer::class.java)
                    assertThat(context).doesNotHaveBean(FeedConsumerScheduler::class.java)
                    assertThat(context).doesNotHaveBean(FeedPositionRepository::class.java)
                }
    }

    @Test
    fun `it should load feed consumer beans if enabled and no leader election is available`() {
        contextRunner
                .withUserConfiguration(SampleFeedConsumerWithLeaderElectionConfiguration::class.java)
                .withPropertyValues("atomfeed.client.schedules[0].cron=30 * * * * *", "atomfeed.client.schedules[0].url=http://localhost")
                .run { context ->
                    assertThat(context).doesNotHaveBean(FeedBuilder::class.java)
                    assertThat(context).doesNotHaveBean(FeedController::class.java)
                    assertThat(context).doesNotHaveBean(FeedEntryRepository::class.java)
                    assertThat(context).hasSingleBean(LeaderOnlyFeedConsumerScheduler::class.java)
                    assertThat(context).hasSingleBean(FeedConsumer::class.java)
                    assertThat(context).hasSingleBean(FeedConsumerScheduler::class.java)
                    assertThat(context).hasSingleBean(FeedPositionRepository::class.java)
                    assertThat(context).doesNotHaveBean("feedConsumerScheduler")
                }
    }

    @Test
    fun `it should load feed consumer and producer beans if both is enabled`() {
        contextRunner
                .withUserConfiguration(SampleFeedConsumerAndProducerConfiguration::class.java)
                .withPropertyValues("atomfeed.server.page-size=2")
                .run { context ->
                    assertThat(context).hasSingleBean(FeedBuilder::class.java)
                    assertThat(context).hasSingleBean(FeedController::class.java)
                    assertThat(context).hasSingleBean(FeedEntryRepository::class.java)
                    assertThat(context).hasSingleBean(FeedConsumer::class.java)
                    assertThat(context).hasSingleBean(FeedConsumerScheduler::class.java)
                    assertThat(context).hasSingleBean(FeedPositionRepository::class.java)
                }
    }

}