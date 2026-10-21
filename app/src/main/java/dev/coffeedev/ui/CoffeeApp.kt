package dev.coffeedev.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.coffeedev.BuildConfig
import dev.coffeedev.data.CoffeeOrder
import dev.coffeedev.data.Member
import dev.coffeedev.data.Product
import java.text.DateFormat

@Composable fun CoffeeApp(vm: CoffeeViewModel) {
    val member by vm.member.collectAsStateWithLifecycle()
    val products by vm.products.collectAsStateWithLifecycle()
    val orders by vm.orders.collectAsStateWithLifecycle()
    val members by vm.members.collectAsStateWithLifecycle()
    val failure by vm.failure.collectAsStateWithLifecycle()
    val s = remember(vm.language) { Strings(vm.language) }
    val snackbar = remember { SnackbarHostState() }
    var showLegal by remember { mutableStateOf(false) }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(member?.uid, member?.status) { selectedId = null }
    LaunchedEffect(vm.message) {
        vm.message?.let { key -> snackbar.showSnackbar(s[key]); vm.clearMessage() }
    }
    CoffeeTheme(vm.theme) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = { AppBar(vm, s, member) { showLegal = true } },
            bottomBar = {
                if (member?.status == "active") NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    val tabs = listOf("home" to Icons.Outlined.Home, "menu" to Icons.Outlined.RestaurantMenu, "orders" to Icons.Outlined.ReceiptLong, "profile" to Icons.Outlined.Person)
                    tabs.forEach { (key, icon) ->
                        val selected = vm.tab == key || (key == "profile" && vm.tab in listOf("access", "adminOrders", "adminMenu", "about"))
                        NavigationBarItem(selected = selected, onClick = { vm.tab = key }, icon = { Icon(icon, null) }, label = { Text(s[key], maxLines = 1, fontSize = 11.sp) })
                    }
                }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                if (vm.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                if (failure) Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        Text(s["syncError"], style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = vm::refresh, enabled = !vm.busy) { Text(s["refresh"]) }
                    }
                }
                when {
                    vm.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    member == null -> Login(vm, s)
                    member?.status != "active" -> Waiting(vm, s, member!!)
                    else -> when(vm.tab) {
                        "home", "menu" -> MenuPage(vm, s, products, vm.tab == "home") { selectedId = it }
                        "cart" -> CartPage(vm, s, products)
                        "orders" -> OrdersPage(vm, s, orders.filter { it.uid == member!!.uid }, false)
                        "access" -> if(member!!.owner) AccessPage(vm, s, members) else ProfilePage(vm, s, member!!)
                        "adminOrders" -> if(member!!.owner) OrdersPage(vm, s, orders, true) else ProfilePage(vm, s, member!!)
                        "adminMenu" -> if(member!!.owner) AdminMenu(vm, s, products) else ProfilePage(vm, s, member!!)
                        "about" -> AboutPage(s)
                        else -> ProfilePage(vm, s, member!!)
                    }
                }
            }
        }
        if (showLegal) LegalCenter(vm, s, member) { showLegal = false }
        products.find { it.id == selectedId }?.takeIf { member?.status == "active" }?.let { product ->
            ProductDialog(product, vm, s) { selectedId = null }
        }
    }
}

@Composable private fun AppBar(vm: CoffeeViewModel, s: Strings, member: Member?, openLegal: () -> Unit) {
    var languageMenu by remember { mutableStateOf(false) }
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            BrandMark(Modifier.size(32.dp))
            Text("CoffeeDev", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 6.dp).weight(1f), maxLines = 1)
            IconButton(onClick = openLegal) { Icon(Icons.Outlined.Info, s["legalCenter"]) }
            Box {
                TextButton(onClick = { languageMenu = true }, modifier = Modifier.semantics { contentDescription = s["language"] + ": " + vm.language.uppercase() }, contentPadding = PaddingValues(8.dp)) { Text(vm.language.uppercase(), style = MaterialTheme.typography.labelLarge) }
                DropdownMenu(expanded = languageMenu, onDismissRequest = { languageMenu = false }) {
                    listOf("pt" to "Português", "en" to "English", "es" to "Español").forEach { (code, label) ->
                        DropdownMenuItem(text = { Text(label) }, onClick = { vm.updateLanguage(code); languageMenu = false })
                    }
                }
            }
            IconButton(onClick = { vm.updateTheme(if(vm.theme == "dark") "light" else "dark") }) {
                Icon(if(vm.theme == "dark") Icons.Outlined.LightMode else Icons.Outlined.DarkMode, s["appearance"])
            }
            if(member?.status == "active") IconButton(onClick = { vm.tab = "cart" }) {
                BadgedBox(badge = { if (vm.cart.isNotEmpty()) Badge { Text(vm.cart.values.sum().toString()) } }) { Icon(Icons.Outlined.ShoppingBag, s["cart"]) }
            }
        }
    }
}

