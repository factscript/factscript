package org.factscript.language

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Labeled<L> {

    operator fun invoke(case: String): L

}
