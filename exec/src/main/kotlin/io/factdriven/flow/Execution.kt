package io.factdriven.flow

import io.factdriven.flow.lang.*
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.full.memberFunctions

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
/**
 * Reconstruct the past flow instance state based on a given history of messages.
 * @param history of (consumed and produced) messages
 * @param flow definition
 * @return instance summarizing the state of a specific flow or null if history is empty
 */
fun <I: Aggregate> past(history: Messages, flow: FlowExecution<I>): I? {

    fun <I: Aggregate> past(history: Messages, aggregate: I): I? {
        if (!history.isEmpty()) {
            val message = history.first()
            val method = aggregate::class.memberFunctions.find { it.parameters.size == 2 && it.parameters[1].type.classifier as KClass<*> == message::class }
            if (method != null) method.call(aggregate, message)
            return past(history.subList(1, history.size), aggregate)
        } else {
            return aggregate
        }
    }

    if (!history.isEmpty()) {
        val message = history.first()
        val constructor = flow.asDefinition()
            .aggregateType.constructors.find { it.parameters.size == 1 && it.parameters[0].type.classifier as KClass<*> == message::class }
        return if (constructor != null) {
            past(history.subList(1, history.size), constructor.call(message) as I)
        } else throw IllegalArgumentException()
    } else {
        return null
    }

}

/**
 * Produce new "present" messages based on a given history of messages and a trigger.
 * @param history of (consumed and produced) messages
 * @param flow definition
 * @param trigger coming in and influencing the flow instance
 * @return new messages produced
 */
fun <I: Aggregate> present(history: Messages, flow: FlowExecution<I>, trigger: Message): Messages {
    TODO()
}

/**
 * Produce a list of future matching patternBuilders matching in the future based on a given history of messages and a trigger.
 * @param history of (consumed and produced) messages
 * @param flow definition
 * @param trigger coming in and influencing the flow instance
 * @return future matching patternBuilders
 */
fun <I: Aggregate> future(history: Messages, flow: FlowExecution<I>, trigger: Message): MessagePatterns {
    TODO()
}

/**
 * Produce a list of potential patternBuilders for an incoming message.
 * @param flow definition
 * @param trigger coming in and potentially influencing many flow instances
 * @return matching patternBuilders
 */
fun <I: Aggregate> potential(flow: FlowExecution<I>, trigger: Message): MessagePatterns {
    TODO()
}

/**
 * Determine a list of flow instances currently matching to given patterns.
 * @param patterns
 * @return matching flow instances
 */
fun <I: Aggregate> determine(patterns: MessagePatterns): AggregateIds {
    TODO()
}

/*

interface ExecutableFlowDefinition: FlowDefinition {

    val instances: FlowInstances

    fun patterns(message: Message): List<MessagePattern>

}

interface FlowDefinitions {

    fun all(): List<ExecutableFlowDefinition>
    fun get(id: FlowElementType): ExecutableFlowDefinition
    // fun add(definition: ExecutableFlowDefinition): List<ExecutableFlowDefinition>

}

interface FlowInstances {

    fun get(id: AggregateId): Aggregate
    fun find(pattern: MessagePattern): AggregateIds

}

interface FlowMessageCorrelator {

    val definitions: FlowDefinitions

    fun process(incoming: MessageContainer) {

        if (incoming.target == null) {

            // Implicit targeting means that all correlations will happen synchronously
            target(incoming).forEach { target -> process(MessageContainer(incoming, target)) }

        } else {

            // Begin transaction for message handling
            // (A flow definition specific transaction mechanism may be provided)

            // -. Retrieve flow definition by means of flow definition id.
            val flowDefinition = definitions.get(incoming.target!!.first)

            // -. Load flow instance history by means of the target flow instance id.
            //    (A flow definition specific loading mechanism may be provided)
            val flowInstance = flowDefinition.instances.get(incoming.target!!.second)

            // -. Reconstruct flow instance status by means of flow instance history.
            //    (A flow definition specific reconstruction mechanism may be provided)

            // -. Correlate message to flow instance and retrieve outgoing messages.
            //    (A flow definition specific correlation mechanism may be provided)

            // -. Append incoming message and outgoing reaction messages to store.
            //    (A flow definition specific appending mechanism may be provided)
            // -. Commit unit of work for message correlation phase in case everything goes smoothly
            //    If temporary failure, feed message into incoming queue and configure retry
            //    If permanent failure, feed message into error queue

            // -. Commit unit of work for message handling in case everything goes smoothly
            //    (A flow definition specific transaction mechanism may be provided)

        }

    }

    fun target(message: MessageContainer) : List<MessageTarget> {

        return definitions.all().map { definition ->

            // Retrieve message correlation keys by means of (all) flow definitions and incoming
            // message. Lookup correlating flow instance ids by means of message correlation keys.
            // (A flow definition specific lookup mechanism may be provided)

            definition.patterns(message).map {
                definition.instances.find(it)
            }.flatten().map {
                Pair(definition.flowElementType, it)
            }

        }.flatten()

    }

    fun correlate(incoming: MessageContainer): Messages {
        TODO()
    }

}

*/