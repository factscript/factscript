package io.factdriven.flow.lang

import io.factdriven.flow.FlowInstance
import io.factdriven.flow.FlowMessage
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

enum class FlowActionType {
    acceptance, progress, success, fix, failure, intent
}

interface FlowActionMessage {

    infix fun by(action: () -> Any)

}

open class FlowActionImpl<I: FlowInstance, M: Any>: FlowNode,
    FlowActionMessage {

    override var id = ""

    var actionType = FlowActionType.success
    var action: (() -> Any)? = null

    infix fun intent(id: String): FlowActionMessage {
        actionType = FlowActionType.intent
        this.id = id
        return this
    }

    infix fun acceptance(id: String): FlowActionMessage {
        actionType = FlowActionType.acceptance
        this.id = id
        return this
    }

    infix fun progress(id: String): FlowActionMessage {
        actionType = FlowActionType.progress
        this.id = id
        return this
    }

    infix fun success(id: String): FlowActionMessage {
        actionType = FlowActionType.success
        this.id = id
        return this
    }

    infix fun failure(id: String): FlowActionMessage {
        actionType = FlowActionType.failure
        this.id = id
        return this
    }

    /*
    infix fun intent(kClass: KClass<out FlowMessage>): FlowActionMessage {
        return intent(kClass.simpleName!!)
    }

    infix fun acceptance(kClass: KClass<out FlowMessage>): FlowActionMessage {
        return acceptance(kClass.simpleName!!)
    }

    infix fun progress(kClass: KClass<out FlowMessage>): FlowActionMessage {
        return progress(kClass.simpleName!!)
    }

    infix fun success(kClass: KClass<out FlowMessage>): FlowActionMessage {
        return success(kClass.simpleName!!)
    }

    infix fun failure(kClass: KClass<out FlowMessage>): FlowActionMessage {
        return failure(kClass.simpleName!!)
    }
    */

    override fun by(action: () -> Any) {
        this.action = action
    }

}
