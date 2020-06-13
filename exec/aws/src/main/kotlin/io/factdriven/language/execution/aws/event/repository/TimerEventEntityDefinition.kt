package io.factdriven.language.execution.aws.event.repository

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime

object TimerEventEntityDefinition : Table("FLOW_TIMER_EVENTS"){
    val id = integer("TIMER_EVENT_ID").autoIncrement()
    val dateTime = datetime("DATE_TIME")
    val reactionType = varchar("REACTION_TYPE", length = 20)
    val taskToken = varchar("TASK_TOKEN", length = 1024)
    val stateMachineArn = varchar("STATE_MACHINE_ARN", length = 255)
    val createdOn = datetime("CREATED_ON")
    val messageHistory = blob("MESSAGE_HISTORY")

    override val primaryKey = PrimaryKey(id, name = "PK_TIMER_EVENT_ID")
}
