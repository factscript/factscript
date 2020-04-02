package io.factdriven.traverse

import io.factdriven.definition.Definition
import io.factdriven.language.Given
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass
import kotlin.reflect.full.staticFunctions
import kotlin.reflect.jvm.javaMethod

@Disabled
class SequentialNodeTraverserTest {

    init {
        Definition.init(io.factdriven.traverse.examples.payment1.PaymentRetrieval::class)
        Definition.init(io.factdriven.traverse.examples.payment2.PaymentRetrieval::class)
    }

    @DisplayName("Simple traverse with start and end")
    @Test
    fun testSimpleTraverse() {
        val fullTraverse = SequentialNodeTraverser(getDefinition(io.factdriven.traverse.examples.payment1.PaymentRetrieval::class)).fullTraverse()

        assertTrue(fullTraverse.size == 2)

        val startingTraverse = fullTraverse.first()
        val endTraverse = fullTraverse.last()

        assertTrue(startingTraverse.nextTraverse == endTraverse)
        assertTrue(endTraverse.previousTraverse == startingTraverse)

        assertTrue(startingTraverse.isStart())
        assertFalse(endTraverse.isStart())

        assertTrue(endTraverse.isEnd())
        assertFalse(startingTraverse.isEnd())
    }

    @DisplayName("Traverse with multiple node traverser")
    @Test
    fun testExample2Traverse(){
        val definition = getDefinition(io.factdriven.execution.examples.payment2.PaymentRetrieval::class)
        val fullTraverse = SequentialNodeTraverser(definition).fullTraverse()

        assertTrue(fullTraverse.size == 4)

        val startingTraverse = fullTraverse.first()
        val endTraverse = fullTraverse.last()

        assertTrue(startingTraverse.nextTraverse == fullTraverse[1])
        assertTrue(fullTraverse[1].previousTraverse == startingTraverse)
        assertTrue(fullTraverse[1].nextTraverse == fullTraverse[2])
        assertTrue(fullTraverse[2].previousTraverse == fullTraverse[1])
        assertTrue(fullTraverse[2].nextTraverse == endTraverse)
        assertTrue(endTraverse.previousTraverse == fullTraverse[2])

        assertFalse(fullTraverse[1].isStart())
        assertFalse(fullTraverse[1].isEnd())

        assertTrue(startingTraverse.isStart())
        assertFalse(endTraverse.isStart())

        assertTrue(endTraverse.isEnd())
        assertFalse(startingTraverse.isEnd())
    }

    @DisplayName("Traverse of XOR")
    @Test
    fun testExampleWithXOR(){
        val definition = getDefinition(io.factdriven.traverse.examples.payment2.PaymentRetrieval::class)
        val fullTraverse = SequentialNodeTraverser(definition).fullTraverse()

        assertTrue(fullTraverse.size == 3)
        assertTrue(fullTraverse[0] is NodeTraverse)
        assertTrue(fullTraverse[1] is BlockTraverse)
        assertTrue(fullTraverse[2] is NodeTraverse)

        val blockTraverse : BlockTraverse = fullTraverse[1] as BlockTraverse

        assertTrue(blockTraverse.paths.size == 2)
        assertTrue(blockTraverse.previousTraverse == fullTraverse[0])
        assertTrue(blockTraverse.nextTraverse == blockTraverse.paths[0])

        val firstPathTraverse : PathTraverse = blockTraverse.paths[0]
        val secondPathTraverse : PathTraverse = blockTraverse.paths[1]

        assertTrue(firstPathTraverse.addition is Given<*>)
        assertTrue(secondPathTraverse.addition is Given<*>)

        assertTrue(firstPathTraverse.previousTraverse == blockTraverse)
        assertTrue(firstPathTraverse.nextTraverse == firstPathTraverse.path.first())
        assertTrue(firstPathTraverse.path.last().nextTraverse == secondPathTraverse)

        assertTrue(secondPathTraverse.previousTraverse == firstPathTraverse.path.last())
        assertTrue(secondPathTraverse.nextTraverse == secondPathTraverse.path.first())
        assertTrue(secondPathTraverse.path.last().nextTraverse == fullTraverse[2])

    }


    private fun getDefinition(kclass: KClass<*>) : Definition {
        kclass.staticFunctions.stream()
                .filter { t -> t.name == "init" }
                .forEach { t -> t.javaMethod?.invoke(null, null) }
        return Definition.getDefinitionByType(kclass)
    }
}