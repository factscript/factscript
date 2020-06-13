package io.factdriven.language.execution.aws.event.repository

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.factdriven.execution.Message
import io.factdriven.language.execution.aws.event.EventReactionType
import io.factdriven.language.impl.utils.compactJson
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class EventRepository {

    private fun connect(){
        val url = System.getenv("AWS_FLOW_RDS_URL")
        val driver = System.getenv("AWS_FLOW_RDS_DRIVER")
        val user = System.getenv("AWS_FLOW_RDS_USER")
        val password = System.getenv("AWS_FLOW_RDS_PASSWORD")

        Database.connect(url, driver, user, password)
    }

    fun save(eventName: String, taskToken: String, reactionType: EventReactionType, reference: String, stateMachineArn: String, messageList: List<Message>, errorCode : String? = null){
        connect()
        transaction {
            SchemaUtils.createMissingTablesAndColumns(EventEntityDefinition)

            EventEntityDefinition.insert {
                it[EventEntityDefinition.name] = eventName
                it[EventEntityDefinition.taskToken] = taskToken
                it[EventEntityDefinition.reference] = reference
                it[EventEntityDefinition.stateMachineArn] = stateMachineArn
                it[EventEntityDefinition.createdOn] = LocalDateTime.now()
                it[EventEntityDefinition.reactionType] = reactionType.toString()
                it[EventEntityDefinition.messageHistory] = ExposedBlob(messageList.compactJson.toByteArray())
                if(errorCode != null) {
                    it[EventEntityDefinition.errorCode] = errorCode
                }
            }
        }
    }


    fun getEventsByNameAndReference(eventName: String, eventReferenceValue: String): List<EventEntity> {
        connect()

        var taskTokens : List<EventEntity> = emptyList()

        transaction {
             taskTokens = EventEntityDefinition
                     .select {
                         (EventEntityDefinition.reference eq eventReferenceValue) and (EventEntityDefinition.name eq eventName)
                         }
                    .map {
                        val clob = String(it[EventEntityDefinition.messageHistory].bytes)
                        val messageList = jacksonObjectMapper()
                                .readValue<List<Message>>(clob, object : TypeReference<List<Message>>() {})
                        EventEntity(it[EventEntityDefinition.taskToken],
                                messageList,
                                it[EventEntityDefinition.reference],
                                EventReactionType.valueOf(it[EventEntityDefinition.reactionType]),
                                it[EventEntityDefinition.errorCode]
                        )
                    }
                    .toCollection(arrayListOf())
        }
        return taskTokens
    }

    fun deleteTaskTokens(taskTokens: List<String>){
        connect()
        transaction {
            taskTokens.forEach {
                EventEntityDefinition.deleteWhere { EventEntityDefinition.taskToken eq it }
            }
        }
    }

}