package com.eninetworks.notas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.eninetworks.notas.ui.navigation.NavGraph
import com.eninetworks.notas.ui.theme.ItzliTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ItzliTheme {
                NavGraph()
            }
        }
    }
}
