package dev.coffeedev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dev.coffeedev.ui.CoffeeApp
import dev.coffeedev.ui.CoffeeViewModel

class MainActivity : ComponentActivity() {
    private val model: CoffeeViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { CoffeeApp(model) }
    }
}
