package edu.temple.safestride.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import edu.temple.safestride.viewmodel.SafeStrideViewModel

@Composable
fun SafeStrideApp(viewModel: SafeStrideViewModel) {
    MaterialTheme {
        SafeStrideScreen(viewModel = viewModel)
    }
}