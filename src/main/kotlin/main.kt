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

fun remove(element: Element) {
    TODO()
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
            // пытаемся перейти по суффиксу
            File(currDir, element.keyHash.drop(i)).isDirectory -> {
                currDir = File(currDir, element.keyHash.drop(i))
                break
            }
            else -> return null
        }
    }

    // обходим файлы в поиске нашего ключа
    currDir.walk().forEach {
        if (it.isFile && getKeyFromFile(it) == element.key) {
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
     * Находит МЕХ имён файлов в папке parent
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
        QueryType.REMOVE -> remove(element)
        QueryType.GET -> {
            val node = getNode(element)
            println(if (node == null) "No such key" else getValueFromFile(node))
        }
    }
}
