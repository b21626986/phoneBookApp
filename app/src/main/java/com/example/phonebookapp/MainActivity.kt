package com.example.phonebookapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api // Bottom Sheet için gerekli olabilir
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.phonebookapp.ui.screens.AddContactScreen
import com.example.phonebookapp.ui.screens.ContactsScreen
import com.example.phonebookapp.ui.screens.ProfileScreen
import com.example.phonebookapp.viewmodel.ContactViewModel

// Bottom Sheet/Dialog olarak açılacak rotalar için 'composable' yerine 'dialog' kullanıyoruz
import androidx.navigation.compose.dialog as bottomSheet

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

@OptIn(ExperimentalMaterial3Api::class) // BottomSheet yapısı için eklenmeli
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

        // 2. AddContactScreen - Artık normal Composable rotası
        composable("add") {
            // Buraya yeni özel dialog bileşenimizi ekleyeceğiz!
            // Ancak, AddContactScreen'i de buna uygun düzenlememiz gerekiyor.
            // Bu rotada şimdilik ekranı direkt çağırıyoruz, ana özelleştirme ekranda yapılacak.
            AddContactScreen(viewModel = viewModel, navController = navController)
        }

        // 3. ProfileScreen - Artık normal Composable rotası
        composable("profile/{phone}?mode={mode}") { backStackEntry ->
            // Aynı şekilde, ProfileScreen'i de buna uygun düzenlememiz gerekiyor.
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