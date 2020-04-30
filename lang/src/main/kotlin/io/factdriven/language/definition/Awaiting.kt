package io.factdriven.language.definition

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Awaiting: Catching, Reporting {

    val properties: List<String>
    val matching: List<Any.() -> Any?>

}
