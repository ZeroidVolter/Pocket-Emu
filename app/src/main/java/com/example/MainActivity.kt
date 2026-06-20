package com.example

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.components.BackgroundLaser
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.LauncherViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("launcher_scaffold")
                ) { innerPadding ->
                    LauncherAppMain(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun LauncherAppMain(modifier: Modifier = Modifier) {
    val viewModel: LauncherViewModel = viewModel()
    val versions by viewModel.allVersions.collectAsState()
    val settingsState by viewModel.settings.collectAsState()
    val settings = settingsState ?: LauncherSettings()
    val macros by viewModel.allMacros.collectAsState()
    val accounts by viewModel.allAccounts.collectAsState()
    val mods by viewModel.allMods.collectAsState()
    val servers by viewModel.allServers.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val safeSettings = settings ?: LauncherSettings()

    BackgroundLaser(modifier = modifier) {
        if (viewModel.isGameActive) {
            // Simulated game is active, so we show the immersion simulation viewport!
            MinecraftGameplaySimulationScreen(viewModel = viewModel, currentSettings = safeSettings)
        } else {
            // Main Futuristic Launcher Console Dashboard
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isWide = maxWidth > 720.dp
                if (isWide) {
                    // Canonical Sidebar layout for wide displays
                    Row(modifier = Modifier.fillMaxSize()) {
                        LauncherSidebar(
                            activeTab = viewModel.currentTab,
                            onTabSelected = { viewModel.currentTab = it },
                            modifier = Modifier
                                .width(240.dp)
                                .fillMaxHeight()
                        )
                        Divider(
                            color = Color(0x3302EAF4),
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            LauncherContentArea(
                                activeTab = viewModel.currentTab,
                                viewModel = viewModel,
                                versions = versions,
                                settings = safeSettings,
                                macros = macros,
                                accounts = accounts,
                                mods = mods,
                                servers = servers,
                                context = context
                            )
                        }
                    }
                } else {
                    // Mobile-first bottom bar layout
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            LauncherContentArea(
                                activeTab = viewModel.currentTab,
                                viewModel = viewModel,
                                versions = versions,
                                settings = safeSettings,
                                macros = macros,
                                accounts = accounts,
                                mods = mods,
                                servers = servers,
                                context = context
                            )
                        }
                        Divider(
                            color = Color(0x3302EAF4),
                            modifier = Modifier.fillMaxWidth().height(1.dp)
                        )
                        LauncherBottomBar(
                            activeTab = viewModel.currentTab,
                            onTabSelected = { viewModel.currentTab = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // If currently triggering launch compiler overlay
                if (viewModel.isLaunching) {
                    LaunchTriggerLoadingOverlay(viewModel = viewModel)
                }
            }
        }
    }
}

// Sidebar component for Expanded screens
@Composable
fun LauncherSidebar(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xE10F1115))
            .padding(16.dp)
    ) {
        Text(
            text = "POCKET HUD",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color(0xFF06B6D4),
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Text(
            text = "BEDROCK EDITION v2.6",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        val navItems = listOf(
            SidebarTab("home", "Launcher Center", Icons.Default.FlashOn, Icons.Outlined.FlashOn),
            SidebarTab("settings", "Launcher Settings", Icons.Default.Settings, Icons.Outlined.Settings),
            SidebarTab("controls", "Cyber Macros", Icons.Default.Gamepad, Icons.Outlined.Gamepad),
            SidebarTab("mods", "Mods & Textures", Icons.Default.Layers, Icons.Outlined.Layers),
            SidebarTab("multiplayer", "Multiplayer hub", Icons.Default.Dns, Icons.Outlined.Dns),
            SidebarTab("accounts", "Profile secure", Icons.Default.AccountCircle, Icons.Outlined.AccountCircle)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            navItems.forEach { item ->
                val active = item.id == activeTab
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) Color(0x1D06B6D4) else Color.Transparent)
                        .clickable { onTabSelected(item.id) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (active) item.activeIcon else item.inactiveIcon,
                        contentDescription = item.label,
                        tint = if (active) Color(0xFF06B6D4) else Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = item.label,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color = if (active) Color(0xFF06B6D4) else Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Sidebar Footer status
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0x3394A3B8))
                .padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF22C55E))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "DISCORD RICH: OK",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color(0xFF22C55E)
                )
            }
        }
    }
}

