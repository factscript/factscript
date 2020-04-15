package io.factdriven.impl.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Json(val node: JsonNode) {

    constructor(json: String): this(mapper.readTree(json))
    constructor(any: Any): this(mapper.valueToTree<JsonNode>(any))

    inline fun <reified P: Any> getObject(property: String, kClass: KClass<P> = P::class): P? {
        return mapper.readValue(mapper.treeAsTokens(node.get(property)), kClass.java)
    }

    fun getNode(path: String): Json {
        return Json(node.path(path))
    }

    inline fun <reified O: Any> toObject(kClass: KClass<O> = O::class, vararg subTypes: KClass<*>): O {
        val mapper = mapper
        val parameters = subTypes.map { it.java }.toTypedArray()
        mapper.registerSubtypes(*parameters)
        val javaType = mapper.typeFactory.constructParametricType(kClass.java, *parameters)
        return mapper.readValue(mapper.treeAsTokens(node), javaType)
    }

    fun asList(): List<Json> {
        return node.map { Json(it) }
    }

    override fun toString(): String {
        return writer.writeValueAsString(node)
    }

    companion object {

        val mapper get() = jacksonObjectMapper()
        private val writer: ObjectWriter get() = mapper.writerWithDefaultPrettyPrinter()

    }

}

val Any.json: String get() {
    return Json(this).toString()
}
val Any.compactJson: String get(){
    return jacksonObjectMapper().writeValueAsString(this)
}