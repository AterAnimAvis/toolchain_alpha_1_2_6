package mixin.eclipse

import java.util.*
import kotlin.collections.LinkedHashSet

class OrderedProperties : Properties() {
    private val order = LinkedHashSet<Any?>()

    @Synchronized
    override fun keys() : Enumeration<Any> {
        return Collections.enumeration(order)
    }

    @Synchronized
    override fun put(key : Any?, value : Any?) : Any? {
        order.add(key)
        return super.put(key, value)
    }

    fun arg(key : String, value : String) : Any? {
        return put("org.eclipse.jdt.apt.processorOptions/$key", value)
    }
}