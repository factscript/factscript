package io.factdriven.flowlang

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Call {

    infix fun service(service: Service.() -> Unit): Service = Service().apply(service)
    infix fun receive(receive: Receive.() -> Unit): Receive = Receive().apply(receive)

}

class Service
class Receive
