package me.zama.simple_di

internal interface Component {

    fun <T: Any> get(klass: Class<T>, qualifier: Qualifier = DefaultQualifier): T
}

internal inline fun <reified T: Any> Component.get() = get(T::class.java, DefaultQualifier)

internal inline fun <reified T: Any> Component.get(qualifier: String) = get<T>(StringQualifier(qualifier))

internal inline fun <reified T: Any> Component.get(qualifier: Enum<*>) = get<T>(EnumQualifier(qualifier))

internal inline fun <reified T: Any> Component.get(
    qualifier: Qualifier = DefaultQualifier
) = get(T::class.java, qualifier)

internal inline fun <reified T : Any> Component.injectLazy(
    qualifier: Qualifier = DefaultQualifier
) = lazy { get(T::class.java, qualifier) }