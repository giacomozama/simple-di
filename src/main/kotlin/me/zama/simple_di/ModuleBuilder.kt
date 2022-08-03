package me.zama.simple_di

@DslMarker
internal annotation class ModuleBuilderDsl

@ModuleBuilderDsl
internal interface ModuleBuilderScope {

    fun <T : Any> factory(klass: Class<T>, factory: Component.() -> T)

    fun <T : Any> factory(klass: Class<T>, qualifier: String, factory: Component.() -> T)

    fun <T : Any> factory(klass: Class<T>, qualifier: Int, factory: Component.() -> T)

    fun <T : Any> factory(klass: Class<T>, qualifier: Enum<*>, factory: Component.() -> T)

    fun <T : Any> singleton(klass: Class<T>, isLazy: Boolean = true, factory: Component.() -> T)

    fun <T : Any> singleton(klass: Class<T>, qualifier: String, isLazy: Boolean = true, factory: Component.() -> T)

    fun <T : Any> singleton(klass: Class<T>, qualifier: Int, isLazy: Boolean = true, factory: Component.() -> T)

    fun <T : Any> singleton(
        klass: Class<T>,
        qualifier: Enum<*>,
        isLazy: Boolean = true,
        factory: Component.() -> T
    )

    fun subModule(module: Module)
}

internal inline fun <reified T : Any> ModuleBuilderScope.factory(
    noinline factory: Component.() -> T
) = factory(T::class.java, factory)

internal inline fun <reified T : Any> ModuleBuilderScope.factory(
    qualifier: String,
    noinline factory: Component.() -> T
) = factory(T::class.java, qualifier, factory)

internal inline fun <reified T : Any> ModuleBuilderScope.factory(
    qualifier: Int,
    noinline factory: Component.() -> T
) = factory(T::class.java, qualifier, factory)

internal inline fun <reified T : Any> ModuleBuilderScope.factory(
    qualifier: Enum<*>,
    noinline factory: Component.() -> T
) = factory(T::class.java, qualifier, factory)

internal inline fun <reified T : Any> ModuleBuilderScope.singleton(
    isLazy: Boolean = true,
    noinline factory: Component.() -> T
) = singleton(T::class.java, isLazy, factory)

internal inline fun <reified T : Any> ModuleBuilderScope.singleton(
    qualifier: String,
    isLazy: Boolean = true,
    noinline factory: Component.() -> T
) = singleton(T::class.java, qualifier, isLazy, factory)

internal inline fun <reified T : Any> ModuleBuilderScope.singleton(
    qualifier: Int,
    isLazy: Boolean = true,
    noinline factory: Component.() -> T
) = singleton(T::class.java, qualifier, isLazy, factory)

internal inline fun <reified T : Any> ModuleBuilderScope.singleton(
    qualifier: Enum<*>,
    isLazy: Boolean = true,
    noinline factory: Component.() -> T
) = singleton(T::class.java, qualifier, isLazy, factory)

internal class ModuleBuilderScopeImpl(
    private val dependencyProviders: MutableMap<Class<*>, MutableMap<Qualifier, DependencyProvider<*>>>,
    private val subModules: MutableSet<Module>
) : ModuleBuilderScope {

    private fun <T : Any> registerProvider(
        klass: Class<T>,
        qualifier: Qualifier,
        provider: DependencyProvider<T>
    ) {
        if (dependencyProviders.getOrPut(klass) { mutableMapOf() }.put(qualifier, provider) != null) {
            if (qualifier is DefaultQualifier) throw DuplicateBindingException(klass)
            throw DuplicateQualifierException(klass, qualifier)
        }
    }

    private fun <T : Any> registerFactory(
        klass: Class<T>,
        qualifier: Qualifier,
        factory: Component.() -> T
    ) {
        registerProvider(klass, qualifier, FactoryDependencyProvider(factory))
    }

    private fun <T : Any> registerSingleton(
        klass: Class<T>,
        qualifier: Qualifier,
        isLazy: Boolean,
        factory: Component.() -> T
    ) {
        val provider = if (isLazy) {
            LazySingletonDependencyProvider(factory)
        } else {
            EagerSingletonDependencyProvider(factory)
        }
        registerProvider(klass, qualifier, provider)
    }

    override fun <T : Any> factory(
        klass: Class<T>,
        factory: Component.() -> T
    ) = registerFactory(klass, DefaultQualifier, factory)

    override fun <T : Any> factory(
        klass: Class<T>,
        qualifier: String,
        factory: Component.() -> T
    ) = registerFactory(klass, StringQualifier(qualifier), factory)

    override fun <T : Any> factory(
        klass: Class<T>,
        qualifier: Int,
        factory: Component.() -> T
    ) = registerFactory(klass, IntQualifier(qualifier), factory)

    override fun <T : Any> factory(
        klass: Class<T>,
        qualifier: Enum<*>,
        factory: Component.() -> T
    ) = registerFactory(klass, EnumQualifier(qualifier), factory)

    override fun <T : Any> singleton(
        klass: Class<T>,
        isLazy: Boolean,
        factory: Component.() -> T
    ) = registerSingleton(klass, DefaultQualifier, isLazy, factory)

    override fun <T : Any> singleton(
        klass: Class<T>,
        qualifier: String,
        isLazy: Boolean,
        factory: Component.() -> T
    ) = registerSingleton(klass, StringQualifier(qualifier), isLazy, factory)

    override fun <T : Any> singleton(
        klass: Class<T>,
        qualifier: Int,
        isLazy: Boolean,
        factory: Component.() -> T
    ) = registerSingleton(klass, IntQualifier(qualifier), isLazy, factory)

    override fun <T : Any> singleton(
        klass: Class<T>,
        qualifier: Enum<*>,
        isLazy: Boolean,
        factory: Component.() -> T
    ) = registerSingleton(klass, EnumQualifier(qualifier), isLazy, factory)

    override fun subModule(module: Module) {
        subModules.add(module)
    }
}

internal inline fun module(build: ModuleBuilderScope.() -> Unit): Module {
    val dependencyProviders = mutableMapOf<Class<*>, MutableMap<Qualifier, DependencyProvider<*>>>()
    val subModules = mutableSetOf<Module>()
    ModuleBuilderScopeImpl(dependencyProviders, subModules).build()
    return ModuleImpl(dependencyProviders, subModules)
}