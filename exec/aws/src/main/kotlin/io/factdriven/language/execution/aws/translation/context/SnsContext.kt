package io.factdriven.language.execution.aws.translation.context

import io.factdriven.language.definition.Consuming
import io.factdriven.language.definition.Node
import io.factdriven.language.definition.Throwing

class SnsContext(val namespace: String, val topics : MutableList<Topic> = mutableListOf(), val resource : String = "arn:aws:states:::sns:publish"){
    companion object{
        fun fromLambdaArn(lambdaArn: String) : SnsContext {
            val regionAndUser = lambdaArn
                    .replace("arn:aws:lambda:", "")
                    .replace(Regex(":function.*"), "")
            return SnsContext("arn:aws:sns:$regionAndUser:")
        }
    }
    class Topic (val name: String, val type : Type) {
        enum class Type {
            Throwing, Consuming
        }
    }

    fun getTopicArn(name : String) : String{
        return "$namespace$name"
    }

    fun getTopicArn(node: Node) : String {
        return if(node is Consuming){
            getConsumingArnTopic(node)
        } else {
            getThrowingArnTopic(node as Throwing)
        }
    }

    private fun getThrowingArnTopic(throwing: Throwing) : String {
        return getTopicArn(throwing.throwing.simpleName!!)
    }

    private fun getConsumingArnTopic(consuming: Consuming) : String {
        return getTopicArn(consuming.consuming.simpleName!!)
    }

    fun addTopic(node : Node){
        if(node is Throwing){
            topics.add(Topic(node.throwing.simpleName!!, Topic.Type.Throwing))
        } else if (node is Consuming) {
            topics.add(Topic(node.consuming.simpleName!!, Topic.Type.Consuming))
        }
    }

    fun getSubscriptionTopicArns(): List<String> {
        return topics
                .filter { topic -> topic.type == Topic.Type.Consuming }
                .map { topic -> getTopicArn(topic.name) }
                .toList()
    }

    fun getPublishTopicArns(): List<String> {
        return topics
                .filter { topic -> topic.type == Topic.Type.Throwing }
                .map { topic -> getTopicArn(topic.name) }
                .toList()
    }

    fun getAllTopicArns() : List<String> {
        return topics
                .map { topic -> getTopicArn(topic.name) }
                .toList()
    }

    fun getAllTopicNames() : List<String> {
        return topics
                .map { topic -> topic.name}
                .toList()
    }
}