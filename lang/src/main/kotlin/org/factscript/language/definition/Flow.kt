package org.factscript.language.definition

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Flow: Node, Continuing

interface ConditionalFlow: Flow, Conditional {

    val conditional: ConditionalNode get() = children.first() as ConditionalNode

}

interface OptionalFlow: ConditionalFlow, Optional

interface CatchingFlow: Flow, Catching

interface ConsumingFlow: CatchingFlow, Consuming

interface CorrelatingFlow: ConsumingFlow, Correlating

interface PromisingFlow: CatchingFlow, Promising

interface WaitingFlow: CatchingFlow, Waiting

interface RepeatingFlow: ConditionalFlow, Looping {

    override val conditional: ConditionalNode get() = children.last() as ConditionalNode

}