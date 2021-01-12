package org.factscript.language.execution.cam

import org.factscript.execution.Messages
import org.factscript.execution.Message
import org.factscript.execution.MessageStore
import org.factscript.language.impl.utils.Json
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.ProcessEngines
import org.camunda.spin.impl.json.jackson.JacksonJsonNode

class EngineMessageStore: MessageStore {

    private val engine: ProcessEngine get() = ProcessEngines.getProcessEngines().values.first()

    override fun load(id: String): List<Message> {
        val processInstance =
            engine.historyService.createHistoricProcessInstanceQuery()
                .processInstanceBusinessKey(id)
                .singleResult()
        val messages = engine.historyService.createHistoricVariableInstanceQuery()
            .processInstanceId(processInstance.id)
            .variableName(MESSAGES_VAR)
            .disableCustomObjectDeserialization()
            .singleResult().value as JacksonJsonNode?
        return messages?.let {
            Messages.fromJson(
                Json(
                    messages.unwrap()
                )
            )
        } ?: throw IllegalArgumentException()
    }

}