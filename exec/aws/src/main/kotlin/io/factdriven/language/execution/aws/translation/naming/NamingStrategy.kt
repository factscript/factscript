package io.factdriven.language.execution.aws.translation.naming

import io.factdriven.language.definition.Node

interface NamingStrategy {
    fun getName(node: Node) : String
    fun getName(prefix: String, node: Node) : String
}

class StatefulNamingStrategy : NamingStrategy {
    private val usedNames = HashMap<String, Int>()
    private val nodeLookup = HashMap<String, String>()

    override fun getName(prefix: String, node: Node): String {
        val lookupKey = getLookupKey(prefix, node)
        if(nodeLookup.containsKey(lookupKey)){
            return nodeLookup[lookupKey]!!
        }
        val collisionFreeName = getCollisionFreeName(toStateName(prefix, node))
        nodeLookup[getLookupKey(prefix, node)] = collisionFreeName
        return collisionFreeName
    }

    override fun getName(node: Node): String {
        val lookupKey = getLookupKey(null, node)
        if(nodeLookup.containsKey(lookupKey)){
            return nodeLookup[lookupKey]!!
        }
        val collisionFreeName = getCollisionFreeName(toStateName(node))
        nodeLookup[getLookupKey(null, node)] = collisionFreeName
        return collisionFreeName
    }

    private fun getLookupKey(prefix: String?, node: Node): String {
        if(prefix != null){
            return "$prefix-${node.id}"
        }
        return node.id
    }

    private fun getCollisionFreeName(name: String) : String{
        if(usedNames.containsKey(name)){
            val increment = usedNames[name]!!.plus(1)
            val collisionFreeName = "$name $increment"
            usedNames[name] = increment
            return collisionFreeName
        }
        usedNames[name] = 0
        return name
    }

    private fun toStateName(prefix: String?, node: Node) : String{
        if(prefix != null){
            if(node.description.isBlank()){
                return prefix
            }
            return "$prefix-${node.description}"
        }
        if(node.description.isBlank()){
            return node.id
        }
        return node.description
    }

    private fun toStateName(node: Node) : String{
        return toStateName(null, node)
    }

}