package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "minecraft_versions")
data class MinecraftVersion(
    @PrimaryKey val id: String, // e.g. "1.21.2", "1.16.200", "1.0.0", "0.15.0", "0.14.0"
    val releaseDate: String,
    val type: String, // "Release", "Beta", "Alpha"
    val isInstalled: Boolean = false,
    val engineCompatible: String, // "RenderDragon" or "GLES"
    val supportsMods: Boolean, // MCPE 1.0+
    val supportsTextures: Boolean, // MCPE 0.15.0+
    val sizeMb: Int,
    val description: String
)

@Entity(tableName = "launcher_settings")
data class LauncherSettings(
    @PrimaryKey val id: Int = 1, // Singleton settings
    val selectedVersionId: String = "1.21.2",
    val renderDistance: Int = 8, // 2 to 16
    val resolutionWidth: Int = 1280,
    val resolutionHeight: Int = 720,
    val isFpsLimit60: Boolean = true,
    val isFancyGraphics: Boolean = true,
    val isAntiAliasingEnabled: Boolean = true,
    val fov: Int = 70, // 30 to 110
    val sensitivity: Int = 50, // 0 to 100
    val audioVolume: Int = 80, // 0 to 100
    val gameEngine: String = "RenderDragon", // GLES, RenderDragon, BedrockForge
    val controlsColorHex: String = "#02EAF4", // Cyberpunk cyan
    val controlsOpacity: Float = 0.7f, // 0.1 to 1.0
    val hotspotMultiplayerEnabled: Boolean = false,
    val activeAccountId: Int? = null
)

@Entity(tableName = "macros")
data class Macro(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val triggerKey: String, // e.g. "Volume Up", "On Screen Button A"
    val isRepeatable: Boolean = false,
    val delayMs: Int = 100,
    val macroCommands: String, // Comma-separated actions, e.g., "tap_coords(300,500),wait(50),key(ATTACK)"
    val isEnabled: Boolean = true
)

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val isCracked: Boolean = true, // If true, crack account without MS Sign-In
    val skinSeed: Int = 101, // Custom skin background ID
    val lastActiveMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "mods")
data class Mod(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val packType: String, // "Mod/Add-on" or "Texture Pack"
    val description: String,
    val sizeKb: Int,
    val isEnabled: Boolean = false,
    val minRequiredVersion: String // MCPE 1.0+ for mods, 0.15.0+ for textures
)

@Entity(tableName = "servers")
data class Server(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val ipAddress: String,
    val port: Int = 19132,
    val isLocalHotspot: Boolean = false,
    val pingMs: Int = 45,
    val activePlayers: Int = 120,
    val maxPlayers: Int = 500,
    val serverMotd: String = "§aSurvival Quest §7[1.20+]"
)
