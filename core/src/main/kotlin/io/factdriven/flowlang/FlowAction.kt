package io.factdriven.flowlang

import kotlin.reflect.KProperty

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
open class FlowAction<I: FlowInstance, M: Any>: FlowNode {

    lateinit var actionType: FlowActionType
    lateinit var action: () -> Any

    infix fun intent(action: () -> Any) {
        actionType = FlowActionType.intent
        this.action = action
    }

    infix fun acceptance(action: () -> Any) {
        TODO()
    }

    infix fun progress(action: () -> Any) {
        TODO()
    }

    infix fun success(action: () -> Any) {
        actionType = FlowActionType.success
        this.action = action
    }

    infix fun failure(action: () -> Any) {
        TODO()
    }

}

enum class FlowActionType {
    acceptance, progress, success, fix, failure, intent
}

open class FlowReaction<I: Any, M: Any> {

    lateinit var actionType: FlowActionType
    var action: ((M) -> Any)? = null

    infix fun acceptance(action: (M) -> Any) {
        TODO()
    }

    infix fun progress(action: (M) -> Any) {
        TODO()
    }

    infix fun success(action: (M) -> Any) {
        actionType = FlowActionType.success
        this.action = action
    }

    infix fun rerun(action: (M) -> Any) {
        TODO()
    }

    infix fun failure(action: (M) -> Any) {
        TODO()
    }

    infix fun mitigation(definition: FlowDefinition<I>.() -> Unit): FlowDefinition<I> = TODO()

}

class FlowReactionToMessage<A: Any, M: Any>(val listener: FlowListener<M>): FlowReaction<A, M>() {

    var p: String by Delegate()

    fun success() {}

    infix fun having(key: () -> Pair<String, Any>): FlowReactionToMessage<A, M> {
        TODO()
    }

    infix fun supporting(assertion: (M) -> Boolean): FlowReactionToMessage<A, M> {
        TODO()
    }

}

class Delegate {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return "$thisRef, thank you for delegating '${property.name}' to me!"
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        println("$value has been assigned to '${property.name}' in $thisRef.")
    }
}