// Bottom Bar for Compact mobile layout
@Composable
fun LauncherBottomBar(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.height(65.dp),
        containerColor = Color(0xF20F1115),
        tonalElevation = 6.dp
    ) {
        val menuItems = listOf(
            SidebarTab("home", "Launch", Icons.Default.FlashOn, Icons.Outlined.FlashOn),
            SidebarTab("settings", "Settings", Icons.Default.Settings, Icons.Outlined.Settings),
            SidebarTab("controls", "Macros", Icons.Default.Gamepad, Icons.Outlined.Gamepad),
            SidebarTab("mods", "Mods", Icons.Default.Layers, Icons.Outlined.Layers),
            SidebarTab("multiplayer", "Servers", Icons.Default.Dns, Icons.Outlined.Dns),
            SidebarTab("accounts", "Profiles", Icons.Default.AccountCircle, Icons.Outlined.AccountCircle)
        )

        menuItems.forEach { item ->
            val active = activeTab == item.id
            NavigationBarItem(
                selected = active,
                onClick = { onTabSelected(item.id) },
                icon = {
                    Icon(
                        imageVector = if (active) item.activeIcon else item.inactiveIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF06B6D4),
                    selectedTextColor = Color(0xFF06B6D4),
                    indicatorColor = Color(0x2206B6D4),
                    unselectedIconColor = Color(0xFF94A3B8),
                    unselectedTextColor = Color(0xFF94A3B8)
                )
            )
        }
    }
}

// Content Router
@Composable
fun LauncherContentArea(
    activeTab: String,
    viewModel: LauncherViewModel,
    versions: List<MinecraftVersion>,
    settings: LauncherSettings,
    macros: List<Macro>,
    accounts: List<Account>,
    mods: List<Mod>,
    servers: List<Server>,
    context: Context
) {
    AnimatedContent(
        targetState = activeTab,
        transitionSpec = {
            fadeIn(animationSpec = tween(150)) togetherWith fadeOut(animationSpec = tween(150))
        },
        label = "tab_switching_anim"
    ) { target ->
        when (target) {
            "home" -> HomeScreen(viewModel, versions, settings, accounts, context)
            "settings" -> SettingsScreen(viewModel, versions, settings, context)
            "controls" -> ControlMacrosScreen(viewModel, settings, macros, context)
            "mods" -> ModsTexturesScreen(viewModel, settings, mods, context)
            "multiplayer" -> MultiplayerScreen(viewModel, servers, settings, context)
            "accounts" -> ProfileAccountsScreen(viewModel, accounts, settings, context)
            else -> HomeScreen(viewModel, versions, settings, accounts, context)
        }
    }
}

