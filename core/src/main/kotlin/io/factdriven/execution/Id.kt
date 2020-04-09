package io.factdriven.execution

import com.fasterxml.jackson.annotation.JsonIgnore
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class EntityId(val type: String, val id: String?) {
    constructor(kClass: KClass<*>, id: String? = null): this(kClass.type.toString(), id)
}

data class MessageId(val entityId: EntityId, val version: Int = 0) {

    val hash = hash(this) @JsonIgnore get

    companion object {

        fun nextAfter(last: MessageId): MessageId {
            return MessageId(last.entityId, last.version + 1)
        }

        private val digest = MessageDigest.getInstance("MD5")

        private fun hash(messageId: MessageId): String {
            val buffer = StringBuffer()
            buffer.append("name=").append(messageId.entityId.type)
            if (messageId.entityId.id != null) {
                buffer.append("|id=").append(messageId.entityId.id)
            }
            buffer.append("|version=").append(messageId.version)
            val bytes = digest.digest(buffer.toString().toByteArray())
            return String.format("%0" + (bytes.size shl 1) + "x", BigInteger(1, bytes))
        }

    }

}
