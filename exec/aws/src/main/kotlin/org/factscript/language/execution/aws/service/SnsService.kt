package org.factscript.language.execution.aws.service

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.sns.model.AddPermissionRequest
import com.amazonaws.services.sns.model.CreateTopicRequest

class SnsService {
    fun createClient() : AmazonSNS {
        return AmazonSNSClient.builder()
                .withRegion("eu-central-1")
                .withCredentials(DefaultAWSCredentialsProviderChain())
                .build()
    }

    fun publishMessage(topicArn: String, subject: String, message: String) {
        createClient().publish(topicArn, message, subject)
    }

    fun createTopics(topics: List<String>) {
        val client = createClient()
        for (topic in topics) {
            client.createTopic(CreateTopicRequest(topic))
        }
    }

    fun subscribeTopics(lambdaArn: String, topicArns: List<String>) {
        val client = createClient()
        for (topicArn in topicArns) {
            client.subscribe(topicArn, "lambda", lambdaArn)
        }
    }
}