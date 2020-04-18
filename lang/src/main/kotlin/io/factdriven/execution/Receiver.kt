package io.factdriven.execution

import com.fasterxml.jackson.annotation.JsonIgnore
import io.factdriven.impl.utils.Id
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.reflect.KClass
import kotlin.collections.Map

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Receptor(val receiving: Type, val expecting: Map<String, Any?> = emptyMap(), val correlating: MessageId? = null) {

    constructor(receiving: KClass<*>, expecting: Map<String, Any?> = emptyMap()): this (receiving.type, expecting)
    constructor(receiving: KClass<*>, correlating: MessageId): this (receiving.type, emptyMap(), correlating)

    val hash = Id(this) @JsonIgnore get

}

data class Receiver (val entity: EntityId, val receptor: Receptor)
