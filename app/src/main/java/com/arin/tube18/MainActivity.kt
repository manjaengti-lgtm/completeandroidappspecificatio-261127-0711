package com.arin.tube18

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arin.tube18.ui.AppNav
import com.arin.tube18.ui.appContainer
import com.arin.tube18.ui.screens.GateScreen
import com.arin.tube18.ui.theme.TubeTheme
import com.arin.tube18.viewmodel.MainViewModel
import com.arin.tube18.viewmodel.VmFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            TubeTheme {
                Root(onLeave = { finish() })
            }
        }
    }
}

@Composable
private fun Root(onLeave: () -> Unit) {
    val container = appContainer()
    val vm: MainViewModel = viewModel(factory = VmFactory.main(container))
    val accepted by vm.ageAccepted.collectAsStateWithLifecycle()
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (accepted) {
            null -> Box(Modifier.fillMaxSize())
            false -> GateScreen(onAccept = vm::accept, onLeave = onLeave)
            true -> AppNav()
        }
    }
}
