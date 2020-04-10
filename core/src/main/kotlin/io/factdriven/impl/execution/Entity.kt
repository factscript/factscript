package io.factdriven.impl.execution

import io.factdriven.Messages
import io.factdriven.impl.utils.apply
import io.factdriven.impl.utils.construct
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class EntityId(val type: String, val id: String?) {
    constructor(kClass: KClass<*>, id: String? = null): this(kClass.type.toString(), id)
}

fun <A: Any> List<Any>.newInstance(entityType: KClass<A>): A {
    return entityType.newInstance(this)
}

inline fun <reified A: Any> List<Any>.newInstance(): A {
    return A::class.newInstance(this)
}

fun <A: Any> KClass<A>.newInstance(facts: List<Any>): A {
    val fact: Any.() -> Fact<*> = {
        when (this) {
            is Message -> fact
            is Fact<*> -> this
            else -> throw IllegalArgumentException()
        }
    }
    return newInstance(*facts.map { it.fact() } .toTypedArray())
}

fun <A: Any> KClass<A>.newInstance(vararg message: Message): A {
    return newInstance(*message.map { it.fact }.toTypedArray())
}

fun <A: Any> KClass<A>.newInstance(vararg fact: Fact<*>): A {
    val entity = construct(fact.first().details)
    fact.asList().subList(1, fact.size).forEach { f ->
        entity.apply(f.details)
    }
    return entity
}

fun <I: Any> KClass<I>.load(id: String): I {
    return Messages.load(Messages.load(id), this)
}
