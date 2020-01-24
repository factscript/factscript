package io.factdriven.view.payment1

import io.factdriven.def.Definition
import io.factdriven.view.render
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    @Test
    fun testDefinition() {

        render(Definition.getDefinitionByType(PaymentRetrieval::class))

    }

}