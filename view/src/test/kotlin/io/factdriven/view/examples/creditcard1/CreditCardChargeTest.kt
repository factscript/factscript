package io.factdriven.view.examples.creditcard1

import io.factdriven.def.Definition
import io.factdriven.view.render
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