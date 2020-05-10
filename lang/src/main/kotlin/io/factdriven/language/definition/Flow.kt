package io.factdriven.language.definition

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Flow: Node, Continuing

interface ConditionalFlow: Flow, ConditionalNode {

    val conditional: ConditionalNode get() = children.first() as ConditionalNode

}

interface OptionalFlow: ConditionalFlow, OptionalNode

interface CatchingFlow: Flow, Catching

interface ConsumingFlow: CatchingFlow, Consuming

interface CorrelatingFlow: ConsumingFlow, Correlating

interface PromisingFlow: CatchingFlow, Promising

interface WaitingFlow: CatchingFlow, Waiting

interface LoopingFlow: ConditionalFlow, LoopingNode {

    override val conditional: ConditionalNode get() = children.last() as ConditionalNode

}