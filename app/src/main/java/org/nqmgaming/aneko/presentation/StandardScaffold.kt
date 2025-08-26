package org.nqmgaming.aneko.presentation

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ramcosta.composedestinations.utils.isRouteOnBackStackAsState
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardScaffold(
    navController: NavController,
    showBottomBar: Boolean,
    items: List<BottomNavItem> = listOf(
        BottomNavItem.Home,
        BottomNavItem.Explore,
    ),
    content: @Composable (PaddingValues) -> Unit,
    viewModel: AnekoViewModel = hiltViewModel(),
) {
    val navigator = navController.rememberDestinationsNavigator()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isFirstLaunch = viewModel.isFirstLaunch.collectAsState().value
    var isShowingDialog by rememberSaveable { mutableStateOf(isFirstLaunch) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris: List<Uri>? ->
            uris?.forEach { uri ->
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    Timber.e(e, "Failed to persist URI permission")
                }

                scope.launch(Dispatchers.IO) {
                    val pkg = viewModel.importSkinFromUri(context, uri)
                    withContext(Dispatchers.Main) {
                        if (pkg != null) {
                            Toast.makeText(
                                context,
                                "Imported skin from ZIP: $pkg",
                                Toast.LENGTH_SHORT
                            ).show()
                            Timber.d("Package name from skin XML: $pkg")
                        } else {
                            Timber.e("Failed to read package name from skin XML in ZIP")
                        }
                    }
                }
            }
        }
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    NavigationBar(
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(0.dp))
                            .background(colorScheme.surface),
                        containerColor = colorScheme.background,
                        contentColor = colorScheme.onBackground,
                        content = {
                            items.forEach { item ->
                                val isCurrentDestOnBackStack by navController.isRouteOnBackStackAsState(
                                    item.direction
                                )
                                val color by animateColorAsState(
                                    targetValue = if (currentDestination?.route?.contains(item.route) == true) {
                                        colorScheme.primary
                                    } else {
                                        colorScheme.onBackground
                                    },
                                    label = "color_anim"
                                )
                                val iconScale by animateFloatAsState(
                                    targetValue = if (currentDestination?.route?.contains(item.route) == true) {
                                        1.2f
                                    } else {
                                        1f
                                    },
                                    label = "scale_anim"
                                )
                                NavigationBarItem(
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = colorScheme.primary,
                                        unselectedIconColor = colorScheme.onBackground,
                                        selectedTextColor = colorScheme.primary,
                                        unselectedTextColor = colorScheme.onBackground,
                                        indicatorColor = Color.Transparent,
                                    ),
                                    icon = {
                                        Icon(
                                            modifier = Modifier
                                                .size(25.dp)
                                                .scale(iconScale),
                                            painter = painterResource(id = item.icon),
                                            contentDescription = stringResource(item.title),
                                            tint = color
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(item.title),
                                            maxLines = 1,
                                            style = typography.bodySmall.copy(
                                                color = color,
                                                fontWeight = if (currentDestination?.route?.contains(
                                                        item.route
                                                    ) == true
                                                ) {
                                                    FontWeight.Bold
                                                } else {
                                                    FontWeight.Normal
                                                },
                                                fontSize = 10.sp
                                            ),
                                        )
                                    },
                                    alwaysShowLabel = true,
                                    selected = currentDestination?.route?.contains(item.route) == true,
                                    onClick = {
                                        if (isCurrentDestOnBackStack) {
                                            navigator.popBackStack(item.direction, false)
                                            return@NavigationBarItem
                                        }

                                        navigator.navigate(item.direction) {
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentDestination?.route?.contains(BottomNavItem.Home.route) == true) {
                FloatingActionButton(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary,
                    onClick = {
                        filePickerLauncher.launch(
                            arrayOf(
                                "application/zip",
                                "application/x-zip-compressed"
                            )
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null
                    )
                }
            }
        }
    ) { innerPadding ->
        content(innerPadding)
    }

    if (isFirstLaunch && isShowingDialog) {
        // show welcome dialog
        AlertDialog(
            containerColor = colorScheme.surface,
            onDismissRequest = {
                isShowingDialog = false
                viewModel.setFirstLaunchDone()
            },
            title = {
                Text("Impotant Notice", style = typography.headlineSmall)
            },
            text = {
                Column {
                    Text("Thank you for installing ANeko! From version 1.3.0 you are no longer needed to import skins by installing APKs. You can now import skin by selecting ZIP files directly from the app.")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // go to explore
                    isShowingDialog = false
                    viewModel.setFirstLaunchDone()
                    navigator.navigate(BottomNavItem.Explore.direction) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }) {
                    Text("Explore Now")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    isShowingDialog = false
                    viewModel.setFirstLaunchDone()
                    Toast.makeText(
                        context,
                        "You can access Explore from the bottom navigation bar later.",
                        Toast.LENGTH_LONG
                    ).show()
                }) {
                    Text("Maybe Later")
                }
            }
        )
    }
}
