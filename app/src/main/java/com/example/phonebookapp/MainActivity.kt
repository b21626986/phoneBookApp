package com.example.phonebookapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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

        setContent {
            // Navigation controller'ı burada oluşturuyoruz
            val navController = rememberNavController()

            // PhoneBookApp'e gerekli parametreleri geçiyoruz
            PhoneBookApp(viewModel = viewModel, navController = navController)
        }
        }
}

@Composable
fun PhoneBookApp(viewModel: ContactViewModel, navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "contacts"
    ) {
        composable("contacts") {
            ContactsScreen(viewModel = viewModel, navController = navController)
        }
        composable("add") {
            AddContactScreen(viewModel = viewModel, navController = navController)
        }
        composable("profile/{phone}") { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            ProfileScreen(phone = phone, viewModel = viewModel, navController = navController)
        }
    }
}

