package org.factscript.execution

import org.factscript.language.impl.utils.Json
import org.factscript.language.impl.utils.Id

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Fact<F: Any> (

    val id: String,
    val type: Type,
    val details: F

) {

    constructor(fact: F): this(Id(), fact::class.type, fact)

    companion object {

        fun fromJson(json: String): Fact<*> {
            return fromJson(Json(json))
        }

        internal fun fromJson(json: Json): Fact<*> {
            return json.toObject(Fact::class, json.getObject<Type>("type")!!.kClass)
        }

    }

}
