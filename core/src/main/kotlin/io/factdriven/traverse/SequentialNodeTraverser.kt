package io.factdriven.traverse

import io.factdriven.definition.Node
import io.factdriven.definition.typeName
import io.factdriven.language.Given
import io.factdriven.language.Select
import java.util.*

class SequentialNodeTraverser (node: Node){
    val startingNode = node
    val traverseHistory: ArrayList<Traverse> = ArrayList()

    fun fullTraverse() : List<Traverse>{
        fullTraverse(startingNode)
        traverseHistory.last().end = true
        return traverseHistory
    }

    private fun fullTraverse(node: Node){
        if(isBlockStructureInitiator(node)){
            blockStructureTraverse(node)
        } else {
            nodeSequenceTraverse(node)
        }
    }

    private fun isBlockStructureInitiator(node: Node): Boolean {
        return node is Select <*>
    }

    private fun blockStructureTraverse(node: Node) {
        val blockTraverse = BlockTraverse(node)
        for (condition in node.children) {
            val pathHistory = SequentialNodeTraverser(condition).fullTraverse()
            val given = findGiven(pathHistory)
            val traverse = PathTraverse(block = blockTraverse, initiator = node, path = pathHistory, addition = given)

            blockTraverse.paths.add(traverse)
        }
        addLinks(blockTraverse)
        traverseHistory.add(blockTraverse)
    }

    private fun findGiven(pathHistory: List<Traverse>): Node? {
        for (traverse in pathHistory) {
            if(traverse is NodeTraverse && traverse.current is Given<*>){
                return traverse.current
            }
        }
        return null
    }

    private fun nodeSequenceTraverse(node: Node) {
        val children = node.children
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

    private fun determineNext(child: Node, children: List<Node>): List<Node> {
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
    abstract fun currentNode() : Node
    abstract fun name(): String
}
class NodeTraverse (val current: Node,
                    val nextNodes: List<Node>) : Traverse(){
    override fun next(): Traverse? {
        return nextTraverse
    }

    override fun currentNode(): Node {
        return current
    }

    override fun name(): String {
        if(current is Given<*>){
            return current.label!!
        }
        return current.typeName.local
    }
}

class BlockTraverse (val current: Node): Traverse(){
    val paths: ArrayList<PathTraverse> = ArrayList()
    override fun next(): Traverse? {
        return paths.first()
    }

    override fun currentNode(): Node {
        return current
    }

    override fun name(): String {
        return current.typeName.local + "block"
    }
}

class PathTraverse (val block: BlockTraverse, val initiator: Node, val addition: Node?, val path: List<Traverse>): Traverse(){
    override fun next(): Traverse? {
        return if (path.isEmpty()) nextTraverse else path.first()
    }

    override fun currentNode(): Node {
        return initiator
    }

    override fun name(): String {
        return block.paths.last().path.last().next()!!.name()
    }
}
