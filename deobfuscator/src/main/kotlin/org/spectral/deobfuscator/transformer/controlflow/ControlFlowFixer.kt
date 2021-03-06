package org.spectral.deobfuscator.transformer.controlflow

import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.LabelNode
import org.spectral.deobfuscator.asm.ClassGroupExt
import org.spectral.deobfuscator.Transformer
import org.tinylog.kotlin.Logger
import java.util.*
import kotlin.collections.AbstractMap

/**
 * Reorders the control flow order in methods to make things more clear
 * and easier to read.
 *
 * Try-catch blocks add in their own issues so we will not re-order any blocks
 * which contain try-catch blocks.
 */
class ControlFlowFixer : Transformer {

    override fun transform(group: ClassGroupExt) {
        var counter = 0

        group.forEach { c ->
            c.methods.forEach { m ->
                if(m.tryCatchBlocks.isEmpty()) {
                    val analyzer = ControlFlowAnalyzer()
                    analyzer.analyze(c.name, m)
                    m.instructions = reorderInstructions(m.instructions, analyzer.blocks)

                    counter += analyzer.blocks.size
                }
            }
        }

        Logger.info("Re-ordered $counter control-flow blocks.")
    }

    private fun reorderInstructions(insns: InsnList, blocks: List<Block>): InsnList {
        val newInsns = InsnList()

        if(blocks.isEmpty()) {
            return insns
        }

        val labelMap = LabelMap()
        val blockQueue = Collections.asLifoQueue(ArrayDeque<Block>())
        val movedBlocks = hashSetOf<Block>()

        blockQueue.add(blocks.first())

        while(blockQueue.isNotEmpty()) {
            val block = blockQueue.remove()
            if(block in movedBlocks) continue
            movedBlocks.add(block)

            block.branches.forEach { blockQueue.add(it.origin) }
            block.next?.let { blockQueue.add(it) }

            for(i in block.startIndex until block.endIndex) {
                newInsns.add(insns[i].clone(labelMap))
            }
        }

        return newInsns
    }

    private class LabelMap : AbstractMap<LabelNode, LabelNode>() {
        private val map = hashMapOf<LabelNode, LabelNode>()

        override val entries: Set<Map.Entry<LabelNode, LabelNode>> get() = throw IllegalAccessException()
        override fun get(key: LabelNode): LabelNode = map.getOrPut(key) { LabelNode() }
    }
}