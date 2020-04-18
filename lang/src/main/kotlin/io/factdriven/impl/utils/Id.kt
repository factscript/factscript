package io.factdriven.impl.utils

import java.util.*
import kotlin.RuntimeException

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@Suppress("FunctionName")
fun Id(seed: Any? = null): String = "gid-" + (when (seed) {
    // Use a random UUID when no seed is defined
    null -> UUID.randomUUID()
    // Use the seed itself if it already is a UUID
    is UUID -> seed
    // Generate UUID from a seed representing one
    is String -> try { UUID.fromString(seed) } catch (e: RuntimeException) { null }
    else -> null
    // Last not least generate a UUID unique for the string representation of any other seed
} ?: UUID.nameUUIDFromBytes(seed.toString().toByteArray())).toString()
