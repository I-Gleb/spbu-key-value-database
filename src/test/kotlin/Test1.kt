import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
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
        assertEquals(remove(elem), true)
        assertEquals(getNode(elem), null)
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
        assertEquals(remove(elem2), true)
        assertEquals(getNode(elem2), null)
        assertEquals(remove(elem2), false)
        assertEquals(remove(elem1), true)
        assertEquals(getNode(elem1), null)
    }

    @Test
    fun testAdd1() {
        val elem = Element("some_key", "some_value")

        assertEquals(add(elem), true)
        assertEquals(getNode(elem)?.readLines() ?: "", listOf(elem.key, elem.value))
    }

    @Test
    fun testAdd2() {
        val elem1 = Element("one_key", "one_value")
        val elem2 = Element("other_key", "other_value")

        assertEquals(add(elem1), true)
        assertEquals(add(elem2), true)
        assertEquals(getNode(elem1)?.readLines() ?: "", listOf(elem1.key, elem1.value))
        assertEquals(getNode(elem2)?.readLines() ?: "", listOf(elem2.key, elem2.value))
    }

    @Test
    fun testProcessInput() {
        assertEquals(processInput(arrayOf("add", "32", "23")), InputData(QueryType.ADD, Element("32", "23")))
        assertEquals(processInput(arrayOf("remove", "hello")), InputData(QueryType.REMOVE, Element("hello", null)))
        assertEquals(processInput(arrayOf("get", "end")), InputData(QueryType.GET, Element("end", null)))
    }

    @Test
    fun testMain() {
        var expectedOut = ""

        main(arrayOf("add", "key1", "1"))
        main(arrayOf("add", "key1", "2")); expectedOut += "This key already exists"

        main(arrayOf("get", "key1")); expectedOut += "\n1"

        main(arrayOf("add", "key2", "2"))

        main(arrayOf("get", "key1")); expectedOut += "\n1"
        main(arrayOf("get", "key2")); expectedOut += "\n2"

        main(arrayOf("remove", "key1"))
        main(arrayOf("remove", "key1")); expectedOut += "\nNo such key"

        main(arrayOf("get", "key1")); expectedOut += "\nNo such key"
        main(arrayOf("get", "key2")); expectedOut += "\n2"

        assertEquals(expectedOut, stream.toString().trim().lines().joinToString("\n"))
    }
}
