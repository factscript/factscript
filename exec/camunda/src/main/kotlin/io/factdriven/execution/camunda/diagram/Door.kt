package io.factdriven.execution.camunda.diagram

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Door {

    val box: Space
    val position: Position
    val direction: Direction

}