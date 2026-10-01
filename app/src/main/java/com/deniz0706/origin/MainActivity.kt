package com.deniz0706.origin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OriginApp()
        }
    }
}

@Composable
private fun OriginApp() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Canvas (
                    modifier = Modifier.fillMaxSize()
                    ){
                    drawCircle(
                        color = Color.white,
                        radius = 20f,
                        center = Offset(
                            x = size.width / 2,
                            y = size.height / 2
                            )
                        )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OriginAppPreview() {
    OriginApp()
}