// ---------------------- 1. HOME SCREEN ----------------------
@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    versions: List<MinecraftVersion>,
    settings: LauncherSettings,
    accounts: List<Account>,
    context: Context
) {
    val activeVer = versions.find { it.id == settings.selectedVersionId } ?: versions.firstOrNull()
    val activeAcct = accounts.find { it.id == settings.activeAccountId } ?: accounts.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Welcome Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "WELCOME BEYOND,",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 2.sp
                )
                Text(
                    text = activeAcct?.username?.uppercase() ?: "CRACKED_GUEST",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF06B6D4).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .background(Color(0x3306B6D4))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Wifi, "Hotspot", tint = Color(0xFF06B6D4), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (settings.hotspotMultiplayerEnabled) "HOTSPOT AP: ACTIVE" else "HOTSPOT AP: STNDBY",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFF06B6D4),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Futuristic Banner
        CyberPanel(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            glowingColor = Color(0xFF06B6D4)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF06B6D4), Color(0xFF6366F1))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SportsEsports,
                        contentDescription = "MCPE Controller",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "READY FOR INSTANT LAUNCH",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Selected profile bypasses security. Active controls will draw in customized holographic cyan.",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // Active version card
        CyberPanel(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            glowingColor = if (activeVer?.isInstalled == true) Color(0xFF22C55E) else Color(0xFF6366F1)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (activeVer?.isInstalled == true) Color(0xFF22C55E) else Color(0xFF6366F1))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VERSION SELECTED",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (activeVer?.isInstalled == true) Color(0x3322C55E) else Color(0x336366F1))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (activeVer?.isInstalled == true) "READY" else "UNINSTALLED",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeVer?.isInstalled == true) Color(0xFF22C55E) else Color(0xFF6366F1)
                        )
                    }
                }

                Text(
                    text = "MINECRAFT BEDROCK v${activeVer?.id ?: "1.21.30"}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    text = activeVer?.description ?: "No description configured.",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x1F94A3B8))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "ENGINE: ${settings.gameEngine}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x1F94A3B8))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "MAX DISTANCE: ${settings.renderDistance} CHUNKS",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Big holographic launch button!
        if (activeVer != null) {
            if (activeVer.isInstalled) {
                // Large Cyber play button
                Button(
                    onClick = { viewModel.triggerLaunchGame() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .testTag("launch_game_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF06B6D4), Color(0xFF3B82F6))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, "Launch", tint = Color.Black, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LAUNCH BEDROCK SIMULATOR",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            } else {
                // Installing controller
                val isInstalling = viewModel.downloadingVersionId == activeVer.id
                Button(
                    onClick = { viewModel.installVersion(activeVer.id) },
                    enabled = !isInstalling,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag("download_game_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6366F1)
                    )
                ) {
                    if (isInstalling) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "INSTALLING CORE RUNTIME (${(viewModel.downloadProgress * 100).toInt()}%)...",
                                fontFamily = FontFamily.Monospace,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudDownload, "Download", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DOWNLOAD CORE JNI (${activeVer.sizeMb} MB)",
                                fontFamily = FontFamily.Monospace,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // System Diagnostic report
        Text(
            text = "HUD DIAGNOSTICS & SYSTEM DATA",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        CyberPanel(
            modifier = Modifier.fillMaxWidth(),
            glowingColor = Color(0x3306B6D4)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                diagnosticLine("Active Profiler Bypass", "Offline (Cracked Autologin)", Color(0xFF22C55E))
                diagnosticLine("Discord Presence Tunnel", "OK (Channel Client Bound)", Color(0xFF22C55E))
                diagnosticLine("Touchscreen Macro Keys", "READY (3 Virtual Profiles mapped)", Color(0xFF06B6D4))
                diagnosticLine("Shader Overlay Engine", "RenderDragon Custom v2.1", Color(0xFF06B6D4))
                diagnosticLine("Physics Refresh rate", "Lag-Free (Optimized 60 FPS)", Color(0xFF22C55E))
            }
        }
    }
}

@Composable
fun diagnosticLine(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "> $label:",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8)
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
    }
}

// ---------------------- 2. SETTINGS SCREEN ----------------------
@Composable
fun SettingsScreen(
    viewModel: LauncherViewModel,
    versions: List<MinecraftVersion>,
    settings: LauncherSettings,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "LAUNCHER SETTINGS",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Configure graphic layers, selected version, rendering engines, and custom control properties.",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Version Selector Dropdown simulation
        Text(
            text = "TARGET EDITON/VERSION",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF06B6D4),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        var dropdownExpanded by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color(0x3306B6D4), RoundedCornerShape(8.dp))
                .background(Color(0x661E293B))
                .clickable { dropdownExpanded = true }
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "v${settings.selectedVersionId} - " + (versions.find { it.id == settings.selectedVersionId }?.engineCompatible ?: "RenderDragon"),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = Color.White
                )
                Icon(Icons.Default.ArrowDropDown, "Select", tint = Color(0xFF06B6D4))
            }
        }

        if (dropdownExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF06B6D4), RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B))
                    .padding(8.dp)
            ) {
                versions.forEach { ver ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectVersion(ver.id)
                                dropdownExpanded = false
                            }
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "v${ver.id} (${ver.type})",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (ver.isInstalled) "[INSTALLED]" else "[DOWNLOAD REQ]",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = if (ver.isInstalled) Color(0xFF22C55E) else Color(0x8B94A3B8)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Render Distance slider (Requirement: 2 up to 16 chunks)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0x661E293B)),
            border = BorderStroke(1.dp, Color(0x3394A3B8))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "RENDER DISTANCE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${settings.renderDistance} CHUNKS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF06B6D4)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = settings.renderDistance.toFloat(),
                    onValueChange = {
                        val chunks = it.toInt()
                        viewModel.updateLauncherSettings(settings.copy(renderDistance = chunks))
                    },
                    valueRange = 2f..16f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF06B6D4),
                        activeTrackColor = Color(0xFF06B6D4)
                    )
                )
                Text(
                    text = "Lower values yield incredible touch responsiveness. Limits chunk loading outside viewport.",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Advanced Settings Grid (Engines, Graphic Details, etc.)
        Text(
            text = "RENDER CONTROLLER CONFIGURATION",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF06B6D4),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Engine Toggle
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x661E293B))
                .border(1.dp, Color(0x3394A3B8), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "GAME RENDERING ENGINE",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("RenderDragon", "GLES", "BedrockForge").forEach { engine ->
                    val active = settings.gameEngine == engine
                    Button(
                        onClick = { viewModel.updateLauncherSettings(settings.copy(gameEngine = engine)) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (active) Color(0x4406B6D4) else Color(0x1F94A3B8)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, if (active) Color(0xFF06B6D4) else Color.Transparent, RoundedCornerShape(10.dp)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = engine,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (active) Color(0xFF06B6D4) else Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // In-game properties: FOV, Sensitivity, Audio Volume
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0x661E293B)),
            border = BorderStroke(1.dp, Color(0x3394A3B8))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // FOV
                sliderSettingItem(
                    title = "FIELD OF VIEW (FOV)",
                    value = settings.fov.toFloat(),
                    valueRange = 30f..110f,
                    displayLabel = "${settings.fov}°"
                ) { f ->
                    viewModel.updateLauncherSettings(settings.copy(fov = f.toInt()))
                }

                // Sensitivity
                sliderSettingItem(
                    title = "TOUCH SENSITIVITY",
                    value = settings.sensitivity.toFloat(),
                    valueRange = 1f..100f,
                    displayLabel = "${settings.sensitivity}%"
                ) { s ->
                    viewModel.updateLauncherSettings(settings.copy(sensitivity = s.toInt()))
                }

                // Volume
                sliderSettingItem(
                    title = "AUDIO ENGINE VOLUME",
                    value = settings.audioVolume.toFloat(),
                    valueRange = 0f..100f,
                    displayLabel = "${settings.audioVolume}%"
                ) { v ->
                    viewModel.updateLauncherSettings(settings.copy(audioVolume = v.toInt()))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Toggle Buttons list
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x661E293B))
                .border(1.dp, Color(0x3394A3B8), RoundedCornerShape(8.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            toggleSettingItem(
                title = "FPS Limit [60 FPS Max Boost]",
                description = "Cap rendering pipeline to avoid heating or battery drain.",
                isChecked = settings.isFpsLimit60
            ) { isChecked ->
                viewModel.updateLauncherSettings(settings.copy(isFpsLimit60 = isChecked))
            }

            toggleSettingItem(
                title = "Fancy graphics overlay",
                description = "Activates custom transparent clouds & soft anti-alias block edges.",
                isChecked = settings.isFancyGraphics
            ) { isChecked ->
                viewModel.updateLauncherSettings(settings.copy(isFancyGraphics = isChecked))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun sliderSettingItem(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    displayLabel: String,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = displayLabel,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Color(0xFF06B6D4)
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF06B6D4),
                activeTrackColor = Color(0xFF06B6D4)
            )
        )
    }
}

