package io.factdriven.traverse

import io.factdriven.definition.api.Executing
import io.factdriven.language.Given
import io.factdriven.language.Select
import java.util.*

class SequentialNodeTraverser (executing: Executing){
    val startingNode = executing
    val traverseHistory: ArrayList<Traverse> = ArrayList()

    fun fullTraverse() : List<Traverse>{
        fullTraverse(startingNode)
        traverseHistory.last().end = true
        return traverseHistory
    }

    private fun fullTraverse(executing: Executing){
        if(isBlockStructureInitiator(executing)){
            blockStructureTraverse(executing)
        } else {
            nodeSequenceTraverse(executing)
        }
    }

    private fun isBlockStructureInitiator(executing: Executing): Boolean {
        return executing is Select <*>
    }

    private fun blockStructureTraverse(executing: Executing) {
        val blockTraverse = BlockTraverse(executing)
        for (condition in executing.children) {
            val pathHistory = SequentialNodeTraverser(condition).fullTraverse()
            val given = findGiven(pathHistory)
            val traverse = PathTraverse(block = blockTraverse, initiator = executing, path = pathHistory, addition = given)

            blockTraverse.paths.add(traverse)
        }
        addLinks(blockTraverse)
        traverseHistory.add(blockTraverse)
    }

    private fun findGiven(pathHistory: List<Traverse>): Executing? {
        for (traverse in pathHistory) {
            if(traverse is NodeTraverse && traverse.current is Given<*>){
                return traverse.current
            }
        }
        return null
    }

    private fun nodeSequenceTraverse(executing: Executing) {
        val children = executing.children
        for (child in children) {
            if(isBlockStructureInitiator(child)){
                fullTraverse(child)
                continue
            }

            val start: Boolean = traverseHistory.isEmpty()
            val next = determineNext(child, children)
            val traverse: Traverse = NodeTraverse(child, next)

            traverse.start = start

            addLinks(traverse)

            traverseHistory.add(traverse)
        }
    }

    private fun addLinks(traverse: Traverse) {
        if (traverseHistory.isNotEmpty()) {
            var last = traverseHistory.last()
            last.nextTraverse = traverse
            traverse.previousTraverse = traverseHistory.last()

            if(last is BlockTraverse){
                last.nextTraverse = last.paths.first()
                last.paths.first().previousTraverse = last
                last.paths.last().nextTraverse = traverse

                addPathLinks(last, traverse)
            }
        }
    }

    private fun addPathLinks(blockTraverse: BlockTraverse, next: Traverse) {
        val paths = blockTraverse.paths
        var lastTraverse : Traverse = blockTraverse

        for((index, current) in paths.subList(0, paths.size).iterator().withIndex()){

            current.previousTraverse = lastTraverse
            current.nextTraverse = current.path.first()
            current.path.first().previousTraverse = current
            if(index+1 >= paths.size){
                break
            }
            current.path.last().nextTraverse = paths[index+1]

            lastTraverse = current.path.last()
        }
        paths.last().path.last().nextTraverse = next
    }

    private fun determineNext(child: Executing, children: List<Executing>): List<Executing> {
        if(children.last() == child){
            return emptyList()
        }

        return listOf(children[children.indexOf(child) + 1])
    }
}
abstract class Traverse () {
    var start: Boolean = false
    var end: Boolean = false
    var previousTraverse: Traverse? = null
    var nextTraverse: Traverse? = null

    fun isStart() : Boolean {
        return previousTraverse == null
    }
    fun isEnd() : Boolean {
        return  nextTraverse == null
    }
    abstract fun next() : Traverse?
    abstract fun currentNode() : Executing
    abstract fun name(): String
}
class NodeTraverse (val current: Executing,
                    val nextExecutings: List<Executing>) : Traverse(){
    override fun next(): Traverse? {
        return nextTraverse
    }

    override fun currentNode(): Executing {
        return current
    }

    override fun name(): String {
        if(current is Given<*>){
            return current.label!!
        }
        return current.type.local
    }
}

class BlockTraverse (val current: Executing): Traverse(){
    val paths: ArrayList<PathTraverse> = ArrayList()
    override fun next(): Traverse? {
        return paths.first()
    }

    override fun currentNode(): Executing {
        return current
    }

    override fun name(): String {
        return current.type.local + "block"
    }
}

class PathTraverse (val block: BlockTraverse, val initiator: Executing, val addition: Executing?, val path: List<Traverse>): Traverse(){
    override fun next(): Traverse? {
        return if (path.isEmpty()) nextTraverse else path.first()
    }

    override fun currentNode(): Executing {
        return initiator
    }

    override fun name(): String {
        return block.paths.last().path.last().next()!!.name()
    }
}
