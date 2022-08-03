package me.zama.simple_di

internal class StandardComponent(modules: Set<Module>) : Component {

    private val dependencyProviders = module { modules.forEach(::subModule) }.aggregateDependencyProviders()

    init {
        for (qualified in dependencyProviders.values) {
            for (provider in qualified.values) {
                (provider as? EagerSingletonDependencyProvider)?.instantiate(this)
            }
        }
    }

    override fun <T : Any> get(klass: Class<T>, qualifier: Qualifier): T {
        val qualified = dependencyProviders.getOrElse(klass) { throw MissingBindingException(klass) }

        @Suppress("UNCHECKED_CAST")
        val provider = qualified.getOrElse(qualifier) {
            throw MissingQualifierException(klass, qualifier)
        } as DependencyProvider<T>

        return provider.get(this)
    }
}