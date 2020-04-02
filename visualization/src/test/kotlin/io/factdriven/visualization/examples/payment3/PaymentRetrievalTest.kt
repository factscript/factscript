package io.factdriven.visualization.examples.payment3

import io.factdriven.definition.Definition
import io.factdriven.definition.Definitions
import io.factdriven.visualization.render
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    @Test
    fun testView() {

        Definitions.init(CreditCardCharge::class)
        render(Definitions.getDefinitionByType(PaymentRetrieval::class))

    }

}