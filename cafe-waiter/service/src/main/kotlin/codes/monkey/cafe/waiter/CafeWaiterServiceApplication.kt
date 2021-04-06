package codes.monkey.cafe.waiter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CafeWaiterServiceApplication

fun main(args: Array<String>) {
	runApplication<CafeWaiterServiceApplication>(*args)
}