@Composable
fun toggleSettingItem(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text = title,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = description,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color(0xFF94A3B8)
            )
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF06B6D4),
                checkedTrackColor = Color(0xFF06B6D4).copy(alpha = 0.4f),
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = Color(0x44334155)
            )
        )
    }
}

// ---------------------- 3. CONTROLS & MACROS SCREEN ----------------------
@Composable
fun ControlMacrosScreen(
    viewModel: LauncherViewModel,
    settings: LauncherSettings,
    macros: List<Macro>,
    context: Context
) {
    var expandedColorPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "CYBER GLASS CONTROLS & MACRO EDITOR",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Set color of on-screen game-pad overlays directly and customize automatic macro keystrokes sequences.",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Control Color and Opacity Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0x661E293B)),
            border = BorderStroke(1.dp, Color(0x3394A3B8))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CUSTOM COLORING CHANGER",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF06B6D4),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Render current control color pill with selectable triggers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val colorsList = listOf(
                        "#06B6D4" to "Cyber Cyan",
                        "#6366F1" to "Holo Indigo",
                        "#22C55E" to "Acid Green",
                        "#EAB308" to "Laser Gold",
                        "#3B82F6" to "Holo Blue"
                    )

                    colorsList.forEach { p ->
                        val active = settings.controlsColorHex.equals(p.first, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(android.graphics.Color.parseColor(p.first)))
                                .border(2.dp, if (active) Color.White else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.updateLauncherSettings(settings.copy(controlsColorHex = p.first))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (active) {
                                Icon(Icons.Default.Check, "Active", tint = Color.Black, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Control opacity settings slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "CONTROL BUTTONS OPACITY",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${(settings.controlsOpacity * 100).toInt()}%",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFF06B6D4)
                    )
                }

                Slider(
                    value = settings.controlsOpacity,
                    onValueChange = {
                        viewModel.updateLauncherSettings(settings.copy(controlsOpacity = it))
                    },
                    valueRange = 0.1f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(android.graphics.Color.parseColor(settings.controlsColorHex)),
                        activeTrackColor = Color(android.graphics.Color.parseColor(settings.controlsColorHex))
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Macros Section: Header & Builder form
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MACRO SEQUENCES INSTALLED",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF06B6D4)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF06B6D4).copy(alpha = 0.8f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${macros.size} PRESETS",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        // Add Macro form expansion panel
        CyberPanel(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            glowingColor = Color(0xFF6366F1)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(16.dp)
            ) {
                Text(
                    text = "REGISTER NEW GAME MACRO",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = viewModel.newMacroName,
                    onValueChange = { viewModel.newMacroName = it },
                    label = { Text("Macro Name (e.g., AutoClicker)", fontFamily = FontFamily.Monospace) },
                    modifier = Modifier.fillMaxWidth().testTag("macro_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF06B6D4),
                        focusedLabelColor = Color(0xFF06B6D4),
                        unfocusedBorderColor = Color(0x3394A3B8)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = viewModel.newMacroCmds,
                    onValueChange = { viewModel.newMacroCmds = it },
                    label = { Text("Command Script Sequence", fontFamily = FontFamily.Monospace) },
                    modifier = Modifier.fillMaxWidth().testTag("macro_cmd_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF06B6D4),
                        focusedLabelColor = Color(0xFF06B6D4),
                        unfocusedBorderColor = Color(0x3394A3B8)
                    )
                )

                Text(
                    text = "Example format: tap_coords(540,960),wait(80),key(ATTACK)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = viewModel.newMacroRepeat,
                            onCheckedChange = { viewModel.newMacroRepeat = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF06B6D4))
                        )
                        Text(
                            text = "Infinite Loop Repeat",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = {
                            if (viewModel.newMacroName.isBlank()) {
                                Toast.makeText(context, "Please enter a macro name", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.addNewMacro()
                                Toast.makeText(context, "Macro Registered!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4)),
                        modifier = Modifier.testTag("save_macro_button")
                    ) {
                        Text(
                            text = "COMPILE MACRO",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // List Macros
        LazyColumn(
            modifier = Modifier.heightIn(max = 300.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(macros) { m ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x661E293B))
                        .border(1.dp, Color(0x1F94A3B8), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = m.name,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = m.macroCommands,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x226366F1))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "BIND: ${m.triggerKey}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    color = Color(0xFF6366F1)
                                )
                            }
                            if (m.isRepeatable) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0x2222C55E))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "LOOPED: ${m.delayMs}ms",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        color = Color(0xFF22C55E)
                                    )
                                }
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = m.isEnabled,
                            onCheckedChange = { viewModel.toggleMacro(m) },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF06B6D4))
                        )
                        IconButton(onClick = { viewModel.deleteMacro(m) }) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }
    }
}

