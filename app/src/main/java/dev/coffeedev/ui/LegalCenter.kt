package dev.coffeedev.ui

import android.app.ActivityManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.coffeedev.BuildConfig
import dev.coffeedev.data.Member
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal const val AUTHOR_CONTACT = "https://www.linkedin.com/in/carlos-eduardo-a8bb50268/"

/** Publicly reachable before login; request drafts never leave the app automatically. */
@Composable fun LegalCenter(vm: CoffeeViewModel, s: Strings, member: Member?, dismiss: () -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var page by remember { mutableStateOf("index") }
    var confirmation by remember { mutableStateOf<String?>(null) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var saveBusy by remember { mutableStateOf(false) }
    val exportDocument = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val body = vm.privacyExport
        vm.clearExport()
        if (uri != null && body != null) scope.launch {
            saveBusy = true
            feedback = try {
                withContext(Dispatchers.IO) { requireNotNull(context.contentResolver.openOutputStream(uri)).bufferedWriter().use { it.write(body) } }
                s["savedFile"]
            } catch (_: Exception) { s["error"] } finally { saveBusy = false }
        }
    }
    LaunchedEffect(vm.privacyExport) {
        if(vm.privacyExport != null) exportDocument.launch("CoffeeDev-meus-dados.json")
    }
    LaunchedEffect(vm.message) {
        vm.message?.let { feedback = s[it]; vm.clearMessage() }
    }
    DisposableEffect(Unit) { onDispose { vm.clearExport() } }
    Dialog(onDismissRequest = { if(page == "index") dismiss() else page = "index" }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                Row(Modifier.fillMaxWidth().padding(8.dp)) {
                    IconButton(onClick = { if(page == "index") dismiss() else page = "index" }) { Icon(Icons.Outlined.ArrowBack, s["close"]) }
                    Text(s[if(page == "index") "legalCenter" else page], style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f).padding(8.dp).semantics { heading() })
                }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (page == "index") {
                        Text(s["authorInfo"])
                        Text(s["conceptImages"], style = MaterialTheme.typography.bodySmall)
                        listOf("privacy", "terms", "cookies", "accessibility", "credits", "requestForm").forEach { key ->
                            OutlinedButton(onClick = { page = key }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(s[key]) }
                        }
                        if (member != null && BuildConfig.IS_DEMO) {
                            Text(s["localActions"], style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
                            Text(s["exportWarning"])
                            Button(onClick = vm::exportPersonalData, enabled = !vm.busy && !saveBusy && vm.privacyExport == null, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(s["exportData"]) }
                            if(!member.owner) OutlinedButton(onClick = { confirmation = "delete" }, enabled = !vm.busy, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(s["deleteAccount"]) }
                            if(member.owner) OutlinedButton(onClick = { confirmation = "reset" }, enabled = !vm.busy, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(s["resetData"]) }
                        } else if(member != null) Text(s["cloudRights"])
                        TextButton(onClick = { runCatching { uriHandler.openUri(AUTHOR_CONTACT) }.onFailure { feedback = s["error"] } }) { Text(s["openContact"]) }
                        Text(s["contactLimit"], style = MaterialTheme.typography.bodySmall)
                    } else if(page == "requestForm") {
                        RequestDraft(s, onCopy = { body -> clipboard.setText(AnnotatedString(body)); feedback = s["copied"] }, openContact = { runCatching { uriHandler.openUri(AUTHOR_CONTACT) }.onFailure { feedback = s["error"] } })
                    } else {
                        val text = remember(page, s.language) {
                            context.assets.open("legal/${s.language}/$page.txt").bufferedReader().use { it.readText() }
                        }
                        SelectionContainer { Text(text, style = MaterialTheme.typography.bodyLarge) }
                        if(page == "credits") listOf("Sora-OFL.txt", "Inter-OFL.txt", "Apache-2.0.txt", "THIRD_PARTY_NOTICES.txt").forEach { name ->
                            var expanded by remember(name) { mutableStateOf(false) }
                            OutlinedButton(onClick = { expanded = !expanded }) { Text(name) }
                            if(expanded) {
                                val notice = remember(name) { context.assets.open("legal/licenses/$name").bufferedReader().use { it.readText() } }
                                SelectionContainer { Text(notice, style = MaterialTheme.typography.bodySmall) }
                            }
                        }
                    }
                    feedback?.let { Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.semantics { liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite }) }
                }
            }
        }
        confirmation?.let { action ->
            AlertDialog(onDismissRequest = { confirmation = null }, title = { Text(s[if(action == "reset") "resetData" else "deleteAccount"]) }, text = { Text(s[if(action == "reset") "resetConfirm" else "deleteConfirm"]) },
                confirmButton = { Button(onClick = {
                    confirmation = null
                    if(action == "reset") (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
                    else { vm.deleteLocalAccount(); dismiss() }
                }) { Text(s["confirmAction"]) } },
                dismissButton = { TextButton(onClick = { confirmation = null }) { Text(s["cancel"]) } })
        }
    }
}

@Composable private fun RequestDraft(s: Strings, onCopy: (String) -> Unit, openContact: () -> Unit) {
    var kind by remember { mutableStateOf("rightsAccess") }
    var expanded by remember { mutableStateOf(false) }
    var contact by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    Text(s["requestInstructions"])
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(s["requestType"] + ": " + s[kind]) }
        DropdownMenu(expanded, { expanded = false }) {
            listOf("rightsAccess", "rightsCorrect", "rightsDelete", "rightsConsent", "rightsShare", "rightsOppose", "barrier").forEach { key ->
                DropdownMenuItem(text = { Text(s[key]) }, onClick = { kind = key; expanded = false })
            }
        }
    }
    OutlinedTextField(contact, { contact = it.take(254) }, label = { Text(s["optionalContact"]) }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(details, { details = it.take(2000) }, label = { Text(s["requestDetails"]) }, modifier = Modifier.fillMaxWidth(), minLines = 4, supportingText = { Text("${details.length}/2000") })
    val draft = "${s["draftOnly"]}\n${s["requestMeta"]}\n${s["requestType"]}: ${s[kind]}\n${s["optionalContact"]}: $contact\n${s["requestDetails"]}: $details"
    Text(s["draftOnly"], style = MaterialTheme.typography.titleMedium)
    SelectionContainer { Text(draft) }
    Button(onClick = { onCopy(draft) }, enabled = details.isNotBlank(), modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(s["copyRequest"]) }
    OutlinedButton(onClick = openContact, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(s["openContact"]) }
    Text(s["contactLimit"], style = MaterialTheme.typography.bodySmall)
}
