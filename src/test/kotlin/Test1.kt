import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertTimeoutPreemptively
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.time.Duration
import kotlin.test.*

internal class Test1 {
    private val standardOut = System.out
    private val standardIn = System.`in`
    private val stream = ByteArrayOutputStream()

    @BeforeTest
    fun setUp() {
        clearDatabase()

        System.setOut(PrintStream(stream))
    }

    @AfterTest
    fun tearDown() {
        clearDatabase()

        System.setOut(standardOut)
        System.setIn(standardIn)
    }

    @Test
    fun testGetNode1() {
        val elem = Element("some_key", "some_value")
        val parent = File("db")
        parent.mkdirs()
        val file = createFileForElement(parent, elem)

        assertEquals(getNode(elem), file)
    }

    @Test
    fun testGetNode2() {
        val elem1 = Element("one_key", "one_value")
        val parent1 = File("db/${elem1.keyHash[0]}")
        parent1.mkdirs()
        val file1 = createFileForElement(parent1, elem1)

        val elem2 = Element("other_key", "other_value")
        val parent2 = File("db/${elem2.keyHash[0]}")
        parent2.mkdirs()
        val file2 = createFileForElement(parent2, elem2)

        assertEquals(getNode(elem1), file1)
        assertEquals(getNode(elem2), file2)
        assertEquals(getNode(Element("third_key", null)), null)
    }

    @Test
    fun testRemove1() {
        val elem = Element("some_key", "some_value")
        val parent = File("db")
        parent.mkdirs()
        val file = createFileForElement(parent, elem)

        assertEquals(getNode(elem), file)
        assertDoesNotThrow { (remove(elem)) }
        assertEquals(getNode(elem), null)
        assertFailsWith<NoSuchKey> { remove(elem) }
    }

    @Test
    fun testRemove2() {
        val elem1 = Element("one_key", "one_value")
        val parent1 = File("db/${elem1.keyHash[0]}")
        parent1.mkdirs()
        val file1 = createFileForElement(parent1, elem1)

        val elem2 = Element("other_key", "other_value")
        val parent2 = File("db/${elem2.keyHash[0]}")
        parent2.mkdirs()
        val file2 = createFileForElement(parent2, elem2)

        assertEquals(getNode(elem1), file1)
        assertEquals(getNode(elem2), file2)
        assertDoesNotThrow { remove(elem2) }
        assertEquals(getNode(elem2), null)
        assertFailsWith<NoSuchKey> { remove(elem2) }
        assertDoesNotThrow { remove(elem1) }
        assertEquals(getNode(elem1), null)
    }

    @Test
    fun testAdd1() {
        val elem = Element("some_key", "some_value")

        assertDoesNotThrow { add(elem) }
        assertFailsWith<KeyAlreadyExists> { add(elem) }
        assertEquals(getNode(elem)?.readLines() ?: "", listOf(elem.key, elem.value))
    }

    @Test
    fun testAdd2() {
        val elem1 = Element("one_key", "one_value")
        val elem2 = Element("other_key", "other_value")

        assertDoesNotThrow { add(elem1) }
        assertDoesNotThrow { add(elem2) }

        assertEquals(getNode(elem1)?.readLines() ?: "", listOf(elem1.key, elem1.value))
        assertEquals(getNode(elem2)?.readLines() ?: "", listOf(elem2.key, elem2.value))
    }

    @Test
    fun testWorkWithFiles() {
        val element = Element("someKey", "someValue")
        val file = createFileForElement(File(STARTDIR), element)
        assertEquals(getKeyFromFile(file), element.key)
        assertEquals(getValueFromFile(file), element.value)
    }

    @Test
    fun testProcessInput() {
        assertEquals(processInput(listOf("add", "32", "23")), Query(QueryType.ADD, Element("32", "23")))
        assertEquals(processInput(listOf("remove", "hello")), Query(QueryType.REMOVE, Element("hello", null)))
        assertEquals(processInput(listOf("get", "end")), Query(QueryType.GET, Element("end", null)))
        assertFailsWith<UnsupportedQuery> { processInput(listOf("some", "incorrect", "query")) }
        assertFailsWith<IncorrectNumberOfArguments> { processInput(listOf("remove", "incorrect", "query")) }
    }

    @Test
    fun testMain1() {
        var expectedOut = ""

        main(arrayOf("add", "key1", "1"))
        main(arrayOf("add", "key1", "2")); expectedOut += "Key key1 already exists"

        main(arrayOf("get", "key1")); expectedOut += "\n1"

        main(arrayOf("remove", "key1"))
        main(arrayOf("remove", "key1")); expectedOut += "\nKey key1 doesn't exists"

        main(arrayOf("get", "key1")); expectedOut += "\nKey key1 doesn't exists"

        assertEquals(expectedOut, stream.toString().trim().lines().joinToString("\n"))
    }

    @Test
    fun testMain2() {
        System.setIn(ByteArrayInputStream("""
            add key1 1
            add key1 2
            get key1
            add key2 2
            get key1
            get key2
            remove key1
            remove key1
            get key1
            get key2
        """.trimIndent().toByteArray()))

        main(arrayOf())

        assertEquals("""
            Key key1 already exists
            1
            1
            2
            Key key1 doesn't exists
            Key key1 doesn't exists
            2
        """.trimIndent(), stream.toString().trim().lines().joinToString("\n"))
    }

    @Test
    fun testEfficiency() {

        val currentMap = mutableMapOf<String, String>()
        val nQueries = 3000

        fun newElement(): Element {
            var newKey = ""
            while (currentMap.containsKey(newKey)) {
                newKey += ('a'..'z').random()
            }
            val newVal = (1..10000).random().toString()
            return Element(newKey, newVal)
        }

        val queries = List(nQueries) {
            if (currentMap.isEmpty()) {
                val newElement = newElement()
                currentMap[newElement.key] = newElement.value.toString()
                Query(QueryType.ADD, newElement)
            }
            else when (QueryType.values().random()) {
                QueryType.ADD -> {
                    val newElement = newElement()
                    currentMap[newElement.key] = newElement.value.toString()
                    Query(QueryType.ADD, newElement)
                }
                QueryType.REMOVE -> {
                    val key = currentMap.keys.random()
                    currentMap.remove(key)
                    Query(QueryType.REMOVE, Element(key, null))
                }
                QueryType.GET -> {
                    val key = currentMap.keys.random()
                    Query(QueryType.GET, Element(key, null))
                }
            }
        }

        assertTimeoutPreemptively(Duration.ofSeconds(20)) {
            queries.forEach { processQuery(it) }
        }
    }
}
