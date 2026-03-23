package org.delcom.pam_2026_ifs23021_proyek1_fe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.navigation.AppNavGraph
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.theme.Pam2026ifs23021proyek1feTheme
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.AuthViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by authViewModel.isDarkMode.collectAsState()
            Pam2026ifs23021proyek1feTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph(authViewModel = authViewModel)
                }
            }
        }
    }
}