import java.io.File
import kotlin.test.*

internal class Test1 {

    @BeforeTest
    fun setUp() {
        clearDatabase()
    }

    @AfterTest
    fun tearDown() {
        clearDatabase()
    }

    @Test
    fun testGetNode1() {
        val elem = Element("some_key", "some_value")
        val parent = File("db/${elem.keyHash}")
        parent.mkdirs()
        val file = createFileForElement(parent, elem)

        assertEquals(getNode(elem), file)
    }

    @Test
    fun testGetNode2() {
        val elem1 = Element("one_key", "one_value")
        val parent1 = File("db/${elem1.keyHash[0]}/${elem1.keyHash.drop(1)}")
        parent1.mkdirs()
        val file1 = createFileForElement(parent1, elem1)

        val elem2 = Element("other_key", "other_value")
        val parent2 = File("db/${elem2.keyHash[0]}/${elem2.keyHash.drop(1)}")
        parent2.mkdirs()
        val file2 = createFileForElement(parent2, elem2)

        assertEquals(getNode(elem1), file1)
        assertEquals(getNode(elem2), file2)
        assertEquals(getNode(Element("third_key", null)), null)
    }
}
