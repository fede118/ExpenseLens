package com.section11.expenselens.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.section11.expenselens.ui.navigation.ExpenseLensNavGraph
import com.section11.expenselens.ui.theme.ExpenseLensTheme

@Composable
fun ExpenseLensApp() {
    val snackbarHostState = remember { SnackbarHostState() }

    ExpenseLensTheme(snackbarHostState = snackbarHostState) {
        val navController = rememberNavController()
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            ExpenseLensNavGraph(
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
                navController = navController
            )
        }
    }
}
