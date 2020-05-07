package io.factdriven.language.definition

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Flow: Node, Continuing

interface OptionalFlow: Flow, Optional

interface CatchingFlow: Flow, Catching

interface ConsumingFlow: CatchingFlow, Consuming

interface CorrelatingFlow: ConsumingFlow, Correlating

interface PromisingFlow: CatchingFlow, Promising

interface WaitingFlow: CatchingFlow, Waiting

interface LoopingFlow: Flow, Looping