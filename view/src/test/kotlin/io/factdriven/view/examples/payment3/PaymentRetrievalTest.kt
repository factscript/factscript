package io.factdriven.view.examples.payment3

import io.factdriven.def.Definition
import io.factdriven.view.render
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    @Test
    fun testView() {

        Definition.init(CreditCardCharge::class)
        render(Definition.getDefinitionByType(PaymentRetrieval::class))

    }

}