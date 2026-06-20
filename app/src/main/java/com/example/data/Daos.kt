package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MinecraftVersionDao {
    @Query("SELECT * FROM minecraft_versions")
    fun getAllVersions(): Flow<List<MinecraftVersion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersions(versions: List<MinecraftVersion>)

    @Query("UPDATE minecraft_versions SET isInstalled = :isInstalled WHERE id = :id")
    suspend fun setInstalled(id: String, isInstalled: Boolean)
}

@Dao
interface LauncherSettingsDao {
    @Query("SELECT * FROM launcher_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<LauncherSettings?>

    @Query("SELECT * FROM launcher_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): LauncherSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: LauncherSettings)

    @Update
    suspend fun updateSettings(settings: LauncherSettings)
}

@Dao
interface MacroDao {
    @Query("SELECT * FROM macros ORDER BY id DESC")
    fun getAllMacros(): Flow<List<Macro>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacro(macro: Macro)

    @Delete
    suspend fun deleteMacro(macro: Macro)

    @Query("UPDATE macros SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setMacroEnabled(id: Int, isEnabled: Boolean)
}

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY lastActiveMillis DESC")
    fun getAllAccounts(): Flow<List<Account>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account)

    @Delete
    suspend fun deleteAccount(account: Account)

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Int): Account?
}

@Dao
interface ModDao {
    @Query("SELECT * FROM mods ORDER BY id DESC")
    fun getAllMods(): Flow<List<Mod>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMod(mod: Mod)

    @Delete
    suspend fun deleteMod(mod: Mod)

    @Query("UPDATE mods SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setModEnabled(id: Int, isEnabled: Boolean)
}

@Dao
interface ServerDao {
    @Query("SELECT * FROM servers ORDER BY isLocalHotspot DESC, id DESC")
    fun getAllServers(): Flow<List<Server>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: Server)

    @Delete
    suspend fun deleteServer(server: Server)
}
