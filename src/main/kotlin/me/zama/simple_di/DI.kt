package me.zama.simple_di

internal object DI : Component {

    private var globalComponent: Component? = null

    private fun requireGlobalComponent() = globalComponent ?: throw GlobalComponentNotInitializedException()

    fun initGlobalComponent(component: Component) {
        if (globalComponent != null) throw GlobalComponentAlreadyInitializedException()
        globalComponent = component
    }

    override fun <T : Any> get(klass: Class<T>, qualifier: Qualifier): T =
        requireGlobalComponent().get(klass, qualifier)

    fun unsetGlobalComponent() {
        globalComponent = null
    }
}

