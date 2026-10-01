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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.drawText

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

  val textMeasurer = rememberTextMeasurer() 
  
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
                        color = Color.White,
                        radius = 100f,
                        center = Offset(
                            x = size.width / 2,
                            y = size.height / 2
                            ),
                        style = Stroke(width = 2f)
                        )
                    val originText = textMeasurer.measure(
                      text = "ORIGIN",
                      style = TextStyle(
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                      )
                    )
                    drawText(
                      textLayoutResult = originText,
                      topLeft = Offset(
                        x = size.width / 2 - originText.size.width / 2,
                        y = size.height / 2 - originText.size.height / 2
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
