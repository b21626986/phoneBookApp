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

        setContent {
            val navController = rememberNavController()
            PhoneBookApp(viewModel = viewModel, navController = navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneBookApp(viewModel: ContactViewModel, navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "contacts"
    ) {
        // 1. Ana Ekran - Normal Composable
        composable("contacts") {
            ContactsScreen(viewModel = viewModel, navController = navController)
        }

        // 2. AddContactScreen
        composable("add") {
            AddContactScreen(viewModel = viewModel, navController = navController)
        }

        // 3. ProfileScreen - ID Tabanlı Navigasyon
        // Rota: profile/{contactId}?mode={mode}
        composable("profile/{contactId}?mode={mode}") { backStackEntry ->
            // URL'den gelen String ID'yi alıyoruz
            val contactIdString = backStackEntry.arguments?.getString("contactId")

            // ID'yi Long'a dönüştürüyoruz, başarısız olursa 0L varsayıyoruz
            val contactId = contactIdString?.toLongOrNull() ?: 0L

            // Mode parametresini alıyoruz
            val mode = backStackEntry.arguments?.getString("mode") ?: "view"

            // ProfileScreen'i Long ID ile çağırıyoruz
            ProfileScreen(
                contactId = contactId, // <<< ARTIK ID KULLANILIYOR
                viewModel = viewModel,
                navController = navController,
                mode = mode
            )
        }
    }
}
