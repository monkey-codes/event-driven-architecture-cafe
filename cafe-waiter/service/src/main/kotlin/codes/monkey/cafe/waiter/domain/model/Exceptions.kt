package codes.monkey.cafe.waiter.domain.model

class ItemsOutOfStockException(val items: List<String>) : Exception("items out of stock: ${items.joinToString(", ")}")