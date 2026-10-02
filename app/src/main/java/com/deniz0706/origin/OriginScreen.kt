package com.deniz0706.origin

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.drawscope.DrawScope

private fun DrawScope.drawConnection(
    from: OriginNode,
    to: OriginNode,
    progress: Float
): Offset {
    val dx = to.center.x - from.center.x
    val dy = to.center.y - from.center.y
    val distance = kotlin.math.sqrt(dx * dx + dy * dy)

    val fromRatio = from.radius / distance
    val toRatio = to.radius / distance

    val lineStart = Offset(
      x = from.center.x + (dx * fromRatio),
      y = from.center.y + (dy * fromRatio)
    )

    val lineEnd = Offset(
      x = to.center.x - (dx * toRatio),
      y = to.center.y - (dy * toRatio)
    )

    val animatedEndX = lineStart.x + ((lineEnd.x - lineStart.x) * progress)
    val animatedEndY = lineStart.y + ((lineEnd.y - lineStart.y) * progress)

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

    return lineEnd
}

private fun DrawScope.drawNodeCircle(
    node: OriginNode,
    startAngle: Float,
    progress: Float
) {
    drawArc(
      startAngle = startAngle,
      sweepAngle = progress * 180f,
      style = Stroke(width = 3f),
      size = Size(
        width = node.radius * 2,
        height = node.radius * 2
      ),
      color = Color.White,
      topLeft = Offset(
        x = node.center.x - node.radius,
        y = node.center.y - node.radius
      ),
      useCenter = false
    )

    drawArc(
      startAngle = startAngle,
      sweepAngle = progress * -180f,
      style = Stroke(width = 3f),
      size = Size(
        width = node.radius * 2,
        height = node.radius * 2
      ),
      color = Color.White,
      topLeft = Offset(
        x = node.center.x - node.radius,
        y = node.center.y - node.radius
      ),
      useCenter = false
    )
}

@Composable
fun OriginApp() {

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
                  val origin = OriginNode(
                    name = "ORIGIN",
                    center = Offset(
                      x = size.width / 2,
                      y = size.height / 2
                    ),
                    radius = 180f
                  )
                  val life = OriginNode(
                    name = "YAŞAM",
                    center = Offset(
                      x = size.width / 2 + 450f,
                      y = size.height / 2 - 330f
                    ),
                    radius = 105f
                  )
                  val lineEnd = drawConnection(
                    from = origin,
                    to = life,
                    progress = lifeLineProgress.value
                  )
                  val lifeStartAngle = Math.toDegrees(
                    kotlin.math.atan2(
                      (lineEnd.y - life.center.y).toDouble(),
                      (lineEnd.x - life.center.x).toDouble()
                    )
                  ).toFloat()

                  drawNodeCircle(
                    node = life,
                    startAngle = lifeStartAngle,
                    progress = lifeCircleProgress.value
                  )

                  drawArc(
                    startAngle = 0f,
                    sweepAngle = originCircleProgress.value * 360f,
                    style = Stroke(width = 3f),
                    size = Size(
                      width = origin.radius * 2,
                      height = origin.radius * 2
                    ),
                    color = Color.White,
                    topLeft = Offset(
                      x = origin.center.x - origin.radius,
                      y = origin.center.y - origin.radius
                    ),
                    useCenter = false
                  )
                  val originText = textMeasurer.measure(
                    text = origin.name,
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
                    text = life.name,
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
                      x = life.center.x - lifeText.size.width / 2,
                      y = life.center.y - lifeText.size.height / 2
                    )
                  )
                  drawText(
                    textLayoutResult = originText,
                    topLeft = Offset(
                      x = origin.center.x - originText.size.width / 2,
                      y = origin.center.y - originText.size.height / 2 + originTextOffsetY
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