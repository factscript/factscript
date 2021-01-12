package org.factscript.execution

import com.fasterxml.jackson.annotation.JsonIgnore
import org.factscript.language.impl.utils.Id
import kotlin.reflect.KClass
import kotlin.collections.Map

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Receptor(val receiving: Type? = null, val expecting: Map<String, Any?> = emptyMap(), val correlating: MessageId? = null) {

    constructor(receiving: KClass<*>, expecting: Map<String, Any?> = emptyMap()): this (receiving.type, expecting)

    val hash = Id(this) @JsonIgnore get

}

data class Receiver (val entity: EntityId, val receptor: Receptor)
