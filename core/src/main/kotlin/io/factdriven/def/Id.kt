package io.factdriven.def

import io.factdriven.play.name

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

val Child.isFirstChild: Boolean get() {
    return parent.children.first() == this
}

val Child.isLastChild: Boolean get() {
    return parent.children.last() == this
}

val Child.index: Int get() {
    return parent.children.indexOf(this)
}

internal val Child.numberOfEntityType: Int get() {
    return parent.children.count {
        it.typeName == typeName && it.index <= index
    }
}
