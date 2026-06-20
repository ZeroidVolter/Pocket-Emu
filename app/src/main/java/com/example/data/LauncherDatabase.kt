package com.example.data

import android.content.Context
import androidx.room.*

@Database(
    entities = [
        MinecraftVersion::class,
        LauncherSettings::class,
        Macro::class,
        Account::class,
        Mod::class,
        Server::class
    ],
    version = 2,
    exportSchema = false
)
abstract class LauncherDatabase : RoomDatabase() {
    abstract fun versionDao(): MinecraftVersionDao
    abstract fun settingsDao(): LauncherSettingsDao
    abstract fun macroDao(): MacroDao
    abstract fun accountDao(): AccountDao
    abstract fun modDao(): ModDao
    abstract fun serverDao(): ServerDao

    companion object {
        @Volatile
        private var INSTANCE: LauncherDatabase? = null

        fun getDatabase(context: Context): LauncherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LauncherDatabase::class.java,
                    "pocket_launcher_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
