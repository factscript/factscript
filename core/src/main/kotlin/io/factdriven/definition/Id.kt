package io.factdriven.definition

import io.factdriven.execution.name

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
val Node.id: String get() {
    val id = StringBuffer()
    if (this is Child)
        id.append(parent.id).append("-")
    id.append(typeName)
    if (this is Child)
        id.append("-").append(numberOfEntityType)
    return id.toString()
}

val Node.typeName: String get() {
    return when (this) {
        is Throwing -> throwingType.name
        is Consuming -> catchingType.name
        else -> entityType.name
    }
}

val Node.isFirstChild: Boolean get() {
    return parent == null || parent!!.children.first() == this
}

val Node.isLastChild: Boolean get() {
    return parent == null || parent!!.children.last() == this
}

val Node.index: Int get() {
    return parent?.children?.indexOf(this) ?: 0
}

internal val Child.numberOfEntityType: Int get() {
    return parent.children.count {
        it.typeName == typeName && it.index <= index
    }
}
