package io.factdriven.definition.api

import io.factdriven.execution.Type
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Executing {

    val id: String
    val type: Type
    val label: String

    val entityType: KClass<*>

    val index: Int
    val parent: Executing?
    val children: List<Executing>
    val isFirstChild: Boolean
    val isLastChild: Boolean

}