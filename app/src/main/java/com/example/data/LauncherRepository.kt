package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LauncherRepository(private val db: LauncherDatabase) {
    val allVersions: Flow<List<MinecraftVersion>> = db.versionDao().getAllVersions()
    val settingsFlow: Flow<LauncherSettings?> = db.settingsDao().getSettingsFlow()
    val allMacros: Flow<List<Macro>> = db.macroDao().getAllMacros()
    val allAccounts: Flow<List<Account>> = db.accountDao().getAllAccounts()
    val allMods: Flow<List<Mod>> = db.modDao().getAllMods()
    val allServers: Flow<List<Server>> = db.serverDao().getAllServers()

    suspend fun initializeDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        // Pre-populate settings if empty
        val currentSettings = db.settingsDao().getSettingsDirect()
        if (currentSettings == null) {
            db.settingsDao().saveSettings(LauncherSettings())
        }

        // Pre-populate minecraft versions
        val versionList = db.versionDao().getAllVersions().firstOrNull() ?: emptyList()
        if (versionList.isEmpty()) {
            db.versionDao().insertVersions(
                listOf(
                    MinecraftVersion(
                        id = "1.21.30",
                        releaseDate = "June 2026",
                        type = "Release",
                        isInstalled = true,
                        engineCompatible = "RenderDragon",
                        supportsMods = true,
                        supportsTextures = true,
                        sizeMb = 380,
                        description = "Latest Bedrock patch. Adds customized neon visual layouts, customized command prompts, and structural rendering updates."
                    ),
                    MinecraftVersion(
                        id = "1.20.80",
                        releaseDate = "April 2024",
                        type = "Release",
                        isInstalled = false,
                        engineCompatible = "RenderDragon",
                        supportsMods = true,
                        supportsTextures = true,
                        sizeMb = 350,
                        description = "Stable update. RenderDragon core engine optimization with experimental copper structures."
                    ),
                    MinecraftVersion(
                        id = "1.16.200",
                        releaseDate = "Dec 2020",
                        type = "Release",
                        isInstalled = false,
                        engineCompatible = "RenderDragon",
                        supportsMods = true,
                        supportsTextures = true,
                        sizeMb = 240,
                        description = "The Nether Update. First implementation of Android RenderDragon graphics API requirements."
                    ),
                    MinecraftVersion(
                        id = "1.12.0",
                        releaseDate = "July 2019",
                        type = "Release",
                        isInstalled = false,
                        engineCompatible = "GLES",
                        supportsMods = true,
                        supportsTextures = true,
                        sizeMb = 180,
                        description = "Village & Pillage Update. Extremely lightweight GLES renderer, highly customizable for slow/older chipsets."
                    ),
                    MinecraftVersion(
                        id = "1.0.0",
                        releaseDate = "Dec 2016",
                        type = "Release",
                        isInstalled = false,
                        engineCompatible = "GLES",
                        supportsMods = true,
                        supportsTextures = true,
                        sizeMb = 85,
                        description = "The Ender Update (MCPE 1.0.0). Introduces Ender Dragon and support for early .mcpack scripts."
                    ),
                    MinecraftVersion(
                        id = "0.15.0",
                        releaseDate = "June 2016",
                        type = "Release",
                        isInstalled = false,
                        engineCompatible = "GLES",
                        supportsMods = false,
                        supportsTextures = true,
                        sizeMb = 45,
                        description = "Friendly Update. Introduces Realms, pistons, custom texture packs, horse riding controls!"
                    ),
                    MinecraftVersion(
                        id = "0.14.0",
                        releaseDate = "Feb 2016",
                        type = "Release",
                        isInstalled = false,
                        engineCompatible = "GLES",
                        supportsMods = false,
                        supportsTextures = false,
                        sizeMb = 28,
                        description = "Classic Minecraft Pocket Edition (MCPE 0.14). Supports custom maps, redstone repeaters, witch houses."
                    )
                )
            )
        }

        // Pre-populate default macros
        val macroList = db.macroDao().getAllMacros().firstOrNull() ?: emptyList()
        if (macroList.isEmpty()) {
            db.macroDao().insertMacro(
                Macro(
                    name = "Auto Clicker",
                    triggerKey = "Volume Up",
                    isRepeatable = true,
                    delayMs = 80,
                    macroCommands = "tap_coords(540,960),wait(80)",
                    isEnabled = true
                )
            )
            db.macroDao().insertMacro(
                Macro(
                    name = "Safe Fast-Bridge",
                    triggerKey = "Side Action Button A",
                    isRepeatable = false,
                    delayMs = 150,
                    macroCommands = "key(S_DOWN),tap_coords(540,1100),wait(100),key(S_UP)",
                    isEnabled = false
                )
            )
            db.macroDao().insertMacro(
                Macro(
                    name = "Instant Golden Apple",
                    triggerKey = "Action Wheel R2",
                    isRepeatable = false,
                    delayMs = 200,
                    macroCommands = "key(SLOT_9),tap_hold(500,500,1600),key(SLOT_1)",
                    isEnabled = true
                )
            )
        }

        // Pre-populate default mods & textures
        val modList = db.modDao().getAllMods().firstOrNull() ?: emptyList()
        if (modList.isEmpty()) {
            db.modDao().insertMod(
                Mod(
                    name = "Pocket Cyber-HUD Controls",
                    packType = "Mod/Add-on",
                    description = "Adds responsive neon overlays and cyberpunk gamepads overlay.",
                    sizeKb = 2048,
                    isEnabled = true,
                    minRequiredVersion = "1.0.0"
                )
            )
            db.modDao().insertMod(
                Mod(
                    name = "F3 Debug Menu Overlay",
                    packType = "Mod/Add-on",
                    description = "Shows real-time coordinates, orientation, chunk data, and device telemetry.",
                    sizeKb = 420,
                    isEnabled = false,
                    minRequiredVersion = "1.0.0"
                )
            )
            db.modDao().insertMod(
                Mod(
                    name = "Neon Grid Shader (No-Lag)",
                    packType = "Texture Pack",
                    description = "Glow-in-the-dark block edges and high contrast dark background textures.",
                    sizeKb = 1536,
                    isEnabled = true,
                    minRequiredVersion = "0.15.0"
                )
            )
            db.modDao().insertMod(
                Mod(
                    name = "Faithful Neon 64x",
                    packType = "Texture Pack",
                    description = "Adds immersive sharp pixel grids with futuristic neon details.",
                    sizeKb = 8192,
                    isEnabled = false,
                    minRequiredVersion = "0.15.0"
                )
            )
        }

        // Pre-populate cracked accounts and online servers
        val accountList = db.accountDao().getAllAccounts().firstOrNull() ?: emptyList()
        if (accountList.isEmpty()) {
            db.accountDao().insertAccount(
                Account(
                    username = "GamerOffline99",
                    isCracked = true,
                    skinSeed = 101
                )
            )
            val settings = db.settingsDao().getSettingsDirect()
            if (settings != null) {
                // Fetch the newly added account and link it as active
                val added = db.accountDao().getAllAccounts().firstOrNull() ?: emptyList()
                if (added.isNotEmpty()) {
                    db.settingsDao().saveSettings(settings.copy(activeAccountId = added[0].id))
                }
            }
        }

        val serverList = db.serverDao().getAllServers().firstOrNull() ?: emptyList()
        if (serverList.isEmpty()) {
            db.serverDao().insertServer(
                Server(
                    name = "NetherGames Bedrock Lobby",
                    ipAddress = "play.nethergames.org",
                    port = 19132,
                    isLocalHotspot = false,
                    pingMs = 28,
                    activePlayers = 1450,
                    maxPlayers = 5000,
                    serverMotd = "§b[Vortex] §eNetherGames §7- SpeedCraft Active"
                )
            )
            db.serverDao().insertServer(
                Server(
                    name = "Galactic Arena PvP",
                    ipAddress = "pe.galactic-arena.net",
                    port = 19132,
                    isLocalHotspot = false,
                    pingMs = 52,
                    activePlayers = 240,
                    maxPlayers = 1000,
                    serverMotd = "§dFuturistic CyberPvP Lobbies §7[No Lag]"
                )
            )
            db.serverDao().insertServer(
                Server(
                    name = "Local WiFi Hotspot Server",
                    ipAddress = "192.168.43.1",
                    port = 19132,
                    isLocalHotspot = true,
                    pingMs = 3,
                    activePlayers = 1,
                    maxPlayers = 8,
                    serverMotd = "§aLocal Hotspot §f| §eHOST: PE_Pro_Player"
                )
            )
        }
    }

    suspend fun saveSettings(settings: LauncherSettings) = withContext(Dispatchers.IO) {
        db.settingsDao().saveSettings(settings)
    }

    suspend fun updateSettings(settings: LauncherSettings) = withContext(Dispatchers.IO) {
        db.settingsDao().updateSettings(settings)
    }

    suspend fun setVersionInstalled(id: String, isInstalled: Boolean) = withContext(Dispatchers.IO) {
        db.versionDao().setInstalled(id, isInstalled)
    }

    suspend fun insertMacro(macro: Macro) = withContext(Dispatchers.IO) {
        db.macroDao().insertMacro(macro)
    }

    suspend fun deleteMacro(macro: Macro) = withContext(Dispatchers.IO) {
        db.macroDao().deleteMacro(macro)
    }

    suspend fun setMacroEnabled(id: Int, isEnabled: Boolean) = withContext(Dispatchers.IO) {
        db.macroDao().setMacroEnabled(id, isEnabled)
    }

    suspend fun insertAccount(account: Account) = withContext(Dispatchers.IO) {
        db.accountDao().insertAccount(account)
    }

    suspend fun deleteAccount(account: Account) = withContext(Dispatchers.IO) {
        db.accountDao().deleteAccount(account)
    }

    suspend fun insertMod(mod: Mod) = withContext(Dispatchers.IO) {
        db.modDao().insertMod(mod)
    }

    suspend fun deleteMod(mod: Mod) = withContext(Dispatchers.IO) {
        db.modDao().deleteMod(mod)
    }

    suspend fun setModEnabled(id: Int, isEnabled: Boolean) = withContext(Dispatchers.IO) {
        db.modDao().setModEnabled(id, isEnabled)
    }

    suspend fun insertServer(server: Server) = withContext(Dispatchers.IO) {
        db.serverDao().insertServer(server)
    }

    suspend fun deleteServer(server: Server) = withContext(Dispatchers.IO) {
        db.serverDao().deleteServer(server)
    }
}
