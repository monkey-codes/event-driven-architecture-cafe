package codes.monkey.cafe.cashier

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CafeCashierServiceApplication

fun main(args: Array<String>) {
	runApplication<CafeCashierServiceApplication>(*args)
}
