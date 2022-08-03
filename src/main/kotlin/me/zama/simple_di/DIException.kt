package me.zama.simple_di

import java.lang.IllegalStateException


internal sealed class DIException(message: String? = null, exception: Exception? = null) : Exception(message, exception)

internal class DuplicateBindingException(klass: Class<*>)
    : DIException("A provider for ${klass.canonicalName} was already declared")

internal class DuplicateQualifierException(klass: Class<*>, qualifier: Qualifier)
    : DIException("A provider for ${klass.canonicalName} with qualifier $qualifier was already declared")

internal class MissingBindingException(klass: Class<*>)
    : DIException("No provider for ${klass.canonicalName} found")

internal class MissingQualifierException(klass: Class<*>, qualifier: Qualifier)
    : DIException("No provider for ${klass.canonicalName} with qualifier $qualifier found")

internal class GlobalComponentNotInitializedException
    : DIException("Global component not initialized", IllegalStateException())

internal class GlobalComponentAlreadyInitializedException
    : DIException("Global component already initialized", IllegalStateException())

internal class CircularDependencyException(klass: Class<*>)
    : DIException("Circular dependency detected while instantiating ${klass.canonicalName}")