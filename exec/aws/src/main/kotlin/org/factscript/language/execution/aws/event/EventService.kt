package org.factscript.language.execution.aws.event

import com.amazonaws.services.stepfunctions.model.SendTaskFailureRequest
import com.amazonaws.services.stepfunctions.model.SendTaskSuccessRequest
import com.amazonaws.services.stepfunctions.model.TaskTimedOutException
import org.factscript.execution.Fact
import org.factscript.execution.Message
import org.factscript.language.definition.Correlating
import org.factscript.language.definition.Waiting
import org.factscript.language.execution.aws.event.repository.EventRepository
import org.factscript.language.execution.aws.event.repository.TimerEventRepository
import org.factscript.language.execution.aws.service.StateMachineService
import org.factscript.language.execution.aws.lambda.EventContext
import org.factscript.language.execution.aws.lambda.ProcessContext
import org.factscript.language.execution.aws.lambda.hashString
import org.factscript.language.impl.utils.compactJson
import java.time.Duration
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

class EventService {

    val eventRepository = EventRepository()
    val timerEventRepository = TimerEventRepository()
    val stateMachineService = StateMachineService()

    fun registerEvent(processContext: ProcessContext, node: Correlating) {
        eventRepository.save(node.consuming.qualifiedName!!,
                processContext.token,
                EventReactionType.SUCCESS,
                extractCorrelationValue(processContext, node),
                processContext.stateMachine.name,
                processContext.messageList)
    }

    fun registerErrorEvent(processContext: ProcessContext, node: Correlating) {
        eventRepository.save(node.consuming.qualifiedName!!,
                processContext.token,
                EventReactionType.ERROR,
                extractCorrelationValue(processContext, node),
                processContext.stateMachine.name,
                processContext.messageList,
                node.consuming.simpleName
        )
    }

    private fun extractCorrelationValue(processContext: ProcessContext, node: Correlating): String {
        val references = node.correlating.entries.
                map { e -> e.key to  e.value.invoke(processContext.processInstance).toString()}
                .toMap()
        return hashReference(references)
    }

    fun correlateEvent(eventContext: EventContext){
        val correlating = eventContext.node as Correlating
        val fact = eventContext.event

        correlateEvent(correlating, fact)
    }

    fun correlateEvent(correlating: Correlating, fact: Any){

        val eventReferenceValue = getEventReferenceValue(fact, correlating)
        val eventEntities = eventRepository.getEventsByNameAndReference(correlating.consuming.qualifiedName!!, eventReferenceValue)
        val message = Message(fact.javaClass::class, Fact(fact))

        for(eventEntity in eventEntities){
            val messageList = eventEntity.messages
            val output = (messageList.plus(message).map { m -> m.compactJson }).compactJson
            correlateToken(eventEntity.token, eventEntity.reactionType, output, eventEntity.errorCode)
        }

        val tokenList = eventEntities.map { t -> t.token }
        eventRepository.deleteTaskTokens(tokenList)
        timerEventRepository.deleteTaskTokens(tokenList)
    }

    private fun getEventReferenceValue(fact: Any, node: Correlating): String {
        val key = node.correlating.entries.map { e -> e.key }.toList()
        val references = fact::class.memberProperties
                .filter {r -> key.contains(r.name)}
                .map { r -> r.name to r.getter.call(fact).toString() }
                .toMap()

        return hashReference(references)
    }

    private fun hashReference(references: Map<String, String>) : String{
        val map = references.toSortedMap()
        return hashString(map.toString())
    }

    fun registerTimerEvent(processContext: ProcessContext, waiting: Waiting){
        val limit = waiting.limit?.invoke(processContext.processInstance)
        val duration = waiting.period?.invoke(processContext.processInstance)
        val dateTime : LocalDateTime = limit ?: LocalDateTime.now().plus(Duration.parse(duration))

        timerEventRepository.save(processContext.token, EventReactionType.SUCCESS, dateTime, processContext.stateMachine.name, processContext.messageList)
    }

    fun registerTimerErrorEvent(processContext: ProcessContext, waiting: Waiting) {
        val limit = waiting.limit?.invoke(processContext.processInstance)
        val duration = waiting.period?.invoke(processContext.processInstance)
        val dateTime : LocalDateTime = limit ?: LocalDateTime.now().plus(Duration.parse(duration))

        timerEventRepository.save(processContext.token,
                EventReactionType.ERROR,
                dateTime,
                processContext.stateMachine.name,
                processContext.messageList,
                "waiting")
    }

    fun correlateTimerEvents(){
        val timerEvents = timerEventRepository.getDueTimerEvents()

        timerEvents.forEach { timerEvent ->
            val messageList = timerEvent.messages
            correlateToken(timerEvent.token, timerEvent.reactionType, (messageList.map { m -> m.compactJson }).compactJson, timerEvent.errorCode)
        }
        val tokenList = timerEvents.map { timerEvent -> timerEvent.token }.toList()
        timerEventRepository.deleteTaskTokens(tokenList)
        eventRepository.deleteTaskTokens(tokenList)
    }

    private fun correlateToken(taskToken : String, reactionType: EventReactionType, output : String, errorCodeIfError: String? = null){
        val client = stateMachineService.createClient()
        when(reactionType){
            EventReactionType.SUCCESS -> {
                val sendTaskSuccessRequest = SendTaskSuccessRequest()
                        .withTaskToken(taskToken)
                        .withOutput(output)
                try {
                    client.sendTaskSuccess(sendTaskSuccessRequest)
                } catch (e : TaskTimedOutException){
                    // ignore
                }
            }
            EventReactionType.ERROR -> {
                val sendTaskFailureRequest = SendTaskFailureRequest()
                        .withTaskToken(taskToken)
                        .withCause(output)
                        .withError(errorCodeIfError)
                try {
                    client.sendTaskFailure(sendTaskFailureRequest)
                } catch (e : TaskTimedOutException){
                    // ignore
                }
            }
            else -> {
                throw ReactionTypeNotSupportedException("this event reaction type has no correlation method")
            }
        }
    }




}