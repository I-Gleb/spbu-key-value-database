import java.io.File
import java.lang.Integer.toBinaryString

const val HASHLEN = 31
const val STARTDIR = "db"

enum class QueryType { ADD, REMOVE, GET }

data class Element(val key: String, val value: String?) {
    val keyHash = toBinaryString(key.hashCode()).padStart(HASHLEN, '0')
}

data class InputData(val queryType: QueryType, val element: Element)

fun processInput(args: Array<String>): InputData {
    TODO()
}

fun add(element: Element) {
    TODO()
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
        if (parent.walk().maxDepth(1).count() > 1) break
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

    for (i in 0 until HASHLEN) {
        when {
            // пытаемся перейти по биту
            File(currDir, element.keyHash[i].toString()).isDirectory -> {
                currDir = File(currDir, element.keyHash[i].toString())
            }
            else -> break
        }
    }

    // обходим файлы в поиске нашего ключа
    currDir.walk().maxDepth(1).drop(1).forEach {
        if (getKeyFromFile(it) == element.key) {
            return it
        }
    }
    return null
}

/*
 * В директории parent создаёт файл для element и возращает его
 */
fun createFileForElement(parent: File, element: Element): File {

    /*
     * Находит МЕХ имён файлов в директории parent
     */
    fun mex(): Int {
        var i = 0
        while (File(parent, i.toString()).exists()) ++i
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

fun main(args: Array<String>) {
    val (queryType, element) = processInput(args)

    when (queryType) {
        QueryType.ADD -> add(element)
        QueryType.REMOVE -> {
            if (!remove(element)) println("No such key")
        }
        QueryType.GET -> {
            val node = getNode(element)
            println(if (node == null) "No such key" else getValueFromFile(node))
        }
    }
}
