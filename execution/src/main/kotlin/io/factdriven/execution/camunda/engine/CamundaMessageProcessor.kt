package io.factdriven.execution.camunda.engine

import io.factdriven.Flows
import io.factdriven.Messages
import io.factdriven.execution.*
import io.factdriven.impl.definition.idSeparator
import io.factdriven.impl.utils.json
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.ProcessEngines
import org.camunda.bpm.engine.impl.event.EventType
import org.camunda.spin.plugin.variable.SpinValues

class CamundaMessageProcessor: MessageProcessor {

    private val engine: ProcessEngine get() = ProcessEngines.getProcessEngines().values.first()

    override fun process(message: Message) {
        if (message.receiver != null) {
            handle(message)
        } else {
            route(message)
        }
    }

    private fun route(message: Message) {

        val messages = Flows.handling(message).map { handling ->

            fun messagesHandledByExternalTasks() : List<Message> {
                val externalTasksHandlingMessage =  engine.externalTaskService
                    .fetchAndLock(Int.MAX_VALUE, handling.hash)
                    .topic(handling.hash, Long.MAX_VALUE)
                    .execute()
                return externalTasksHandlingMessage.map { task ->
                    Message(
                        message, Receiver(
                            EntityId(
                                Type.from(task.processDefinitionKey),
                                task.businessKey
                            ), handling
                        )
                    )
                }
            }

            fun messagesHandledByEventSubscriptions() : List<Message> {

                val eventSubscriptionsHandlingMessage = engine.runtimeService.createEventSubscriptionQuery()
                    .eventType(EventType.MESSAGE.name())
                    .eventName(handling.hash)
                    .list()

                val businessKeysOfRunningProcessInstances = eventSubscriptionsHandlingMessage.map { subscription ->
                    subscription.processInstanceId?.let {
                        engine.runtimeService.createProcessInstanceQuery()
                            .processInstanceId(subscription.processInstanceId)
                            .singleResult().businessKey
                    }
                }

                return eventSubscriptionsHandlingMessage.mapIndexed { index, subscription ->
                    val processDefinitionKey = subscription.activityId.split(idSeparator)
                    Message(
                        message, Receiver(
                            EntityId(
                                Type(
                                    processDefinitionKey[0],
                                    processDefinitionKey[1]
                                ), businessKeysOfRunningProcessInstances[index]
                            ), handling
                        )
                    )
                }

            }

            listOf(messagesHandledByExternalTasks(), messagesHandledByEventSubscriptions()).flatten()

        }.flatten()

        messages.forEach {
            Messages.publish(it)
        }

    }

    private fun handle(message: Message) {

        val handler = message.receiver?.entity
            ?: throw IllegalArgumentException("Messages not (yet) routed to receiver!")

        val processInstanceId = handler.id?.let {
            engine.runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(handler.id)
                .singleResult()
                ?.id
        }

        fun variables(): Map<String, Any> {

            val messages = Messages.load(handler.id)

            with(messages.toMutableList()) {
                add(message)
                return mapOf(
                    MESSAGES_VAR to SpinValues.jsonValue(
                        json
                    ).create()
                )
            }

        }

        if (processInstanceId != null) {

            val externalTask = engine.externalTaskService
                .createExternalTaskQuery()
                .processInstanceId(processInstanceId)
                .topicName(message.receiver!!.receptor.hash)
                .singleResult()

            if (externalTask != null) {
                engine.externalTaskService.complete(
                    externalTask.id,
                    message.receiver!!.receptor.hash,
                    variables()
                )
                return
            }

        }

        val correlationBuilder = engine.runtimeService
            .createMessageCorrelation(message.receiver!!.receptor.hash)
            .setVariables(variables())
        if (processInstanceId != null) {
            correlationBuilder.processInstanceId(processInstanceId)
        } else {
            correlationBuilder.processInstanceBusinessKey(message.fact.id)
        }
        correlationBuilder.correlate()

    }

}