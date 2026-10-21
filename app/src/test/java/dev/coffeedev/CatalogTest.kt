package dev.coffeedev

import dev.coffeedev.data.Catalog
import dev.coffeedev.ui.Strings
import org.junit.Assert.*
import org.junit.Test

class CatalogTest {
    @Test fun totalUsesIntegerCents() { assertEquals(3870, Catalog.total(mapOf("cappuccino" to 2,"espresso" to 1), Catalog.products)) }
    @Test fun rejectsInvalidCart() {
        listOf(emptyMap(), mapOf("unknown" to 1), mapOf("espresso" to 0), mapOf("espresso" to -1), mapOf("espresso" to 11)).forEach { cart ->
            assertTrue(runCatching { Catalog.total(cart, Catalog.products) }.isFailure)
        }
    }
    @Test fun rejectsUnavailableItem() {
        val menu = Catalog.products.map { if(it.id == "espresso") it.copy(available = false) else it }
        assertTrue(runCatching { Catalog.total(mapOf("espresso" to 1), menu) }.isFailure)
    }
    @Test fun usesCurrentMenuPrice() {
        val menu = Catalog.products.map { if(it.id == "espresso") it.copy(priceCents = 990) else it }
        assertEquals(1980, Catalog.total(mapOf("espresso" to 2), menu))
    }
    @Test fun allProductsHaveTranslationsInEachLanguage() {
        listOf("pt","en","es").forEach { code ->
            val s = Strings(code)
            Catalog.products.forEach { assertNotEquals(it.id+"Desc", s[it.id+"Desc"]) }
            assertNotEquals("signIn",s["signIn"])
            assertTrue(s.money(1490).isNotEmpty())
        }
    }
}
