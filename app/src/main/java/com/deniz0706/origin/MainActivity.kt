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

  var lifeLineStarted = remember { mutableStateOf(false) }
  var originCircleStarted = remember { mutableStateOf(false) }
  var originTextStarted = remember { mutableStateOf(false) }
  val lifeTextStarted = remember { mutableStateOf(false) }
  var lifeCircleStarted = remember { mutableStateOf(false) }
  val textMeasurer = rememberTextMeasurer() 
  val lifeLineProgress = animateFloatAsState(
    targetValue = if (lifeLineStarted.value){
      1f
    } else {
      0f
    },
    animationSpec = tween(durationMillis = 2800)
  )
  val originCircleProgress = animateFloatAsState(
    targetValue = if (originCircleStarted.value){
      1f
    } else {
      0f
    },
    animationSpec = tween(durationMillis = 1900)
  )
  val originTextProgress = animateFloatAsState(
    targetValue = if (originTextStarted.value){
      1f
    } else {
      0f
    },
    animationSpec = tween(durationMillis = 1600)
  )
  val lifeTextProgress = animateFloatAsState(
    targetValue = if (lifeTextStarted.value){
      1f
    } else {
      0f
    },
    animationSpec = tween(durationMillis = 1000)
  )
  val lifeCircleProgress = animateFloatAsState(
    targetValue = if (lifeCircleStarted.value){
      1f
    } else {
      0f
    },
    animationSpec = tween(durationMillis = 1000)
  )
       
    LaunchedEffect(Unit) {
      originTextStarted.value = true
      delay(1200)
      originCircleStarted.value = true
      delay(1900)
      lifeLineStarted.value =  true
      delay(2800)
      lifeCircleStarted.value =  true
      delay(1000)
      lifeTextStarted.value = true
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
                    x = size.width / 2 + 450f,
                    y = size.height / 2 - 330f
                  )
                  val dx = lifeCenter.x - originCenter.x
                  val dy = lifeCenter.y - originCenter.y
                  val distance = kotlin.math.sqrt(dx*dx + dy*dy)
                  val originRatio = 180f / distance
                  val lifeRatio = 105f / distance
                  val lineStart = Offset(
                    x = originCenter.x + (dx*originRatio),
                    y = originCenter.y + (dy*originRatio)
                  )
                  val lineEnd = Offset(
                    x = lifeCenter.x - (dx*lifeRatio),
                    y = lifeCenter.y - (dy*lifeRatio)
                  )
                  val lifeStartAngle = Math.toDegrees(
                    kotlin.math.atan2(
                      (lineEnd.y - lifeCenter.y).toDouble(),
                      (lineEnd.x - lifeCenter.x).toDouble()
                    )
                  ).toFloat()
                  val animatedEndX = lineStart.x + ((lineEnd.x - lineStart.x) * lifeLineProgress.value)
                  val animatedEndY = lineStart.y + ((lineEnd.y - lineStart.y) * lifeLineProgress.value)
                  val animatedEnd = Offset(
                    x = animatedEndX,
                    y = animatedEndY
                  )
                  drawLine(
                    color = Color.White,
                    start = lineStart,
                    end = animatedEnd,
                    strokeWidth = 3f
                  )
                  drawArc(
                    startAngle = lifeStartAngle,
                    sweepAngle = lifeCircleProgress.value * 180f,
                    style = Stroke(width = 3f),
                    size = Size(
                      width = 210f,
                      height = 210f
                    ),
                    color = Color.White,
                    topLeft = Offset(
                      x = lifeCenter.x - 105f,
                      y = lifeCenter.y - 105f
                    ),
                    useCenter = false
                  )
                  drawArc(
                    startAngle = lifeStartAngle,
                    sweepAngle = lifeCircleProgress.value * -180f,
                    style = Stroke(width = 3f),
                    size = Size(
                      width = 210f,
                      height = 210f
                    ),
                    color = Color.White,
                    topLeft = Offset(
                      x = lifeCenter.x - 105f,
                      y = lifeCenter.y - 105f
                    ),
                    useCenter = false
                  )
                  drawArc(
                    startAngle = 0f,
                    sweepAngle = originCircleProgress.value * 360f,
                    style = Stroke(width = 3f),
                    size = Size(
                      width = 360f,
                      height = 360f
                    ),
                    color = Color.White,
                    topLeft = Offset(
                      x = originCenter.x - 180f,
                      y = originCenter.y - 180f
                    ),
                    useCenter = false
                  )
                  val originText = textMeasurer.measure(
                    text = "ORIGIN",
                    style = TextStyle(
                    color = Color.White.copy(
                      alpha = originTextProgress.value
                    ),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                   )
                )
                  val lifeText = textMeasurer.measure(
                    text = "YAŞAM",
                    style = TextStyle(
                      color = Color.White.copy(
                        alpha = lifeTextProgress.value
                      ),
                      fontSize = 20.sp,
                      fontWeight = FontWeight.Bold,
                      letterSpacing = 1.sp
                    )
                  )
                  val originTextOffsetY = 45f * (1f - originTextProgress.value)
                  
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