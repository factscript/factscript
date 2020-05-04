package io.factdriven.language.definition

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Flow: Node, Reporting

interface ConditionalFlow: Flow, Conditional

interface AwaitingFlow: Flow, ConsumingEvent
