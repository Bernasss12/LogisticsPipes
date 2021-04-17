/*
 * Copyright (c) 2021  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2021  RS485
 *
 * This MIT license was reworded to only match this file. If you use the regular
 * MIT license in your project, replace this copyright notice (this line and any
 * lines below and NOT the copyright line above) with the lines from the original
 * MIT license located here: http://opensource.org/licenses/MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this file and associated documentation files (the "Source Code"), to deal in
 * the Source Code without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Source Code, and to permit persons to whom the Source Code is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Source Code, which also can be
 * distributed under the MIT.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package network.rs485.logisticspipes.property

import net.minecraft.nbt.NBTTagCompound
import java.util.*

interface IBitSet {
    val size: Int
    operator fun get(bit: Int): Boolean
    operator fun set(bit: Int, value: Boolean)
    fun flip(bit: Int)
    fun nextSetBit(idx: Int): Int
    fun setFrom(other: BitSet)
    fun setFrom(other: IBitSet)
    fun copyValue(): BitSet
    fun clear()
}

class BitSetProperty(private val bitset: BitSet, override val tagKey: String) : IBitSet, Property<BitSet> {

    override val propertyObservers: MutableList<ObserverCallback<BitSet>> = mutableListOf()

    override fun copyValue(): BitSet = bitset.clone() as BitSet

    override val size: Int
        get() = bitset.size()

    override fun clear() = bitset.clear()

    fun copyValue(startIdx: Int, endIdx: Int): BitSet = bitset.get(startIdx, endIdx)

    override fun copyProperty(): BitSetProperty = BitSetProperty(copyValue(), tagKey)

    fun replaceWith(other: BitSet) = other.takeUnless { it == bitset }
        ?.let { bitset.clear(); bitset.or(it) }
        ?.alsoIChanged()

    fun replaceWith(other: BitSetProperty) = other.takeUnless { it === this }
        ?.let { bitset.clear(); bitset.or(other.bitset) }
        ?.alsoIChanged()

    override fun setFrom(other: BitSet) {
        if (other.size() == 0) return
        bitset.clear(0, other.size() - 1)
        bitset.or(other)
        iChanged()
    }

    override fun setFrom(other: IBitSet) {
        if (other.size == 0) return
        bitset.clear(0, other.size - 1)
        bitset.or(other.copyValue())
        iChanged()
    }

    override fun readFromNBT(tag: NBTTagCompound) {
        if (tag.hasKey(tagKey)) replaceWith(BitSet.valueOf(tag.getByteArray(tagKey)))
    }

    override fun writeToNBT(tag: NBTTagCompound) = tag.setByteArray(tagKey, bitset.toByteArray())

    override fun get(bit: Int): Boolean = bitset.get(bit)
    override fun set(bit: Int, value: Boolean) = bitset.set(bit, value).alsoIChanged()
    override fun flip(bit: Int) = bitset.flip(bit).alsoIChanged()
    override fun nextSetBit(idx: Int): Int = bitset.nextSetBit(idx)

    fun get(startIdx: Int, endIdx: Int): IBitSet {
        if (startIdx < 0 || startIdx >= bitset.size()) {
            throw IndexOutOfBoundsException("startIdx[$startIdx] is out of bounds")
        }
        if (endIdx < 0 || endIdx >= bitset.size()) {
            throw IndexOutOfBoundsException("endIdx[$endIdx] is out of bounds")
        }
        return PartialBitSet(startIdx..endIdx)
    }

    override fun equals(other: Any?): Boolean = (other as? BitSetProperty)?.let {
        tagKey == other.tagKey && bitset == other.bitset
    } ?: false

    override fun hashCode(): Int = Objects.hash(tagKey, bitset)

    override fun toString(): String = "BitSetProperty(tagKey=$tagKey, bitset=$bitset)"

    inner class PartialBitSet(private val indices: IntRange) : IBitSet {

        override val size: Int
            get() = (indices.last - indices.first) + 1

        init {
            if (indices.last < indices.first) {
                throw IllegalArgumentException("start[${indices.first}] must be <= end[${indices.last}]")
            }
        }

        private fun rangeCheck(idx: Int): Int {
            if (idx < 0 || idx >= size) {
                throw IndexOutOfBoundsException("idx[$idx] out of bounds of $this")
            }
            return idx
        }

        override fun get(bit: Int): Boolean = this@BitSetProperty[indices.first + rangeCheck(bit)]
        override fun set(bit: Int, value: Boolean) = this@BitSetProperty.set(indices.first + rangeCheck(bit), value)
        override fun flip(bit: Int) = this@BitSetProperty.flip(indices.first + rangeCheck(bit))
        override fun nextSetBit(idx: Int): Int = this@BitSetProperty.nextSetBit(indices.first + rangeCheck(idx))
        override fun clear() = this@BitSetProperty.bitset.clear(indices.first, indices.last).alsoIChanged()

        override fun setFrom(other: BitSet) {
            rangeCheck(other.size() - 1)
            bitset.clear(indices.first, indices.last)
            other.stream().forEach { bit -> bitset.set(indices.first + bit) }
            iChanged()
        }

        override fun setFrom(other: IBitSet) {
            rangeCheck(other.size - 1)
            bitset.clear(indices.first, indices.last)
            var idx = other.nextSetBit(0)
            val setBits = generateSequence {
                idx.takeUnless { it == -1 }
                    ?.also { idx = other.nextSetBit(idx) }
            }
            setBits.forEach { bit -> bitset.set(indices.first + bit) }
            iChanged()
        }

        override fun copyValue(): BitSet = this@BitSetProperty.copyValue(indices.first, indices.last)

        override fun equals(other: Any?): Boolean = (other as? IBitSet)?.copyValue()?.equals(copyValue()) ?: false
        override fun hashCode(): Int = copyValue().hashCode()

        override fun toString(): String =
            "PartialBitSet(indices=$indices, bitset=${copyValue()})"
    }

}
