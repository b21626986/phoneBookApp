package com.example.phonebookapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.phonebookapp.ui.screens.AddContactScreen
import com.example.phonebookapp.ui.screens.ContactsScreen
import com.example.phonebookapp.ui.screens.ProfileScreen
import com.example.phonebookapp.viewmodel.ContactViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: ContactViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sets the activity's content to Compose UI.
        setContent {
            val navController = rememberNavController()
            PhoneBookApp(viewModel = viewModel, navController = navController)
        }
    }
}

/**
 * Defines the entire navigation graph for the application using Compose Navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneBookApp(viewModel: ContactViewModel, navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "contacts"
    ) {
        //Contacts Screen Route
        composable("contacts") {
            ContactsScreen(viewModel = viewModel, navController = navController)
        }

        //Add Contact Screen Route
        composable("add") {
            AddContactScreen(viewModel = viewModel, navController = navController)
        }

        //Profile Screen Route
        composable("profile/{phone}?mode={mode}") { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            val mode = backStackEntry.arguments?.getString("mode") ?: "view"
            ProfileScreen(
                phone = phone,
                viewModel = viewModel,
                navController = navController,
                mode = mode
            )
        }
    }
}