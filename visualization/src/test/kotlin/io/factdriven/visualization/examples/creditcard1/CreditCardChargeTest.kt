package io.factdriven.visualization.examples.creditcard1

import io.factdriven.definition.Definition
import io.factdriven.definition.Definitions
import io.factdriven.visualization.render
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CreditCardChargeTest {

    @Test
    fun testDefinition() {

        render(Definitions.getDefinitionByType(CreditCardCharge::class))

    }

}