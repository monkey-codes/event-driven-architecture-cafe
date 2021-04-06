package codes.monkey.cafe.starter

import codes.monkey.cafe.starter.leaderelection.LeaderElectionConfig
import org.springframework.context.annotation.Import

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Import(LeaderElectionConfig::class)
annotation class EnableLeaderElection
