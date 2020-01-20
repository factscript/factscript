package io.factdriven.view.creditcard1

import io.factdriven.lang.Flow
import io.factdriven.view.render
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CreditCardChargeTest {

    @Test
    fun testDefinition() {

        render(Flow.get(CreditCardCharge::class))

    }

}