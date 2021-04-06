package codes.monkey.cafe.waiter.adapters.api

import codes.monkey.cafe.waiter.domain.model.ItemsOutOfStockException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(ItemsOutOfStockException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleItemsOutOfStockException(ex: ItemsOutOfStockException): Map<String, String> {
        return mapOf(
                "error" to ex.message!!
        )
    }
}