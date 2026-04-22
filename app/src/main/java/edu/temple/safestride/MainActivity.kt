package edu.temple.safestride

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import edu.temple.safestride.ui.SafeStrideApp
import edu.temple.safestride.viewmodel.SafeStrideViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SafeStrideViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SafeStrideApp(viewModel = viewModel)
        }
    }
}