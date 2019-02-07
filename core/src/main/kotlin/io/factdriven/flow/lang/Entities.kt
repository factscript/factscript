package io.factdriven.flow.lang

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
typealias Entity = Any
typealias EntityId = String
typealias EntityName = String
typealias EntityType = KClass<out Entity>
