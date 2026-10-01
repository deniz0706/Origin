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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.animation.core.tween
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import kotlinx.coroutines.delay

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

  var animationStarted = remember { mutableStateOf(false) }
  var originCircleStarted = remember { mutableStateOf(false) }
  var originTextStarted = remember { mutableStateOf(false) }
  val textMeasurer = rememberTextMeasurer() 
  val lineProgress = animateFloatAsState(
    targetValue = if (animationStarted.value){
      1f
    } else {
      0f
    },
    animationSpec = tween(durationMillis = 3500)
  )
  val originCircleProgress = animateFloatAsState(
    targetValue = if (originCircleStarted.value){
      1f
    } else {
      0f
    },
    animationSpec = tween(durationMillis = 2000)
  )
  val originTextProgress = animateFloatAsState(
    targetValue = if (originTextStarted.value){
      1f
    } else {
      0f
    },
    animationSpec = tween(durationMillis = 1000)
  )
       
    LaunchedEffect(Unit) {
      originTextStarted.value = true
      delay(800)
      originCircleStarted.value = true
      delay (2000)
      animationStarted.value =  true
    }
    
    MaterialTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = Color.Black
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Canvas (
                    modifier = Modifier.fillMaxSize()
                    ){
                  val originCenter = Offset(
                    x = size.width / 2,
                    y = size.height / 2
                  )
                  val lifeCenter = Offset(
                    x = size.width / 2 + 250f,
                    y = size.height / 2 - 200f
                  )
                  val dx = lifeCenter.x - originCenter.x
                  val dy = lifeCenter.y - originCenter.y
                  val distance = kotlin.math.sqrt(dx*dx + dy*dy)
                  val originRatio = 100f / distance
                  val lifeRatio = 70f / distance
                  val lineStart = Offset(
                    x = originCenter.x + (dx*originRatio),
                    y = originCenter.y + (dy*originRatio)
                  )
                  val lineEnd = Offset(
                    x = lifeCenter.x - (dx*lifeRatio),
                    y = lifeCenter.y - (dy*lifeRatio)
                  )
                  val animatedEndX = lineStart.x + ((lineEnd.x - lineStart.x) * lineProgress.value)
                  val animatedEndY = lineStart.y + ((lineEnd.y - lineStart.y) * lineProgress.value)
                  val animatedEnd = Offset(
                    x = animatedEndX,
                    y = animatedEndY
                  )
                  drawLine(
                    color = Color.White,
                    start = lineStart,
                    end = animatedEnd,
                    strokeWidth = 2f
                  )
                  drawCircle(
                    color = Color.White,
                    radius = 70f,
                    center = lifeCenter,
                    style = Stroke(width = 2f)
                  )
                  drawArc(
                    startAngle = 0f,
                    sweepAngle = originCircleProgress.value * 360f,
                    style = Stroke(width = 2f),
                    size = Size(
                      width = 200f,
                      height = 200f
                    ),
                    color = Color.White,
                    topLeft = Offset(
                      x = originCenter.x - 100f,
                      y = originCenter.y - 100f
                    ),
                    useCenter = false
                  )
                  val originText = textMeasurer.measure(
                    text = "ORIGIN",
                    style = TextStyle(
                    color = Color.White.copy(
                      alpha = originTextProgress.value
                    ),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                   )
                )
                  val lifeText = textMeasurer.measure(
                    text = "YAŞAM",
                    style = TextStyle(
                      color = Color.White,
                      fontSize = 15.sp,
                      fontWeight = FontWeight.Bold,
                      letterSpacing = 1.sp
                    )
                  )
                  val originTextOffsetY = 25f * (1f - originTextProgress.value)
                  
                  drawText(
                    textLayoutResult = lifeText,
                    topLeft = Offset(
                      x = lifeCenter.x - lifeText.size.width / 2,
                      y = lifeCenter.y - lifeText.size.height / 2
                    )
                  )
                  drawText(
                    textLayoutResult = originText,
                    topLeft = Offset(
                      x = size.width / 2 - originText.size.width / 2,
                      y = size.height / 2 - originText.size.height / 2 + originTextOffsetY
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