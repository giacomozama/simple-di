package me.zama.simple_di

internal sealed interface DependencyProvider<T : Any> {

    fun get(component: Component): T
}

internal class LazySingletonDependencyProvider<T : Any>(
    private val factory: Component.() -> T
) : DependencyProvider<T> {

    private val lock = Any()
    private var value: T? = null

    override fun get(component: Component): T {
        return value ?: synchronized(lock) { value ?: component.factory().also { value = it } }
    }
}

internal class EagerSingletonDependencyProvider<T : Any>(
    private val factory: Component.() -> T
) : DependencyProvider<T> {

    private lateinit var value: T

    fun instantiate(component: Component) {
        value = component.factory()
    }

    override fun get(component: Component) = value
}

internal class FactoryDependencyProvider<T : Any>(
    private val factory: Component.() -> T
) : DependencyProvider<T> {

    override fun get(component: Component) = component.factory()
}