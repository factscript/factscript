package io.factdriven.language.execution.aws.event.repository

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime

object EventEntityDefinition : Table("FLOW_EVENTS"){
    val id = integer("EVENT_ID").autoIncrement()
    val name = varchar("NAME", length = 255)
    val reactionType = varchar("REACTION_TYPE", length = 20)
    val taskToken = varchar("TASK_TOKEN", length = 1024)
    val reference = varchar("REFERENCE", length = 64)
    val stateMachineArn = varchar("STATE_MACHINE_ARN", length = 255)
    val createdOn = datetime("CREATED_ON")
    val messageHistory = blob("MESSAGE_HISTORY")

    override val primaryKey = PrimaryKey(id, name = "PK_EVENT_ID")
}
