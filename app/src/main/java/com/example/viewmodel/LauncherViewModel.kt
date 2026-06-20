package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LauncherViewModel(application: Application) : AndroidViewModel(application) {
    private val db = LauncherDatabase.getDatabase(application)
    private val repository = LauncherRepository(db)

    // Exposed Flows from database
    val allVersions = repository.allVersions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val settings = repository.settingsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), LauncherSettings()
    )
    val allMacros = repository.allMacros.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allAccounts = repository.allAccounts.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allMods = repository.allMods.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allServers = repository.allServers.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // UI Interactive States
    var currentTab by mutableStateOf("home") // "home", "settings", "controls", "mods", "multiplayer", "accounts"
    var isLaunching by mutableStateOf(false)
    var launchProgress by mutableStateOf(0f)
    var isGameActive by mutableStateOf(false)
    var consoleLogs = mutableStateListOf<String>()

    // Version download state
    var downloadingVersionId by mutableStateOf<String?>(null)
    var downloadProgress by mutableStateOf(0f)

    // Editor Modals or Temp Input fields
    var newAcctName by mutableStateOf("")
    var newMacroName by mutableStateOf("")
    var newMacroTrigger by mutableStateOf("Volume Up")
    var newMacroCmds by mutableStateOf("tap_coords(540,960),wait(80)")
    var newMacroRepeat by mutableStateOf(true)
    var newMacroDelay by mutableStateOf(80)

    var newModName by mutableStateOf("")
    var newModType by mutableStateOf("Mod/Add-on") // "Mod/Add-on" or "Texture Pack"
    var newModDesc by mutableStateOf("")
    var newModMinVer by mutableStateOf("1.0.0")

    var newServerName by mutableStateOf("")
    var newServerIp by mutableStateOf("")
    var newServerPort by mutableStateOf(19132)
    var newServerIsHotspot by mutableStateOf(false)

    init {
        viewModelScope.launch {
            // Populate database with mock game metadata on first launch
            repository.initializeDatabaseIfEmpty()
        }
    }

    // Version Download simulation
    fun installVersion(versionId: String) {
        viewModelScope.launch {
            downloadingVersionId = versionId
            downloadProgress = 0f
            while (downloadProgress < 1.0f) {
                delay(200)
                downloadProgress += 0.1f
            }
            repository.setVersionInstalled(versionId, true)
            downloadingVersionId = null
            downloadProgress = 0f
        }
    }

    // Version selection
    fun selectVersion(versionId: String) {
        viewModelScope.launch {
            val s = settings.value ?: LauncherSettings()
            repository.updateSettings(s.copy(selectedVersionId = versionId))
        }
    }

    // Save customized launcher settings
    fun updateLauncherSettings(updated: LauncherSettings) {
        viewModelScope.launch {
            repository.updateSettings(updated)
        }
    }

    // Launch MCPE Game Simulation
    fun triggerLaunchGame() {
        if (isGameActive) {
            isGameActive = false
            isLaunching = false
            launchProgress = 0f
            consoleLogs.clear()
            return
        }

        viewModelScope.launch {
            isLaunching = true
            consoleLogs.clear()
            consoleLogs.add("⚡ [BOOT] PocketLauncher customized core initializer online.")
            delay(400)
            launchProgress = 15f
            consoleLogs.add("📂 [ASSETS] Scanning local storage folders for selected MCPE binary...")
            
            val currentVer = settings.value?.selectedVersionId ?: "1.21.30"
            val s = settings.value ?: LauncherSettings()
            delay(500)
            launchProgress = 35f
            consoleLogs.add("📱 [HW_INFO] Resolution set to: ${s.resolutionWidth}x${s.resolutionHeight} @ 60 FPS")
            consoleLogs.add("🎯 [ENGINE] Booting Game Engine: [${s.gameEngine}] with render distance set to: ${s.renderDistance} CHUNKS")

            delay(600)
            launchProgress = 55f
            consoleLogs.add("🛠️ [MODS] Injecting resource overlays:")
            val activeMods = allMods.value.filter { it.isEnabled }
            if (activeMods.isEmpty()) {
                consoleLogs.add("     - No active sub-system mods/textures loaded.")
            } else {
                activeMods.forEach { mod ->
                    // Guard versions for mods & textures
                    val isCompatible = checkModCompatibility(mod, currentVer)
                    if (isCompatible) {
                        consoleLogs.add("     ✅ [ACTIVE] ${mod.name} (${mod.packType}) -> INJECTED!")
                    } else {
                        consoleLogs.add("     ❌ [BLOCKED] ${mod.name} -> INCOMPATIBLE WITH VER $currentVer!")
                    }
                }
            }

            delay(500)
            launchProgress = 75f
            consoleLogs.add("🧩 [MACROS] Mapping customized macro hotkeys:")
            val activeMacros = allMacros.value.filter { it.isEnabled }
            if (activeMacros.isEmpty()) {
                consoleLogs.add("     - No custom screen macros mapped.")
            } else {
                activeMacros.forEach { m ->
                    consoleLogs.add("     ⚡ Bound Map: Trigger Key '${m.triggerKey}' matches execution: \"${m.macroCommands}\"")
                }
            }

            delay(600)
            launchProgress = 90f
            // Account sign-in
            val accountsList = allAccounts.value
            val activeAcct = accountsList.find { it.id == s.activeAccountId } ?: accountsList.firstOrNull()
            
            if (activeAcct != null) {
                if (activeAcct.isCracked) {
                    consoleLogs.add("👤 [ACCOUNT] Connected via CRACKED ACCOUNT bypass. Xbox Network: Offline.")
                    consoleLogs.add("     - Target player name: \"${activeAcct.username}\" (Offline Mode Online!)")
                } else {
                    consoleLogs.add("👤 [ACCOUNT] Authentic Xbox / Microsoft Session synchronized with Discord Rich Presence.")
                }
            } else {
                consoleLogs.add("👤 [ACCOUNT] Proceeding as default Guest profile \"Player\"...")
            }

            delay(400)
            launchProgress = 100f
            consoleLogs.add("🎉 [LAUNCHED] JNI rendering success. Minecraft Bedrock Viewport created!")
            
            delay(300)
            isGameActive = true
            isLaunching = false
        }
    }

    // Helper: Mod Compatibility checker
    fun checkModCompatibility(mod: Mod, version: String): Boolean {
        // Simple checklist logic:
        // MCPE 1.0.0 check for Mod/Add-ons:
        if (mod.packType == "Mod/Add-on") {
            // Mod requires MCPE 1.0.0+
            val pieces = version.split(".")
            if (pieces.isEmpty()) return false
            val major = pieces[0].toIntOrNull() ?: 0
            if (major < 1) return false
        } else if (mod.packType == "Texture Pack") {
            // Texture requires 0.15.0+
            val pieces = version.split(".")
            if (pieces.size < 2) return false
            val major = pieces[0].toIntOrNull() ?: 0
            val minor = pieces[1].toIntOrNull() ?: 0
            if (major == 0 && minor < 15) return false
        }
        return true
    }

    // Macro CRUD
    fun addNewMacro() {
        if (newMacroName.isBlank()) return
        viewModelScope.launch {
            val newM = Macro(
                name = newMacroName,
                triggerKey = newMacroTrigger,
                isRepeatable = newMacroRepeat,
                delayMs = newMacroDelay,
                macroCommands = newMacroCmds,
                isEnabled = true
            )
            repository.insertMacro(newM)
            newMacroName = ""
            newMacroCmds = "tap_coords(540,960),wait(80)"
        }
    }

    fun toggleMacro(m: Macro) {
        viewModelScope.launch {
            repository.setMacroEnabled(m.id, !m.isEnabled)
        }
    }

    fun deleteMacro(m: Macro) {
        viewModelScope.launch {
            repository.deleteMacro(m)
        }
    }

    // Mod CRUD
    fun addNewMod() {
        if (newModName.isBlank()) return
        viewModelScope.launch {
            val m = Mod(
                name = newModName,
                packType = newModType,
                description = newModDesc.ifBlank { "Futuristic game overlay addon files." },
                sizeKb = (100..4000).random(),
                isEnabled = false,
                minRequiredVersion = newModMinVer
            )
            repository.insertMod(m)
            newModName = ""
            newModDesc = ""
        }
    }

    fun toggleMod(m: Mod) {
        viewModelScope.launch {
            repository.setModEnabled(m.id, !m.isEnabled)
        }
    }

    fun deleteMod(m: Mod) {
        viewModelScope.launch {
            repository.deleteMod(m)
        }
    }

    // Server CRUD
    fun addNewServer() {
        if (newServerName.isBlank() || newServerIp.isBlank()) return
        viewModelScope.launch {
            val s = Server(
                name = newServerName,
                ipAddress = newServerIp,
                port = newServerPort,
                isLocalHotspot = newServerIsHotspot,
                pingMs = if (newServerIsHotspot) (1..8).random() else (20..150).random(),
                activePlayers = if (newServerIsHotspot) 1 else (0..2000).random(),
                maxPlayers = if (newServerIsHotspot) 8 else 5000,
                serverMotd = "§d⚡ Cyber Bedrock Server §7[Custom Launcher Entry]"
            )
            repository.insertServer(s)
            newServerName = ""
            newServerIp = ""
            newServerPort = 19132
            newServerIsHotspot = false
        }
    }

    fun deleteServer(s: Server) {
        viewModelScope.launch {
            repository.deleteServer(s)
        }
    }

    // Account CRUD
    fun createCrackedAccount() {
        if (newAcctName.isBlank()) return
        viewModelScope.launch {
            val a = Account(
                username = newAcctName,
                isCracked = true,
                skinSeed = (100..999).random()
            )
            repository.insertAccount(a)
            newAcctName = ""

            // Link newly generated account immediately as active settings target
            val list = repository.allAccounts.firstOrNull() ?: emptyList()
            val freshAcct = list.firstOrNull { it.username == a.username }
            if (freshAcct != null) {
                val s = settings.value ?: LauncherSettings()
                repository.updateSettings(s.copy(activeAccountId = freshAcct.id))
            }
        }
    }

    fun switchActiveAccount(acctId: Int) {
        viewModelScope.launch {
            val s = settings.value ?: LauncherSettings()
            repository.updateSettings(s.copy(activeAccountId = acctId))
        }
    }

    fun deleteAccount(a: Account) {
        viewModelScope.launch {
            repository.deleteAccount(a)
            // Reset setting if it was active
            val s = settings.value ?: LauncherSettings()
            if (s.activeAccountId == a.id) {
                repository.updateSettings(s.copy(activeAccountId = null))
            }
        }
    }
}