// ---------------------- 4. MODS & TEXTURES SCREEN ----------------------
@Composable
fun ModsTexturesScreen(
    viewModel: LauncherViewModel,
    settings: LauncherSettings,
    mods: List<Mod>,
    context: Context
) {
    var minVerInput by remember { mutableStateOf("1.0.0") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "ADD-ONS & THEMES MANAGER",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Inject personalized behavior scripts (.mcpack) and resolution texture grids. Validated specifically against launcher constraints.",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Version Constraint alert / checks
        val pieces = settings.selectedVersionId.split(".")
        val major = pieces.getOrNull(0)?.toIntOrNull() ?: 0
        val minor = pieces.getOrNull(1)?.toIntOrNull() ?: 0

        val supportsMods = major >= 1
        val supportsTextures = major > 0 || minor >= 15

        if (!supportsMods || !supportsTextures) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x22EF4444)),
                border = BorderStroke(1.dp, Color(0xFFEF4444))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, "Warning", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VERSION LIMIT CHECK ALERT",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "You are targeting legacy MCPE ${settings.selectedVersionId}. " +
                                (if (!supportsMods) "\n❌ Custom Add-ons (.mcpack) are disabled (Requires 1.0.0+)." else "") +
                                (if (!supportsTextures) "\n❌ Custom Texture Packs are disabled (Requires 0.15.0+)." else ""),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color.White
                    )
                }
            }
        }

        // Add Mod Form
        CyberPanel(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            glowingColor = Color(0xFF06B6D4)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(16.dp)
            ) {
                Text(
                    text = "REGISTER ADD-ON PACKAGE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = viewModel.newModName,
                    onValueChange = { viewModel.newModName = it },
                    label = { Text("Pack Name (e.g., Space Block Shader)", fontFamily = FontFamily.Monospace) },
                    modifier = Modifier.fillMaxWidth().testTag("mod_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF06B6D4),
                        focusedLabelColor = Color(0xFF06B6D4),
                        unfocusedBorderColor = Color(0x3394A3B8)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Mod/Add-on", "Texture Pack").forEach { type ->
                        val active = viewModel.newModType == type
                        Button(
                            onClick = { viewModel.newModType = type },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (active) Color(0x4406B6D4) else Color(0x1F94A3B8)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = type,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = if (active) Color(0xFF06B6D4) else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.newModDesc,
                    onValueChange = { viewModel.newModDesc = it },
                    label = { Text("Brief Pack Description", fontFamily = FontFamily.Monospace) },
                    modifier = Modifier.fillMaxWidth().testTag("mod_desc_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF06B6D4),
                        focusedLabelColor = Color(0xFF06B6D4),
                        unfocusedBorderColor = Color(0x3394A3B8)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (viewModel.newModName.isBlank()) {
                            Toast.makeText(context, "Please configure a package name", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addNewMod()
                            Toast.makeText(context, "Mod added successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("add_mod_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4))
                ) {
                    Text(
                        text = "INSTALL COMPATIBLE PACKAGE",
                        fontFamily = FontFamily.Monospace,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // List Mods
        Text(
            text = "LOADED RESOURCES LIST",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.heightIn(max = 350.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(mods) { m ->
                val ok = viewModel.checkModCompatibility(m, settings.selectedVersionId)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x661E293B))
                        .border(1.dp, Color(0x1F94A3B8), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = m.name,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (ok) Color.White else Color(0x66FFFFFF),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (m.packType == "Mod/Add-on") Color(0x226366F1) else Color(0x2206B6D4))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (m.packType == "Mod/Add-on") "MCADDON" else "TEXTURE",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.sp,
                                    color = if (m.packType == "Mod/Add-on") Color(0xFF6366F1) else Color(0xFF06B6D4)
                                )
                            }
                        }

                        Text(
                            text = m.description,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "Target compliance: ${m.minRequiredVersion}+ | Size: ${m.sizeKb} KB",
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )

                        if (!ok) {
                            Text(
                                text = "⚠️ BLOCKED: Not compatible with target v${settings.selectedVersionId}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = m.isEnabled && ok,
                            enabled = ok,
                            onCheckedChange = { viewModel.toggleMod(m) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF22C55E),
                                checkedTrackColor = Color(0xFF22C55E).copy(alpha = 0.4f),
                                uncheckedThumbColor = Color(0xFF94A3B8)
                            )
                        )
                        IconButton(onClick = { viewModel.deleteMod(m) }) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }
    }
}

// ---------------------- 5. MULTIPLAYER SERVER LIST ----------------------
@Composable
fun MultiplayerScreen(
    viewModel: LauncherViewModel,
    servers: List<Server>,
    settings: LauncherSettings,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "COSMIC MULTIPLAYER PORTAL",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Join online Bedrock servers or toggle the local WiFi hotspot offline multiplayer server network for lan play.",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // WiFi Hotspot controller card (Offline multiplayer Requirement)
        CyberPanel(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            glowingColor = if (settings.hotspotMultiplayerEnabled) Color(0xFF22C55E) else Color(0xFF06B6D4)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PortableWifiOff,
                            contentDescription = "Hotspot",
                            tint = if (settings.hotspotMultiplayerEnabled) Color(0xFF22C55E) else Color(0xFF06B6D4),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "OFFLINE HOTSPOT INTERCONNECT",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Bypass MS and play over local offline hotspot networks",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Switch(
                        checked = settings.hotspotMultiplayerEnabled,
                        onCheckedChange = {
                            viewModel.updateLauncherSettings(settings.copy(hotspotMultiplayerEnabled = it))
                            Toast.makeText(
                                context,
                                if (it) "Offline Hotspot Server Launched! Local PE peers can join!" else "Local Hotspot Server Stopped",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF22C55E),
                            checkedTrackColor = Color(0xFF22C55E).copy(alpha = 0.4f)
                        )
                    )
                }

                if (settings.hotspotMultiplayerEnabled) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x1F22C55E))
                            .border(1.dp, Color(0x3322C55E), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "🔥 Broadcasting server on: 192.168.43.1:19132. No internet sync required. Use Hotspot with friends physically nearby!",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color(0xFF22C55E)
                        )
                    }
                }
            }
        }

        // Add server form
        CyberPanel(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            glowingColor = Color(0xFF6366F1)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(16.dp)
            ) {
                Text(
                    text = "REGISTER CUSTOM BEDROCK SERVER",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = viewModel.newServerName,
                    onValueChange = { viewModel.newServerName = it },
                    label = { Text("Server Name", fontFamily = FontFamily.Monospace) },
                    modifier = Modifier.fillMaxWidth().testTag("server_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6366F1),
                        focusedLabelColor = Color(0xFF6366F1),
                        unfocusedBorderColor = Color(0x3394A3B8)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = viewModel.newServerIp,
                        onValueChange = { viewModel.newServerIp = it },
                        label = { Text("Server IP/Address", fontFamily = FontFamily.Monospace) },
                        modifier = Modifier.weight(1.5f).testTag("server_ip_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6366F1),
                            focusedLabelColor = Color(0xFF6366F1),
                            unfocusedBorderColor = Color(0x3394A3B8)
                        )
                    )

                    OutlinedTextField(
                        value = viewModel.newServerPort.toString(),
                        onValueChange = { viewModel.newServerPort = it.toIntOrNull() ?: 19132 },
                        label = { Text("Port", fontFamily = FontFamily.Monospace) },
                        modifier = Modifier.weight(0.7f).testTag("server_port_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6366F1),
                            focusedLabelColor = Color(0xFF6366F1),
                            unfocusedBorderColor = Color(0x3394A3B8)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (viewModel.newServerName.isBlank() || viewModel.newServerIp.isBlank()) {
                            Toast.makeText(context, "Please configure name and address fields", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addNewServer()
                            Toast.makeText(context, "Server registered to lobby list!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("add_server_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                ) {
                    Text(
                        text = "REGISTER TO SERVER CATALOG",
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Servers list
        Text(
            text = "MULTIPLAYER SERVERS DETECTED",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.heightIn(max = 300.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(servers) { s ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x661E293B))
                        .border(1.dp, Color(0x1F94A3B8), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = s.name,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (s.isLocalHotspot) Color(0x2222C55E) else Color(0x2206B6D4))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (s.isLocalHotspot) "HOTSPOT AP" else "ONLINE",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.sp,
                                    color = if (s.isLocalHotspot) Color(0xFF22C55E) else Color(0xFF06B6D4)
                                )
                            }
                        }

                        Text(
                            text = "${s.ipAddress}:${s.port}",
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )

                        Text(
                            text = s.serverMotd,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF6366F1),
                            fontSize = 10.sp
                        )

                        Text(
                            text = "Ping Latency: ${s.pingMs} ms | Active: ${s.activePlayers}/${s.maxPlayers} players",
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "Handshaking server ${s.name} on thread... Success!", Toast.LENGTH_SHORT).show()
                                viewModel.triggerLaunchGame()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (s.isLocalHotspot) Color(0xFF22C55E) else Color(0xFF06B6D4)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Text(
                                text = "JOIN",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        IconButton(onClick = { viewModel.deleteServer(s) }) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }
    }
}

// ---------------------- 6. PROFILE / ACCOUNTS SECURITY ----------------------
@Composable
fun ProfileAccountsScreen(
    viewModel: LauncherViewModel,
    accounts: List<Account>,
    settings: LauncherSettings,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "REBEL PROFILES CORE",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Allow Cracked Accounts to bypass official sign-in requirements (offline mode). Sync seamlessly with Discord multiplayer bridges.",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Cracked Accounts Form
        CyberPanel(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            glowingColor = Color(0xFF06B6D4)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(16.dp)
            ) {
                Text(
                    text = "GENERATE OFFLINE CRACKED USERNAME",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = viewModel.newAcctName,
                    onValueChange = { viewModel.newAcctName = it },
                    label = { Text("Cracked Gamer Name (No Xbox Required)", fontFamily = FontFamily.Monospace) },
                    modifier = Modifier.fillMaxWidth().testTag("crack_username_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF06B6D4),
                        focusedLabelColor = Color(0xFF06B6D4),
                        unfocusedBorderColor = Color(0x3394A3B8)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (viewModel.newAcctName.isBlank()) {
                            Toast.makeText(context, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.createCrackedAccount()
                            Toast.makeText(context, "Cracked account activated/registered!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("add_crack_account_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4))
                ) {
                    Text(
                        text = "REGISTER BYPASS ACCOUNT",
                        fontFamily = FontFamily.Monospace,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Active account selector list
        Text(
            text = "REGISTERED LAUNCH PROFILES",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.heightIn(max = 300.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(accounts) { a ->
                val active = settings.activeAccountId == a.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (active) Color(0x3D06B6D4) else Color(0x661E293B))
                        .border(1.dp, if (active) Color(0xFF06B6D4) else Color(0x1F94A3B8), RoundedCornerShape(12.dp))
                        .clickable { viewModel.switchActiveAccount(a.id) }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Custom skin block box
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFFFAA00), Color(0xFFFF5500))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = a.username.take(1).uppercase(),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = a.username,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFF22C55E))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (a.isCracked) "CRACKED ACCOUNT (Xbox Bypass)" else "AUTHENTICATED",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    color = if (a.isCracked) Color(0xFF22C55E) else Color(0xFF6366F1)
                                )
                            }
                        }
                    }

                    Row {
                        if (active) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF06B6D4))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(onClick = { viewModel.deleteAccount(a) }) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }
    }
}

