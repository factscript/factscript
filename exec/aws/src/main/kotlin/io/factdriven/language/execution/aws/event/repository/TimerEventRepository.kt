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

class TimerEventRepository {

    private fun connect(){
        val url = System.getenv("AWS_FLOW_RDS_URL")
        val driver = System.getenv("AWS_FLOW_RDS_DRIVER")
        val user = System.getenv("AWS_FLOW_RDS_USER")
        val password = System.getenv("AWS_FLOW_RDS_PASSWORD")

        Database.connect(url, driver, user, password)
    }

    fun save(taskToken: String, reactionType: EventReactionType, dateTime: LocalDateTime, stateMachineArn: String, messageList: List<Message>, errorCode : String? = null){
        connect()
        transaction {
            SchemaUtils.createMissingTablesAndColumns(TimerEventEntityDefinition)

            TimerEventEntityDefinition.insert {
                it[TimerEventEntityDefinition.taskToken] = taskToken
                it[TimerEventEntityDefinition.dateTime] = dateTime
                it[TimerEventEntityDefinition.stateMachineArn] = stateMachineArn
                it[TimerEventEntityDefinition.createdOn] = LocalDateTime.now()
                it[TimerEventEntityDefinition.reactionType] = reactionType.toString()
                it[TimerEventEntityDefinition.messageHistory] = ExposedBlob(messageList.compactJson.toByteArray())
                if(errorCode != null) {
                    it[TimerEventEntityDefinition.errorCode] = errorCode
                }
            }
        }
    }

    fun getDueTimerEvents(): List<TimerEventEntity> {
        connect()

        var taskTokens : List<TimerEventEntity> = emptyList()

        transaction {
             taskTokens = TimerEventEntityDefinition
                     .select {
                         (TimerEventEntityDefinition.dateTime lessEq LocalDateTime.now())
                         }
                    .map {
                        val clob = String(it[TimerEventEntityDefinition.messageHistory].bytes)
                        val messageList = jacksonObjectMapper()
                                .readValue<List<Message>>(clob, object : TypeReference<List<Message>>() {})
                        TimerEventEntity(it[TimerEventEntityDefinition.taskToken],
                                messageList,
                                it[TimerEventEntityDefinition.dateTime],
                                EventReactionType.valueOf(it[TimerEventEntityDefinition.reactionType]),
                                it[TimerEventEntityDefinition.errorCode]
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
                TimerEventEntityDefinition.deleteWhere { TimerEventEntityDefinition.taskToken eq it }
            }
        }
    }

}