@Composable private fun Login(vm: CoffeeViewModel, s: Strings) {
    var readTerms by remember { mutableStateOf(false) }
    var accepted by rememberSaveable { mutableStateOf(false) }
    var registration by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    // Password deliberately excluded from saved instance state.
    var password by remember { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CoffeeArt("cappuccino", Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(28.dp)), s["cappuccino"])
        Text(s[if(registration) "register" else "loginTitle"], style = MaterialTheme.typography.headlineLarge)
        Text(s[if(registration) "registerBody" else "loginBody"], color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (registration) OutlinedTextField(name, { name = it.take(80) }, label = { Text(s["name"]) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
        OutlinedTextField(email, { email = it.take(254) }, label = { Text(s["email"]) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
        OutlinedTextField(password, { password = it.take(128) }, label = { Text(s["password"]) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), visualTransformation = if(showPassword) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { showPassword = !showPassword }) { Icon(if(showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, s[if(showPassword) "hidePassword" else "showPassword"]) } }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
        if(registration) Text(s["passwordHint"], style = MaterialTheme.typography.bodySmall)
        if (registration) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = accepted, onCheckedChange = { accepted = it }, modifier = Modifier.semantics { contentDescription = s["acceptTerms"] })
                Text(s["acceptTerms"], Modifier.weight(1f))
            }
            TextButton(onClick = { readTerms = true }) { Text(s["readPolicies"]) }
        }
        Button(onClick = { vm.authenticate(name, email, password, registration) }, enabled = !vm.busy && (!registration || accepted), modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp), shape = RoundedCornerShape(18.dp)) { Text(s[if(registration) "requestAccess" else "signIn"]) }
        TextButton(onClick = { registration = !registration; password = "" }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text(s[if(registration) "hasAccount" else "noAccount"] + if(!registration) " ${s["register"]}" else "") }
        if(BuildConfig.IS_DEMO) InfoCard(s["demo"], s["demoBody"]) { Text(s["demoPrivacy"], style = MaterialTheme.typography.bodySmall) }
    }
    if(readTerms) LegalCenter(vm, s, null) { readTerms = false }
}

@Composable private fun Waiting(vm: CoffeeViewModel, s: Strings, member: Member) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(28.dp), verticalArrangement = Arrangement.spacedBy(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(32.dp)); BrandMark(Modifier.size(100.dp))
        Text(s[if(member.status == "blocked") "blocked" else "pending"], style = MaterialTheme.typography.headlineMedium)
        Text(s[if(member.status == "blocked") "blockedBody" else "pendingBody"])
        Text(member.email, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = vm::refresh, enabled = !vm.busy) { Text(s["refresh"]) }
        TextButton(onClick = vm::signOut, enabled = !vm.busy) { Text(s["signOut"]) }
    }
}

