package io.factdriven.flowlang

import kotlin.reflect.KProperty

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

enum class FlowActionType {
    acceptance, progress, success, fix, failure, intent
}

open class FlowAction<I: FlowInstance, M: Any>: FlowNode {

    override val label: String get() {
        return action.invoke().javaClass.simpleName
    }

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
