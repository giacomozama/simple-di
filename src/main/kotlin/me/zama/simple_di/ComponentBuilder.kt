package me.zama.simple_di

@DslMarker
internal annotation class ComponentBuilderDsl

@ComponentBuilderDsl
internal interface ComponentBuilderScope {

    fun module(module: Module)
}

internal class ComponentBuilderScopeImpl(
    private val modules: MutableSet<Module>
) : ComponentBuilderScope {

    override fun module(module: Module) {
        modules.add(module)
    }
}

internal fun component(build: ComponentBuilderScope.() -> Unit): Component {
    val modules = mutableSetOf<Module>()
    ComponentBuilderScopeImpl(modules).build()
    return StandardComponent(modules)
}

internal fun componentOf(vararg modules: Module): Component = StandardComponent(modules.toSet())

internal fun validatingComponent(build: ComponentBuilderScope.() -> Unit): Component {
    val modules = mutableSetOf<Module>()
    ComponentBuilderScopeImpl(modules).build()
    return ValidatingComponent(modules)
}

internal fun validatingComponentOf(vararg modules: Module): Component = ValidatingComponent(modules.toSet())