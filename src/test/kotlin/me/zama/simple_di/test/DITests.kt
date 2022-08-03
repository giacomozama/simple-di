package me.zama.simple_di.test

import me.zama.simple_di.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class DITests {

    @AfterEach
    fun setup() {
        resetCounters()
        DI.unsetGlobalComponent()
    }

    @Test
    fun basic_injection() {
        val component = componentOf(
            module {
                factory { A() }
                factory { B() }
            },
            module {
                factory { C(get(), get()) }
            }
        )
        val c = component.get<C>()
        assertEquals(c.id, 0)
        assertEquals(c.a.id, 0)
        assertEquals(c.b.id, 0)
    }

    @Test
    fun interface_binding() {
        val component = componentOf(
            module {
                factory<I> { IImpl() }
            }
        )
        assert(component.get<I>() is IImpl)
        assertThrows<MissingBindingException> {
            component.get<IImpl>()
        }
    }

    @Test
    fun enum_qualifiers() {
        val component = componentOf(
            module {
                singleton(qualifier = Q.Q1) { A() }
                singleton(qualifier = Q.Q2) { A() }
            }
        )
        assertNotEquals(component.get<A>(Q.Q1).id, component.get<A>(Q.Q2).id)
    }

    @Test
    fun string_qualifiers() {
        val component = componentOf(
            module {
                singleton(qualifier = "a") { A() }
                singleton(qualifier = "b") { A() }
            }
        )
        assertNotEquals(component.get<A>("a").id, component.get<A>("b").id)
    }

    @Test
    fun singletons_1() {
        val component = componentOf(
            module {
                factory { A() }
                factory { B() }
            },
            module {
                singleton { C(get(), get()) }
            }
        )
        var c = component.get<C>()
        assertEquals(0, c.id)
        assertEquals(0, c.a.id)
        assertEquals(0, c.b.id)
        c = component.get()
        assertEquals(0, c.id)
        assertEquals(0, c.a.id)
        assertEquals(0, c.b.id)
    }

    @Test
    fun singletons_2() {
        val component = componentOf(
            module {
                singleton { A(123) }
                singleton { B(123) }
            },
            module {
                factory { C(get(), get()) }
            }
        )
        var c = component.get<C>()
        assertEquals(0, c.id)
        assertEquals(123, c.a.id)
        assertEquals(123, c.b.id)
        c = component.get()
        assertEquals(1, c.id)
        assertEquals(123, c.a.id)
        assertEquals(123, c.b.id)
    }

    @Test
    fun factories() {
        val component = componentOf(
            module {
                factory { A() }
                factory { B() }
            },
            module {
                factory { C(get(), get()) }
            }
        )
        var c = component.get<C>()
        assertEquals(0, c.id)
        assertEquals(0, c.a.id)
        assertEquals(0, c.b.id)
        c = component.get()
        assertEquals(1, c.id)
        assertEquals(1, c.a.id)
        assertEquals(1, c.b.id)
    }

    @Test
    fun eager_singleton() {
        val component = componentOf(
            module {
                singleton(qualifier = "a", isLazy = false) { A() }
                singleton(qualifier = "b") { A() }
            }
        )
        assert(component.get<A>("b").id > component.get<A>("a").id)
    }

    @Test
    fun duplicate_binding_in_same_module() {
        assertFailsWith<DuplicateBindingException> {
            componentOf(
                module {
                    factory<I> { IImpl() }
                    factory<I> { IImpl() }
                }
            )
        }
    }

    @Test
    fun duplicate_qualifier_in_same_module() {
        assertFailsWith<DuplicateQualifierException> {
            componentOf(
                module {
                    factory<I>("test") { IImpl() }
                    factory<I>("test") { IImpl() }
                }
            )
        }
    }

    @Test
    fun duplicate_binding_in_separate_modules() {
        assertFailsWith<DuplicateBindingException> {
            componentOf(
                module {
                    factory<I> { IImpl() }
                },
                module {
                    factory<I> { IImpl() }
                }
            )
        }
    }

    @Test
    fun duplicate_qualifier_in_separate_modules() {
        assertFailsWith<DuplicateQualifierException> {
            componentOf(
                module {
                    factory<I>("test") { IImpl() }
                },
                module {
                    factory<I>("test") { IImpl() }
                }
            )
        }
    }

    @Test
    fun circular_dependencies() {
        assertFailsWith<CircularDependencyException> {
            validatingComponentOf(
                module {
                    factory() { CircA(get()) }
                    factory() { CircB(get()) }
                }
            ).get<CircA>()
        }
    }
}