@Composable private fun MenuPage(vm: CoffeeViewModel, s: Strings, products: List<Product>, home: Boolean, select: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("all") }
    var favoritesOnly by rememberSaveable { mutableStateOf(false) }
    val columns = if (LocalDensity.current.fontScale >= 1.3f) 1 else 2
    val shown = products.filter { (category == "all" || it.category == category) && (!favoritesOnly || it.id in vm.favorites) && s[it.id].contains(query, true) }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { Text(s["welcome"], style = MaterialTheme.typography.headlineLarge) }
        if(home) item {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(24.dp)) {
                Row(Modifier.fillMaxWidth().heightIn(min = 164.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f).padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(s["featured"], style = MaterialTheme.typography.titleLarge)
                        Text(s["featuredBody"], style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { vm.tab = "menu" }, contentPadding = PaddingValues(0.dp)) { Text(s["explore"]) }
                    }
                    CoffeeArt("cappuccino", Modifier.width(120.dp).height(164.dp), s["cappuccino"])
                }
            }
        }
        item {
            OutlinedTextField(query, { query = it }, label = { Text(s["search"]) }, placeholder = { Text(s["search"]) }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp))
        }
        item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("all", "hot", "cold", "food").forEach { key -> FilterChip(selected = category == key, onClick = { category = key }, label = { Text(s[key]) }) }
                FilterChip(selected = favoritesOnly, onClick = { favoritesOnly = !favoritesOnly }, label = { Text(s["favorites"]) }, leadingIcon = { Icon(Icons.Outlined.FavoriteBorder, null, Modifier.size(16.dp)) })
            }
        }
        if(home) item { Text(s["popular"], style = MaterialTheme.typography.titleLarge) }
        if(shown.isEmpty()) item { EmptyState(if(favoritesOnly) Icons.Outlined.FavoriteBorder else Icons.Outlined.Search, s[if(favoritesOnly) "noFavorites" else "noResults"]) }
        items(shown.chunked(columns), key = { it.first().id }) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                row.forEach { product -> Box(Modifier.weight(1f)) { ProductCard(product, vm, s, select) } }
                if(row.size == 1 && columns == 2) Spacer(Modifier.weight(1f))
            }
        }
        item { Text(s["paymentInfo"], style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable private fun ProductCard(product: Product, vm: CoffeeViewModel, s: Strings, select: (String) -> Unit) {
    Card(onClick = { select(product.id) }, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Box {
            CoffeeArt(product.id, Modifier.fillMaxWidth().aspectRatio(1.12f), s[product.id])
            FilledIconButton(onClick = { vm.toggleFavorite(product.id) }, modifier = Modifier.align(Alignment.TopEnd).padding(5.dp).size(48.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surface)) { Icon(if(product.id in vm.favorites) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder, s[if(product.id in vm.favorites) "unfavorite" else "favorite"] + ": " + s[product.id], modifier = Modifier.size(20.dp)) }
        }
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(s[product.id], style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if(product.available) s.money(product.priceCents) else s["unavailable"], modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = { vm.add(product.id, 1) }, enabled = product.available && !vm.busy, modifier = Modifier.size(48.dp)) { Icon(Icons.Outlined.Add, s["add"] + ": " + s[product.id]) }
            }
        }
    }
}

@Composable private fun ProductDialog(product: Product, vm: CoffeeViewModel, s: Strings, dismiss: () -> Unit) {
    var quantity by remember(product.id) { mutableIntStateOf(1) }
    AlertDialog(onDismissRequest = dismiss,
        title = { Text(s[product.id], style = MaterialTheme.typography.headlineMedium) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                CoffeeArt(product.id, Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(18.dp)), s[product.id])
                Text(s[product.id + "Desc"])
                if(product.category != "food") Text(s["allergens"], style = MaterialTheme.typography.bodySmall)
                Text(s.money(product.priceCents), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Quantity(quantity, { if(quantity > 1) quantity-- }, { if(quantity < 10) quantity++ }, s, !vm.busy)
            }
        },
        confirmButton = { Button(onClick = { vm.add(product.id, quantity); dismiss() }, enabled = product.available && !vm.busy) { Text(s[if(product.available) "add" else "unavailable"]) } },
        dismissButton = { TextButton(onClick = dismiss) { Text(s["close"]) } }
    )
}

