package me.zama.simple_di

internal sealed interface Qualifier

internal data class StringQualifier(val qualifier: String) : Qualifier {

    override fun toString() = qualifier
}

internal data class IntQualifier(val qualifier: Int) : Qualifier {

    override fun toString() = qualifier.toString()
}

internal data class EnumQualifier(val qualifier: Enum<*>) : Qualifier {

    override fun toString() = qualifier.toString()
}

internal object DefaultQualifier : Qualifier
