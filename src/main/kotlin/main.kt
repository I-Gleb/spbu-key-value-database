enum class QueryType { ADD, REMOVE, GET }

data class Element(val key: String, val value: String?)

data class InputData(val queryType: QueryType, val element: Element)

fun processInput(args: Array<String>): InputData {
    TODO()
}

fun add(element: Element) {
    TODO()
}

fun remove(element: Element) {
    TODO()
}

fun getValue(element: Element): String? {
    TODO()
}

fun main(args: Array<String>) {
    val (queryType, element) = processInput(args)

    when (queryType) {
        QueryType.ADD -> add(element)
        QueryType.REMOVE -> remove(element)
        QueryType.GET -> println(getValue(element))
    }
}
