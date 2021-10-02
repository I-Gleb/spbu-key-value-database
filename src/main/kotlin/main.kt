import java.io.File
import java.lang.Integer.toBinaryString
import java.nio.file.Files
import kotlin.math.ceil
import kotlin.system.exitProcess

const val HASHLEN = 31
const val STARTDIR = "db"

enum class QueryType { ADD, REMOVE, GET }

data class Element(val key: String, val value: String?) {
    val keyHash = toBinaryString(key.hashCode()).padStart(HASHLEN, '0')
}

data class InputData(val queryType: QueryType, val element: Element)

fun processInput(args: Array<String>): InputData {
    if (args.isEmpty()) {
        println("No arguments provided")
        exitProcess(1)
    }
    return when (args[0]) {
        "add" -> {
            if (args.size != 3) {
                println("Incorrect input")
                exitProcess(1)
            }
            InputData(QueryType.ADD, Element(args[1], args[2]))
        }
        "remove" -> {
            if (args.size != 2) {
                println("Incorrect input")
                exitProcess(1)
            }
            InputData(QueryType.REMOVE, Element(args[1], null))
        }
        "get" -> {
            if (args.size != 2) {
                println("Incorrect input")
                exitProcess(1)
            }
            InputData(QueryType.GET, Element(args[1], null))
        }
        else -> {
            println("Incorrect input")
            exitProcess(1)
        }
    }
}

/*
 * Получает element.
 * Если файла с таким ключом не существует, то добавляет его и возвращает true.
 * Иначе возвращает false.
 */
fun add(element: Element): Boolean {

    /*
     * Создаёт для файла более глубокую директорию и перемещает его туда
     */
    fun push(parent: File, file: File, i: Int) {
        val elem = getElementFromFile(file)
        file.delete()

        val newDir = File(parent, elem.keyHash[i].toString())
        newDir.mkdir()
        createFileForElement(newDir, elem)
    }


    if (getNode(element) != null) return false

    // начинаем в корне структуры
    var currDir = File(STARTDIR)

    // спускаемся в директорию, в которую надо поместить файл для element
    for (i in 0 until HASHLEN) {
        if (getChildren(currDir).count() == 0) break

        // спускаем все файлы из директории на уровень ниже
        getChildren(currDir).filter{ it.isFile }.forEach{ push(currDir, it, i) }

        // переходим по биту, а если такой директории не существует, то создаём
        currDir = File(currDir, element.keyHash[i].toString())
        if (!currDir.isDirectory) currDir.mkdir()
    }

    createFileForElement(currDir, element)
    return true
}

/*
 * Получает element.
 * Если есть файл с ключом element.key, то удаляет его и все более не нужные директории и возвращает true.
 * Иначе возвращает false.
 */
fun remove(element: Element): Boolean {
    var file = getNode(element) ?: return false
    while (file.name != "db") {
        val parent = file.parentFile
        file.delete()
        file = parent
        if (getChildren(file).count() > 0) break
    }
    return true
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

fun main(args: Array<String>) {
    val (queryType, element) = processInput(args)

    when (queryType) {
        QueryType.ADD -> {
            if (!add(element)) println("This key already exists")
        }
        QueryType.REMOVE -> {
            if (!remove(element)) println("No such key")
        }
        QueryType.GET -> {
            val node = getNode(element)
            println(if (node == null) "No such key" else getValueFromFile(node))
        }
    }
}
