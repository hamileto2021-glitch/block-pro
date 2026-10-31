package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.BlocketDatabase
import com.example.data.repository.BlocketRepository
import com.example.ui.components.CreateSalesOrderDialog
import com.example.ui.components.LogProductionBatchDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EmployeesScreen
import com.example.ui.screens.PaymentsScreen
import com.example.ui.screens.SalesScreen
import com.example.ui.screens.StocksScreen
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BlocketProTheme
import com.example.ui.theme.Slate500
import com.example.ui.viewmodel.BlocketViewModel
import com.example.ui.viewmodel.BlocketViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Reporting : Screen("reporting", "Reporting", Icons.Default.Dashboard)
    object Production : Screen("production", "Production", Icons.Default.Build)
    object Inventory : Screen("inventory", "Inventory", Icons.Default.Inventory2)
    object Sales : Screen("sales", "Sales", Icons.Default.ShoppingCart)
    object Payments : Screen("payments", "Payments", Icons.Default.AttachMoney)
    object Employees : Screen("employees", "Employees", Icons.Default.People)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val dbScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val database = BlocketDatabase.getDatabase(applicationContext, dbScope)
        val repository = BlocketRepository(database.blocketDao())
        val factory = BlocketViewModelFactory(repository)

        setContent {
            BlocketProTheme {
                val viewModel: BlocketViewModel = viewModel(factory = factory)
                BlocketApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlocketApp(viewModel: BlocketViewModel) {
    val navController = rememberNavController()

    // Global quick action dialog triggers
    var showGlobalLogBatchDialog by remember { mutableStateOf(false) }
    var showGlobalNewSaleDialog by remember { mutableStateOf(false) }

    val products by viewModel.blockProducts.collectAsStateWithLifecycle()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Reporting.route

    val navBarDestinations = listOf(
        Screen.Reporting,
        Screen.Production,
        Screen.Inventory,
        Screen.Sales,
        Screen.Payments,
        Screen.Employees
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AmberPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Blocket Pro",
                                fontWeight = FontWeight.Black,
                                fontSize = 19.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Concrete & Brick Production Management",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = com.example.ui.theme.Slate700,
                                maxLines = 1
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    IconButton(
                        onClick = { showGlobalLogBatchDialog = true },
                        modifier = Modifier.testTag("top_bar_log_batch")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Quick Log Batch",
                            tint = AmberPrimary
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                navBarDestinations.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = AmberPrimary,
                            indicatorColor = AmberPrimary,
                            unselectedIconColor = com.example.ui.theme.Slate700,
                            unselectedTextColor = com.example.ui.theme.Slate700
                        ),
                        modifier = Modifier.testTag("nav_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Reporting.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            enterTransition = {
                fadeIn(animationSpec = tween(220)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    tween(220)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(220)) + slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    tween(220)
                )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(220)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    tween(220)
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(220)) + slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    tween(220)
                )
            }
        ) {
            // 1. Reporting Dashboard Screen
            composable(Screen.Reporting.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToStocks = { navController.navigate(Screen.Inventory.route) },
                    onNavigateToSales = { navController.navigate(Screen.Sales.route) },
                    onNavigateToPayments = { navController.navigate(Screen.Payments.route) },
                    onNavigateToEmployees = { navController.navigate(Screen.Employees.route) },
                    onOpenLogBatch = { showGlobalLogBatchDialog = true },
                    onOpenNewSale = { showGlobalNewSaleDialog = true }
                )
            }

            // 2. Production Batches & Molding Screen
            composable(Screen.Production.route) {
                StocksScreen(
                    viewModel = viewModel,
                    initialTabIndex = 2
                )
            }

            // 3. Inventory & Raw Materials Screen
            composable(Screen.Inventory.route) {
                StocksScreen(
                    viewModel = viewModel,
                    initialTabIndex = 0
                )
            }

            // 4. Sales Orders & Receivables Screen
            composable(Screen.Sales.route) {
                SalesScreen(viewModel = viewModel)
            }

            // 5. Payment Ledger & Collections Screen
            composable(Screen.Payments.route) {
                PaymentsScreen(viewModel = viewModel)
            }

            // 6. Employees, Attendance & Payroll Screen
            composable(Screen.Employees.route) {
                EmployeesScreen(viewModel = viewModel)
            }
        }
    }

    if (showGlobalLogBatchDialog) {
        LogProductionBatchDialog(
            products = products,
            onDismiss = { showGlobalLogBatchDialog = false },
            onConfirm = { batchCode, productId, qty, defects, cement, sand, gravel, water, labor, sup, notes ->
                viewModel.logProduction(batchCode, productId, qty, defects, cement, sand, gravel, water, labor, sup, notes)
            }
        )
    }

    if (showGlobalNewSaleDialog) {
        CreateSalesOrderDialog(
            products = products,
            onDismiss = { showGlobalNewSaleDialog = false },
            onConfirm = { name, phone, address, prodId, qty, price, fee, deposit, method, notes ->
                viewModel.createSalesOrder(name, phone, address, prodId, qty, price, fee, deposit, method, notes)
            }
        )
    }
}
