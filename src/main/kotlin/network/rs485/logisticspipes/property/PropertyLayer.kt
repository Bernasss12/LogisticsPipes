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

import java.util.*
import kotlin.streams.toList

abstract class PropertyLayer(propertiesIn: Collection<Property<*>>) : PropertyHolder {
    private val lowerLayer: List<Property<*>> = propertiesIn.toList()
    private val upperLayer: List<Property<*>> = propertiesIn.map(Property<*>::copyProperty).toList()
    private val changedIndices: BitSet = BitSet(propertiesIn.size)

    /**
     * A list consisting only of changed [properties][Property] on this [PropertyLayer].
     */
    override val properties: List<Property<*>>
        get() = changedIndices.stream().mapToObj { upperLayer[it] }.toList()

    init {
        lowerLayer.addObserver(::onChange)
        upperLayer.addObserver(::onPropertyWrite)
    }

    private fun onPropertyWrite(prop: Property<*>) = lookupIndex(prop, upperLayer).let {
        if (!changedIndices.get(it)) onFirstChange(it)
        changedIndices.set(it)
    }

    private fun onFirstChange(idx: Int) {
        // replace lower layer change listener with ours
        lowerLayer[idx].propertyObservers.remove(::onChange)
        upperLayer[idx].addObserver { onChange(lowerLayer[idx]) }
        upperLayer[idx].propertyObservers.remove(::onPropertyWrite)
        onChange(lowerLayer[idx])
    }

    private fun lookupIndex(prop: Property<*>, propList: List<Property<*>>): Int =
        propList.indexOfFirst { other -> prop === other }.takeUnless { it == -1 }
            ?: throw IllegalArgumentException("Property <$prop> not in this layer")

    @Suppress("UNCHECKED_CAST")
    fun <V : Property<T>, T> getWritableProperty(prop: V): V {
        // same index, same type
        return upperLayer[lookupIndex(prop, lowerLayer)] as V
    }

    @Suppress("UNCHECKED_CAST")
    fun <V : ValueProperty<T>, T> getLayerValue(prop: V): T {
        val idx = lookupIndex(prop, lowerLayer)
        val valueProperty = if (changedIndices.get(idx)) {
            upperLayer[idx]
        } else {
            lowerLayer[idx]
        } as ValueProperty<T>

        return valueProperty.value
    }

    @Suppress("UNCHECKED_CAST")
    fun <V : Property<T>, T> getLayerValue(prop: V): T {
        val idx = lookupIndex(prop, lowerLayer)
        val property = if (changedIndices.get(idx)) {
            upperLayer[idx]
        } else {
            lowerLayer[idx]
        } as Property<T>

        return property.copyValue()
    }

    fun unregister() = lowerLayer.forEach { it.propertyObservers.remove(::onChange) }

    /**
     * The passed property is *only* for checking equality with the properties passed to the layer as input properties.
     * The current value can be retrieved with [getLayerValue].
     */
    protected abstract fun onChange(property: Property<*>)

}
