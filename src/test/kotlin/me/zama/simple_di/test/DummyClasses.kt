package me.zama.simple_di.test

class A(id: Int? = null) {
    val id = id ?: created++

    companion object {
        var created = 0
    }
}

class B(id: Int? = null) {
    val id = id ?: created++

    companion object {
        var created = 0
    }
}

class C(val a: A, val b: B, id: Int? = null) {
    val id = id ?: created++

    companion object {
        var created = 0
    }
}

interface I

class IImpl(id: Int? = null) : I {
    val id = id ?: created++

    companion object {
        var created = 0
    }
}

enum class Q { Q1, Q2 }

fun resetCounters() {
    A.created = 0
    B.created = 0
    C.created = 0
    IImpl.created = 0
}

class CircA(val b: CircB)

class CircB(val a: CircA)