package io.factdriven.view.payment1

import io.factdriven.lang.Flow
import io.factdriven.view.render
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    @Test
    fun testDefinition() {

        render(Flow.get(PaymentRetrieval::class))

    }

}