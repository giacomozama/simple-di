package me.zama.simple_di

internal typealias DependencyProviderMap = Map<Class<*>, Map<Qualifier, DependencyProvider<*>>>

internal interface Module {

    fun aggregateDependencyProviders(): DependencyProviderMap
}

internal class ModuleImpl(
    private val dependencyProviders: DependencyProviderMap,
    private val subModules: Set<Module>
) : Module {

    override fun aggregateDependencyProviders(): DependencyProviderMap {
        val result = dependencyProviders.mapValues { (_, v) -> v.toMutableMap() }.toMutableMap()
        for (subModule in subModules) {
            for ((klass, qualified) in subModule.aggregateDependencyProviders()) {
                val inResult = result.getOrPut(klass) { mutableMapOf() }
                for ((qualifier, provider) in qualified) {
                    if (inResult.put(qualifier, provider) != null) {
                        if (qualifier is DefaultQualifier) throw DuplicateBindingException(klass)
                        throw DuplicateQualifierException(klass, qualifier)
                    }
                }
            }
        }
        return result
    }
}
