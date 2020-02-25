package io.factdriven.visualization.examples.creditcard1

import io.factdriven.definition.Definition
import io.factdriven.visualization.render
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CreditCardChargeTest {

    @Test
    fun testDefinition() {

        render(Definition.getDefinitionByType(CreditCardCharge::class))

    }

}