// ---------------------- SIMULATOR GAME SCREEN (Immersion Active) ----------------------
@Composable
fun MinecraftGameplaySimulationScreen(
    viewModel: LauncherViewModel,
    currentSettings: LauncherSettings
) {
    var playerLogs by remember { mutableStateOf("Ready to craft... D-pad connected.") }
    val coroutineScope = rememberCoroutineScope()

    // Key values mapping control customization properties
    val customHex = currentSettings.controlsColorHex
    val opacity = currentSettings.controlsOpacity
    val overlayColor = Color(android.graphics.Color.parseColor(customHex)).copy(alpha = opacity)

    BackgroundLaser(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Simulator Game Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF090B10))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📱 BEDROCK REBEL GAMEPLAY VIEWPORT",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Engine: [${currentSettings.gameEngine}] | Render Distance: ${currentSettings.renderDistance} Chunks | FOV: ${currentSettings.fov}",
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF8B9BB4),
                        fontSize = 10.sp
                    )
                }

                Button(
                    onClick = { viewModel.triggerLaunchGame() }, // Toggle back off
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "CLOSE SIMULATOR",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }

            // Central 3D canvas viewport rendering cubes & coordinates
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF141A29))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Interactive background cube structure graphics
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Drawing beautiful isometric retro neon block structures
                    drawRect(
                        color = Color(0x3BBD93F9),
                        topLeft = Offset(w / 2f - 140f, h / 2f - 140f),
                        size = androidx.compose.ui.geometry.Size(280f, 280f)
                    )
                    drawRect(
                        color = Color(0xFF090B10),
                        topLeft = Offset(w / 2f - 100f, h / 2f - 100f),
                        size = androidx.compose.ui.geometry.Size(200f, 200f)
                    )
                    // Neon edges
                    val edgeColor = Color(android.graphics.Color.parseColor(customHex))
                    drawLine(edgeColor, Offset(w / 2f - 140f, h / 2f - 140f), Offset(w / 2f + 140f, h / 2f - 140f), 4f)
                    drawLine(edgeColor, Offset(w / 2f - 140f, h / 2f + 140f), Offset(w / 2f + 140f, h / 2f + 140f), 4f)
                    drawLine(edgeColor, Offset(w / 2f - 140f, h / 2f - 140f), Offset(w / 2f - 140f, h / 2f + 140f), 4f)
                    drawLine(edgeColor, Offset(w / 2f + 140f, h / 2f - 140f), Offset(w / 2f + 140f, h / 2f + 140f), 4f)
                }

                // Internal telemetry logs overlay
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x9E090B10))
                                .padding(6.dp)
                        ) {
                            Text(
                                text = "XYZ: 240, 64, -1023\nFPS: 60 / GPU: Qualcomm GLES-Core v3\nChunks Loaded: ${currentSettings.renderDistance}",
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF22C55E),
                                fontSize = 9.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x9E090B10))
                                .padding(6.dp)
                        ) {
                            Text(
                                text = "ACTIVE MODS INJECTED\n- Cyber HUD Controls (Active)\n- Discord Multicast (Online)",
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF06B6D4),
                                fontSize = 9.sp
                            )
                        }
                    }

                    // Simulated feed in center
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x9E090B10))
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚡ RENDERDRAGON SIMULATION TELEMETRY",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6366F1),
                            fontSize = 11.sp
                        )
                        Text(
                            text = playerLogs,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Simulated controller gamepad layout! Respects CUSTOM CHOSEN HEX & OPACITY
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Left Controller (Dpad simulation)
                        Column(
                            modifier = Modifier.size(110.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(overlayColor)
                                    .clickable { playerLogs = "[GAMEPLAY] Player moved FORWARD. Map chunk buffer loaded." },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ArrowDropUp, "Up", tint = Color.Black)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(overlayColor)
                                        .clickable { playerLogs = "[GAMEPLAY] Player strafed LEFT. FOV shifted." },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ArrowLeft, "Left", tint = Color.Black)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(overlayColor.copy(alpha = overlayColor.alpha * 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.Black)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(overlayColor)
                                        .clickable { playerLogs = "[GAMEPLAY] Player strafed RIGHT." },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ArrowRight, "Right", tint = Color.Black)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(overlayColor)
                                    .clickable { playerLogs = "[GAMEPLAY] Player moved BACKWARD." },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ArrowDropDown, "Down", tint = Color.Black)
                            }
                        }

                        // Hotbar Slot items mapping
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                                .height(38.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x76000000)),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (1..5).forEach { i ->
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .border(1.dp, overlayColor, RoundedCornerShape(4.dp))
                                        .clickable { playerLogs = "[GAMEPLAY] Active Slot changed to: Slot #$i" },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "$i", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = overlayColor)
                                }
                            }
                        }

                        // Right Controller: Jump, Attack & MACRO QUICK SHOOTER
                        Column(
                            modifier = Modifier.size(110.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            // High performance Macro Triggers test
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF06B6D4))
                                    .clickable {
                                        coroutineScope.launch {
                                            playerLogs = "⚡ [MACRO] Running Action: Click loop init..."
                                            delay(200)
                                            playerLogs = "⚡ [MACRO] Auto-Click (Slot 9 coordinate fire) -> OK!"
                                            delay(300)
                                            playerLogs = "⚡ [MACRO] Key event completed. Slot swapped back."
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                  Text(
                                      text = "FAST MACRO",
                                      fontFamily = FontFamily.Monospace,
                                      fontSize = 9.sp,
                                      fontWeight = FontWeight.Bold,
                                      color = Color.Black
                                  )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(21.dp))
                                    .background(overlayColor)
                                    .clickable { playerLogs = "[GAMEPLAY] Player triggered ATTACK! Handshaking block breaking." },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "SWORD", fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(21.dp))
                                    .background(overlayColor)
                                    .clickable { playerLogs = "[GAMEPLAY] JUMP! Player position upward (Y=65.2)" },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "JUMP", fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------- LOG / COMPILER OVERLAY ----------------------
