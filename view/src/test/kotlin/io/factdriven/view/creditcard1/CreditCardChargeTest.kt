package io.factdriven.language.examples.creditcard1

import io.factdriven.language.Flow
import io.factdriven.view.render
import org.junit.jupiter.api.Assertions
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