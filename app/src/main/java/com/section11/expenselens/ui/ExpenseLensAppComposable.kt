package com.section11.expenselens.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.section11.expenselens.ui.navigation.ExpenseLensNavGraph
import com.section11.expenselens.ui.theme.ExpenseLensTheme

@Composable
fun ExpenseLensApp() {
    ExpenseLensTheme {
        val navController = rememberNavController()
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            ExpenseLensNavGraph(
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
                navController = navController
            )
        }
    }
}