@Composable private fun CartPage(vm: CoffeeViewModel, s: Strings, products: List<Product>) {
    var note by rememberSaveable { mutableStateOf("") }
    val total = vm.cart.entries.sumOf { (id, q) -> (products.find { it.id == id }?.priceCents ?: 0) * q }
    val valid = vm.cart.isNotEmpty() && vm.cart.keys.all { id -> products.any { it.id == id && it.available } }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { PageTitle(s["cart"], s["pickup"]) }
        if(vm.cart.isEmpty()) item {
            EmptyState(Icons.Outlined.ShoppingBag, s["emptyCart"])
            Button(onClick = { vm.tab = "menu" }, modifier = Modifier.fillMaxWidth()) { Text(s["explore"]) }
        }
        items(vm.cart.entries.toList(), key = { it.key }) { (id, quantity) ->
            val product = products.find { it.id == id }
            Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CoffeeArt(id, Modifier.size(76.dp).clip(RoundedCornerShape(16.dp)), s[id])
                    Column(Modifier.weight(1f)) {
                        Text(s[id], style = MaterialTheme.typography.titleMedium)
                        Text(if(product?.available == true) s.money(product.priceCents * quantity) else s["unavailable"], color = MaterialTheme.colorScheme.primary)
                        Quantity(quantity, { vm.changeQuantity(id, -1) }, { vm.changeQuantity(id, 1) }, s, !vm.busy)
                    }
                }
            }
        }
        if(vm.cart.isNotEmpty()) {
            item { OutlinedTextField(note, { note = it.take(240) }, label = { Text(s["note"]) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), maxLines = 3, supportingText = { Text("${note.length}/240") }) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(s["total"], style = MaterialTheme.typography.titleLarge); Text(s.money(total), style = MaterialTheme.typography.titleLarge) }
            }
            item { Button(onClick = { vm.checkout(note) }, enabled = valid && !vm.busy, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(18.dp)) { Text(s["checkout"]) } }
            item { Text(s["paymentInfo"], style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable private fun OrdersPage(vm: CoffeeViewModel, s: Strings, orders: List<CoffeeOrder>, admin: Boolean) {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { PageTitle(s[if(admin) "adminOrders" else "orders"], if(BuildConfig.IS_DEMO) s["demo"] else s["latestOrders"]) }
        if(orders.isEmpty()) item { EmptyState(Icons.Outlined.ReceiptLong, s["emptyOrders"]) }
        items(orders, key = { it.id }) { order ->
            Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("${s["order"]} #${order.id.take(6).uppercase()}", style = MaterialTheme.typography.titleMedium)
                        Text(s.money(order.totalCents), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    if(order.createdAt > 0) Text(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, s.locale).format(java.util.Date(order.createdAt)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    SuggestionChip(onClick = {}, label = { Text(s[order.status]) }, icon = { Icon(Icons.Outlined.LocalCafe, null, Modifier.size(16.dp)) })
                    order.quantities.forEach { (id, quantity) -> Text("${quantity}× ${s[id]}", style = MaterialTheme.typography.bodyMedium) }
                    if(order.note.isNotBlank()) Text(order.note, style = MaterialTheme.typography.bodySmall)
                    if(admin && order.status != "completed") OutlinedButton(onClick = { vm.advance(order.id, order.status) }, enabled = !vm.busy) { Text(s["advance"]) }
                }
            }
        }
    }
}

@Composable private fun ProfilePage(vm: CoffeeViewModel, s: Strings, member: Member) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
        PageTitle(s["profile"], member.email)
        InfoCard(member.name, s[if(member.owner) "owner" else "active"]) {}
        Text(s["appearance"], style = MaterialTheme.typography.titleLarge)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("system", "light", "dark").forEach { key -> FilterChip(selected = vm.theme == key, onClick = { vm.updateTheme(key) }, label = { Text(s[key]) }) }
        }
        Text(s["language"], style = MaterialTheme.typography.titleLarge)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("pt" to "Português", "en" to "English", "es" to "Español").forEach { (code, label) -> FilterChip(selected = vm.language == code, onClick = { vm.updateLanguage(code) }, label = { Text(label) }) }
        }
        if(member.owner) {
            Text(s["admin"], style = MaterialTheme.typography.titleLarge)
            MenuLink(Icons.Outlined.AdminPanelSettings, s["access"]) { vm.tab = "access" }
            MenuLink(Icons.Outlined.ReceiptLong, s["adminOrders"]) { vm.tab = "adminOrders" }
            MenuLink(Icons.Outlined.RestaurantMenu, s["adminMenu"]) { vm.tab = "adminMenu" }
        }
        MenuLink(Icons.Outlined.Info, s["about"]) { vm.tab = "about" }
        if(BuildConfig.IS_DEMO) InfoCard(s["demo"], s["demoBody"]) {}
        OutlinedButton(onClick = vm::signOut, enabled = !vm.busy, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Logout, null); Spacer(Modifier.width(8.dp)); Text(s["signOut"]) }
    }
}

