package io.factdriven.language.definition

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface ConsumingEvent: Consuming, Reporting {

    val properties: List<String>
    val matching: List<Any.() -> Any?>

}
