package io.factdriven.flow

import io.factdriven.flow.lang.*
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.full.memberFunctions

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

/**
 * Produce new "present" messages based on a given history of messages and a trigger.
 * @param history of (consumed and produced) messages
 * @param flow definition
 * @param trigger coming in and influencing the flow instance
 * @return new messages produced
 */
fun <I: Aggregate> present(history: Messages, flow: FlowExecution<I>, trigger: Fact): Messages {
    TODO()
}

/**
 * Produce a list of future matching patternBuilders matching in the future based on a given history of messages and a trigger.
 * @param history of (consumed and produced) messages
 * @param flow definition
 * @param trigger coming in and influencing the flow instance
 * @return future matching patternBuilders
 */
fun <I: Aggregate> future(history: Messages, flow: FlowExecution<I>, trigger: Fact): MessagePatterns {
    TODO()
}

/**
 * Produce a list of potential patternBuilders for an incoming data.
 * @param flow definition
 * @param trigger coming in and potentially influencing many flow instances
 * @return matching patternBuilders
 */
fun <I: Aggregate> potential(flow: FlowExecution<I>, trigger: Fact): MessagePatterns {
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

    fun patterns(data: Message): List<MessagePattern>

}

interface FlowDefinitions {

    fun all(): List<ExecutableFlowDefinition>
    fun get(id: ElementName): ExecutableFlowDefinition
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

            // Begin transaction for data handling
            // (A flow definition specific transaction mechanism may be provided)

            // -. Retrieve flow definition by means of flow definition id.
            val flowDefinition = definitions.get(incoming.target!!.first)

            // -. Load flow instance history by means of the target flow instance id.
            //    (A flow definition specific loading mechanism may be provided)
            val flowInstance = flowDefinition.instances.get(incoming.target!!.second)

            // -. Reconstruct flow instance status by means of flow instance history.
            //    (A flow definition specific reconstruction mechanism may be provided)

            // -. Correlate data to flow instance and retrieve outgoing messages.
            //    (A flow definition specific correlation mechanism may be provided)

            // -. Append incoming data and outgoing reaction messages to store.
            //    (A flow definition specific appending mechanism may be provided)
            // -. Commit unit of work for data correlation phase in case everything goes smoothly
            //    If temporary failure, feed data into incoming queue and configure retry
            //    If permanent failure, feed data into error queue

            // -. Commit unit of work for data handling in case everything goes smoothly
            //    (A flow definition specific transaction mechanism may be provided)

        }

    }

    fun target(data: MessageContainer) : List<MessageTarget> {

        return definitions.all().map { definition ->

            // Retrieve data correlation keys by means of (all) flow definitions and incoming
            // data. Lookup correlating flow instance ids by means of data correlation keys.
            // (A flow definition specific lookup mechanism may be provided)

            definition.patterns(data).map {
                definition.instances.find(it)
            }.flatten().map {
                Pair(definition.name, it)
            }

        }.flatten()

    }

    fun correlate(incoming: MessageContainer): Messages {
        TODO()
    }

}

*/