@Composable private fun AccessPage(vm: CoffeeViewModel, s: Strings, members: List<Member>) {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { PageTitle(s["access"], s["registerBody"]) }
        val candidates = members.filterNot { it.owner }.sortedBy { it.status != "pending" }
        if(candidates.isEmpty()) item { EmptyState(Icons.Outlined.PersonAdd, s["noRequests"]) }
        items(candidates, key = { it.uid }) { member ->
            InfoCard(member.name, member.email) {
                Text(s[member.status], color = MaterialTheme.colorScheme.primary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if(member.status != "active") Button(onClick = { vm.access(member.uid, true) }, enabled = !vm.busy) { Text(s["approve"]) }
                    if(member.status != "blocked") OutlinedButton(onClick = { vm.access(member.uid, false) }, enabled = !vm.busy) { Text(s["block"]) }
                }
            }
        }
    }
}

@Composable private fun AdminMenu(vm: CoffeeViewModel, s: Strings, products: List<Product>) {
    var editId by rememberSaveable { mutableStateOf<String?>(null) }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { PageTitle(s["adminMenu"], "CoffeeDev") }
        items(products, key = { it.id }) { product ->
            InfoCard(s[product.id], s.money(product.priceCents)) {
                Text(s[if(product.available) "available" else "unavailable"])
                OutlinedButton(onClick = { editId = product.id }, enabled = !vm.busy) { Text(s["adminMenu"]) }
            }
        }
    }
    products.find { it.id == editId }?.let { product ->
        var price by remember(product.id) { mutableStateOf(java.math.BigDecimal(product.priceCents).movePointLeft(2).toPlainString()) }
        var available by remember(product.id) { mutableStateOf(product.available) }
        val cents = runCatching { java.math.BigDecimal(price.replace(',', '.')).movePointRight(2).intValueExact() }.getOrNull()
        AlertDialog(onDismissRequest = { editId = null }, title = { Text(s[product.id]) }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(price, { price = it.take(10) }, label = { Text(s["price"]) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), isError = cents == null || cents !in 100..100000)
                Row(verticalAlignment = Alignment.CenterVertically) { Text(s["available"], Modifier.weight(1f)); Switch(available, { available = it }) }
            }
        }, confirmButton = { Button(onClick = { cents?.let { vm.saveProduct(product.id, it, available) }; editId = null }, enabled = cents != null && cents in 100..100000 && !vm.busy) { Text(s["save"]) } }, dismissButton = { TextButton(onClick = { editId = null }) { Text(s["cancel"]) } })
    }
}

@Composable private fun AboutPage(s: Strings) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
        BrandMark(Modifier.size(96.dp)); Text("CoffeeDev", style = MaterialTheme.typography.headlineLarge)
        Text(s["tagline"], style = MaterialTheme.typography.titleLarge)
        Text(s["aboutBody"]); Text(s["paymentInfo"], color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(BuildConfig.VERSION_NAME, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable private fun Quantity(value: Int, minus: () -> Unit, plus: () -> Unit, s: Strings, enabled: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = minus, enabled = enabled) { Icon(Icons.Outlined.Remove, s["decrease"]) }
        Text(value.toString(), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 4.dp))
        IconButton(onClick = plus, enabled = enabled && value < 10) { Icon(Icons.Outlined.Add, s["increase"]) }
    }
}
@Composable private fun PageTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() }); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) }
}
@Composable private fun EmptyState(icon: ImageVector, text: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) { Icon(icon, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary); Text(text) }
}
@Composable private fun InfoCard(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); content() }
    }
}
@Composable private fun MenuLink(icon: ImageVector, text: String, click: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = click)) { Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary); Text(text, style = MaterialTheme.typography.titleMedium) } }
}