@Composable
fun LaunchTriggerLoadingOverlay(viewModel: LauncherViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF9090B10))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().widthIn(max = 550.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Futuristic spinning tech circle
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .border(2.dp, Color(0xFF06B6D4), RoundedCornerShape(40.dp)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(54.dp),
                    color = Color(0xFF6366F1),
                    strokeWidth = 3.dp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "COMPILING JNI GRAPHICS VIEWPORT",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )

            // Dynamic progress bar
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = viewModel.launchProgress / 100f,
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = Color(0xFF06B6D4),
                trackColor = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Compiler/Telemetry logs feed
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x661E293B)),
                border = BorderStroke(1.dp, Color(0x3306B6D4))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(viewModel.consoleLogs) { log ->
                        Text(
                            text = log,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = if (log.contains("✅") || log.contains("SUCCESS")) {
                                Color(0xFF22C55E)
                            } else if (log.contains("❌") || log.contains("BLOCKED")) {
                                Color(0xFFEF4444)
                            } else if (log.contains("⚡") || log.contains("BOOT")) {
                                Color(0xFF06B6D4)
                            } else {
                                Color(0xFF94A3B8)
                            }
                        )
                    }
                }
            }
        }
    }
}

// Custom reusable components
@Composable
fun CyberPanel(
    modifier: Modifier = Modifier,
    glowingColor: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x661E293B)) // bg-slate-800/40 (Slate 800 is #1E293B)
            .border(1.dp, glowingColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp)) // border-slate-700/50 with glow accent
    ) {
        content()
    }
}

// Data holder
data class SidebarTab(
    val id: String,
    val label: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
)
