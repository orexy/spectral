package org.spectral.mapper.util

import org.spectral.asm.Class
import org.spectral.asm.Field
import org.spectral.asm.Method
import org.spectral.asm.value
import org.spectral.mapper.MatchGroup

/**
 * Contains utility methods used for generating a
 * similarity score for classes, methods, and fields.
 */
object ScoreUtil {

    /**
     * Calculates a similarity score for a pair of two potential matching
     * [Class] objects.
     *
     * @param a Class
     * @param b Class
     * @return Int
     */
    fun calculateScore(a: Class, b: Class): Int {
        var score = 0

        /*
         * Check for potential matching methods
         */
        a.methods.forEach { methodA ->
            b.methods.forEach { methodB ->
                if(CompareUtil.isPotentialMatch(methodA, methodB)) score++
            }
        }

        /*
         * Check for potential matching fields
         */
        a.fields.forEach { fieldA ->
            b.fields.forEach { fieldB ->
                if(CompareUtil.isPotentialMatch(fieldA, fieldB)) score++
            }
        }

        return score
    }

    /**
     * Calculates a similarity score for a pair of two potential
     * matching [Method] objects.
     *
     * @param a Method
     * @param b Method
     * @return Int
     */
    fun calculateScore(a: Method, b: Method): Int {
        var score = 0

        /*
         * Check for potential matching invocations.
         */
        a.invokes.forEach { invokeA ->
            b.invokes.forEach { invokeB ->
                if(CompareUtil.isPotentialMatch(invokeA, invokeB)) score++
            }
        }

        /*
         * Check for potential matching references.
         */
        a.references.forEach { refA ->
            b.references.forEach { refB ->
                if(CompareUtil.isPotentialMatch(refA, refB)) score++
            }
        }

        /*
         * Check for potential matching field reads.
         */
        a.fieldReads.forEach { fieldA ->
            b.fieldReads.forEach { fieldB ->
                if(CompareUtil.isPotentialMatch(fieldA, fieldB)) score++
            }
        }

        /*
         * Check for potential matching field writes
         */
        a.fieldWrites.forEach { fieldA ->
            b.fieldWrites.forEach { fieldB ->
                if(CompareUtil.isPotentialMatch(fieldA, fieldB)) score++
            }
        }

        /*
         * Check for matching hierarchy
         */
        a.overrides.forEach { overrideA ->
            b.overrides.forEach { overrideB ->
                if(CompareUtil.isPotentialMatch(overrideA, overrideB)) score++
            }
        }

        return score
    }

    /**
     * Calculates a simple similarity score between a given
     * pair of [Field] objects.
     *
     * @param a Field
     * @param b Field
     * @return Int
     */
    fun calculateScore(a: Field, b: Field): Int {
        var score = 0

        if(!a.isStatic && !b.isStatic) {
            if(CompareUtil.isPotentialMatch(a.owner, b.owner)) score++
        }

        a.reads.forEach { readA ->
            b.reads.forEach { readB ->
                if(CompareUtil.isPotentialMatch(readA, readB)) score++
            }
        }

        a.writes.forEach { writeA ->
            b.writes.forEach { writeB ->
                if(CompareUtil.isPotentialMatch(writeA, writeB)) score++
            }
        }

        if(a.type.sort == b.type.sort) {
            if(a.value == b.value) score++
        }

        return score
    }
}