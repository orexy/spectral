package org.spectral.deobfuscator.transformer

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.spectral.deobfuscator.asm.ClassGroupExt
import org.spectral.deobfuscator.Transformer
import org.tinylog.kotlin.Logger
import java.lang.reflect.Modifier

/**
 * Moves static fields into classes which is the only place
 * it is invoked.
 */
class FieldInliner : Transformer {

    override fun transform(group: ClassGroupExt) {
        var counter = 0

        val resolver = FieldResolver(group)

        group.forEach { c ->
            c.methods.forEach { m ->
                m.instructions.iterator().forEach { insn ->
                    if(insn is FieldInsnNode) {
                        val opcode = insn.opcode
                        val oldOwner = insn.owner
                        insn.owner = resolver.getOwner(
                            insn.owner,
                            insn.name,
                            insn.desc,
                            (opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC)
                        )

                        val newOwner = insn.owner
                        if(oldOwner != newOwner) counter++
                    }
                }
            }
        }

        Logger.info("Inlined $counter static fields.")
    }

    /**
     * Represents a field call graph resolver object.
     *
     * @property group ClassGroup
     * @constructor
     */
    private class FieldResolver(private val group: ClassGroupExt) {

        /**
         * A map of [group] to the class name as a key.
         */
        private val namedGroup = group.associateBy { it.name }

        /**
         * Gets the proper owner of a field by analyzing the invoke tree
         * of a given field.
         *
         * @param owner String
         * @param name String
         * @param desc String
         * @param isStatic Boolean
         * @return String
         */
        fun getOwner(owner: String, name: String, desc: String, isStatic: Boolean): String {

            var node = namedGroup[owner] ?: return owner

            /**
             * Loop forever until the block returns a value.
             */
            while(true) {
                if(node.hasDeclaredField(name, desc, isStatic)) {
                    return node.name
                }

                val superName = node.superName
                node = namedGroup[superName] ?: return superName
            }
        }


        /**
         * Checks if a [ClassNode] has a field matching the inputs.
         *
         * @receiver ClassNode
         * @param name String
         * @param desc String
         * @param isStatic Boolean
         * @return Boolean
         */
        private fun ClassNode.hasDeclaredField(name: String, desc: String, isStatic: Boolean): Boolean {
            return this.fields.any {
                it.name == name && it.desc == desc && Modifier.isStatic(it.access) == isStatic
            }
        }
    }
}