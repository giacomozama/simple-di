package me.zama.simple_di

import java.util.concurrent.ConcurrentHashMap

internal class ValidatingComponent(modules: Set<Module>): Component {

    private val enqueuedDependencies: MutableSet<Triple<Long, Class<*>, Qualifier>> = ConcurrentHashMap.newKeySet()

    private val dependencyProviders = module { modules.forEach(::subModule) }.aggregateDependencyProviders()

    init {
        for (qualified in dependencyProviders.values) {
            for (provider in qualified.values) {
                (provider as? EagerSingletonDependencyProvider)?.instantiate(this)
            }
        }
    }

    override fun <T : Any> get(klass: Class<T>, qualifier: Qualifier): T {
        val key = Triple(Thread.currentThread().id, klass, qualifier)
        if (!enqueuedDependencies.add(key)) throw CircularDependencyException(klass)

        val qualified = dependencyProviders.getOrElse(klass) {
            throw MissingBindingException(klass)
        }

        @Suppress("UNCHECKED_CAST")
        val provider = qualified.getOrElse(qualifier) {
            throw MissingQualifierException(klass, qualifier)
        } as DependencyProvider<T>

        val dependency = provider.get(this)

        enqueuedDependencies.remove(key)

        return dependency
    }
}