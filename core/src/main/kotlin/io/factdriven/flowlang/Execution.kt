package io.factdriven.flowlang

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Execution {

    infix fun service(service: Service.() -> Unit): Service = TODO()
    infix fun receive(receive: Receive.() -> Unit): Receive = TODO()

}

class Service
class Receive
