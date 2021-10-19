import mu.KotlinLogging
import java.io.File
import java.lang.Integer.toBinaryString

const val HASHLEN = 32
const val STARTDIR = "db"

enum class QueryType { ADD, REMOVE, GET }

data class Element(val key: String, val value: String? = null) {
    // создаёт бинарный хэш ключа
    val keyHash = toBinaryString(key.hashCode()).padStart(HASHLEN, '0')
}

data class Query(val queryType: QueryType, val element: Element)

/*
 * Принимает строку с запросом
 * Если запрос корректный, то возращает Query, иначе кидает исключение
 */
fun processInput(args: List<String>): Query {

    /*
     * Принимает строку с запросом
     * Если запрос корректный, то возращает тип запроса, иначе кидает исключение
     */
    fun getQueryType(args: List<String>): QueryType {
        if (args.isEmpty()) throw EmptyLine()
        return when (args[0]) {
            "add" -> if (args.size == 3) QueryType.ADD else throw IncorrectNumberOfArguments(args.size - 1, 2, "add")
            "remove" -> if (args.size == 2) QueryType.REMOVE else throw IncorrectNumberOfArguments(args.size - 1, 1, "remove")
            "get" -> if (args.size == 2) QueryType.GET else throw IncorrectNumberOfArguments(args.size - 1, 1, "get")
            else -> throw UnsupportedQuery(args[0])
        }
    }

    return when (getQueryType(args)) {
        QueryType.ADD -> Query(QueryType.ADD, Element(args[1], args[2]))
        QueryType.REMOVE -> Query(QueryType.REMOVE, Element(args[1]))
        QueryType.GET -> Query(QueryType.GET, Element(args[1]))
    }
}

/*
 * Получает element.
 * Если файла с таким ключом не существует, то добавляет его.
 * Иначе кидает исключение.
 */
fun add(element: Element) {

    /*
     * Создаёт для файла более глубокую директорию, соответсвующую его хэшу, и перемещает его туда
     */
    fun push(parent: File, file: File, i: Int) {
        val elem = getElementFromFile(file)
        file.delete()

        val newDir = File(parent, elem.keyHash[i].toString())
        newDir.mkdir()
        createFileForElement(newDir, elem)
    }


    if (getNode(element) != null) throw KeyAlreadyExists(element.key)

    // начинаем в корне структуры
    var currDir = File(STARTDIR)

    // спускаемся в директорию, в которую надо поместить файл для element
    for (i in 0 until HASHLEN) {
        if (getChildren(currDir).count() == 0) break

        // спускаем все файлы из текущей директории на уровень ниже
        getChildren(currDir).filter{ it.isFile }.forEach{ push(currDir, it, i) }

        // переходим по биту, а если такой директории не существует, то создаём
        currDir = File(currDir, element.keyHash[i].toString())
        if (!currDir.isDirectory) currDir.mkdir()
    }

    createFileForElement(currDir, element)
}

/*
 * Получает element.
 * Если есть файл с ключом element.key, то удаляет его и все более не нужные директории.
 * Иначе кидает исключение.
 */
fun remove(element: Element) {
    var file = getNode(element) ?: throw NoSuchKey(element.key)
    while (file.name != "db") {
        val parent = file.parentFile
        file.delete()
        file = parent
        if (getChildren(file).count() > 0) break
    }
}

/*
 * Получает element.
 * Ищет файл с ключом element.key спуском по структуре.
 * Если файл найден, то возращает его, иначе null.
 */
fun getNode(element: Element): File? {
    // начинаем в корне структуры
    var currDir = File(STARTDIR)

    // спускаемся по структуре до директории, в которой должен лежать соответсвующий файл
    for (bit in element.keyHash) {
        when {
            // пытаемся перейти по биту
            File(currDir, bit.toString()).isDirectory -> {
                currDir = File(currDir, bit.toString())
            }
            else -> break
        }
    }

    // ищем в директории нужный файл
    return getChildren(currDir).find { it.isFile && getKeyFromFile(it) == element.key }
}

/*
 * В директории parent создаёт файл для element и возращает его
 */
fun createFileForElement(parent: File, element: Element): File {

    /*
     * Находит МЕХ имён файлов в директории parent
     */
    fun mex(): Int {
        var i = 2
        while (File(parent, i.toString()).isFile) ++i
        return i
    }

    val file = File(parent, mex().toString())
    file.createNewFile()
    file.writeText("${element.key}\n${element.value}\n")
    return file
}

/*
 * Удаляет все элементы базы данных
 */
fun clearDatabase() {
    File(STARTDIR).deleteRecursively()
    File(STARTDIR).mkdir()
}

/*
 * По файлу получает ключ, который в нем хранится
 */
fun getKeyFromFile(file: File) = file.readLines()[0]

/*
 * По файлу получает значение, которое в нем хранится
 */
fun getValueFromFile(file: File) = file.readLines()[1]

/*
 * По файлу получает элемент, который в нем хранится
 */
fun getElementFromFile(file: File) = Element(getKeyFromFile(file), getValueFromFile(file))

/*
 * Возращает содержимое директории
 */
fun getChildren(dir: File) = dir.walk().maxDepth(1).drop(1)

/*
 * Выполняет один запрос
 */
fun processQuery(query: Query) {
    val (queryType, element) = query
    when (queryType) {
        QueryType.ADD -> add(element)
        QueryType.REMOVE -> remove(element)
        QueryType.GET -> {
            val node = getNode(element) ?: throw NoSuchKey(element.key)
            println(getValueFromFile(node))
        }
    }
}

val logger = KotlinLogging.logger {  }

fun main(args: Array<String>) {
    logger.info {"program started"}

    if (args.isNotEmpty())
        try { processQuery(processInput(args.toList())) }
        catch (e: Exception) {
            logger.error { e.message }
            println(e.message)
        }
    else generateSequence { readLine() }.forEach {
        try { processQuery(processInput(it.split(" "))) }
        catch (e: Exception) {
            logger.error { e.message }
            println(e.message)
        }
    }

    logger.info {"program completed"}
}
