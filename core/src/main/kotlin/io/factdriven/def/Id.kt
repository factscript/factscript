package io.factdriven.def

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
        is Catching -> catchingType.simpleName!!
        is Throwing -> throwingType.simpleName!!
        else -> entityType.simpleName!!
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
