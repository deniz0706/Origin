package com.deniz0706.origin

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val PlatformSurface = Color(0xFFF1F4F6)
private val PlatformShadow = Color(0xFF161B22)
private val PlatformEdge = Color(0xFFFFFFFF)
private val PlatformBlue = Color(0xFF238CFF)
private val PlatformBlueSoft = Color(0xFF73B7FF)

private fun DrawScope.drawConnection(
    from: OriginNode,
    to: OriginNode,
    progress: Float
): Float {
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

    val animatedEnd = Offset(
      x = lineStart.x + ((lineEnd.x - lineStart.x) * progress),
      y = lineStart.y + ((lineEnd.y - lineStart.y) * progress)
    )

    drawLine(
      color = Color.White.copy(alpha = 0.78f),
      start = lineStart,
      end = animatedEnd,
      strokeWidth = 3f
    )

    return Math.toDegrees(
      kotlin.math.atan2(
        (lineEnd.y - to.center.y).toDouble(),
        (lineEnd.x - to.center.x).toDouble()
      )
    ).toFloat()
}

private fun DrawScope.drawNodePlatform(
    node: OriginNode,
    startAngle: Float,
    progress: Float
) {
    if (progress <= 0f) return

    val diameter = node.radius * 2

    val platformSize = Size(
      width = diameter,
      height = diameter
    )

    val platformTopLeft = Offset(
      x = node.center.x - node.radius,
      y = node.center.y - node.radius
    )

    val shadowOffset = 9f

    val shadowTopLeft = Offset(
      x = platformTopLeft.x,
      y = platformTopLeft.y + shadowOffset
    )

    /*
     * ALT KATMAN
     *
     * Platformun birkaç piksel aşağısında duran koyu disk.
     * Beyaz yüzeyin havada asılı bir parça gibi algılanmasını sağlıyor.
     */
    drawOval(
      color = PlatformShadow.copy(
        alpha = 0.80f * progress
      ),
      topLeft = shadowTopLeft,
      size = platformSize
    )

    /*
     * YÜZEY
     *
     * Progress arttıkça platformun iç yüzeyi beliriyor.
     */
    drawOval(
      color = PlatformSurface.copy(
        alpha = 0.96f * progress
      ),
      topLeft = platformTopLeft,
      size = platformSize
    )

    /*
     * HAFİF İÇ DERİNLİK
     *
     * Yüzeyin tamamen düz beyaz bir buton gibi görünmesini engelliyor.
     */
    scale(
      scaleX = 0.90f,
      scaleY = 0.90f,
      pivot = node.center
    ) {
      drawOval(
        color = Color(0xFFDCE2E8).copy(
          alpha = 0.28f * progress
        ),
        topLeft = platformTopLeft,
        size = platformSize
      )
    }

    /*
     * MAVİ İÇ RIM
     *
     * Çok hafif. Platformu neon tabelaya çevirmeden
     * Origin'in mavi görsel dilini yüzeye taşıyor.
     */
    drawCircle(
      color = PlatformBlueSoft.copy(
        alpha = 0.16f * progress
      ),
      radius = node.radius - 7f,
      center = node.center,
      style = Stroke(width = 3f)
    )

    /*
     * MEVCUT AÇILMA ANİMASYONU
     *
     * Çizgi node'a ulaştığı noktadan çember iki yana doğru büyüyor.
     */
    drawArc(
      startAngle = startAngle,
      sweepAngle = progress * 180f,
      style = Stroke(width = 4f),
      size = platformSize,
      color = PlatformEdge,
      topLeft = platformTopLeft,
      useCenter = false
    )

    drawArc(
      startAngle = startAngle,
      sweepAngle = progress * -180f,
      style = Stroke(width = 4f),
      size = platformSize,
      color = PlatformEdge,
      topLeft = platformTopLeft,
      useCenter = false
    )
}

private fun DrawScope.drawNodeText(
    node: OriginNode,
    textMeasurer: TextMeasurer,
    progress: Float
) {
    /*
     * Önce geniş ve saydam yazı:
     * mavi ışığın yüzey üzerinde yayılması.
     */
    val glowText = textMeasurer.measure(
      text = node.name,
      style = TextStyle(
        color = PlatformBlue.copy(
          alpha = 0.20f * progress
        ),
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.4.sp
      )
    )

    drawText(
      textLayoutResult = glowText,
      topLeft = Offset(
        x = node.center.x - glowText.size.width / 2,
        y = node.center.y - glowText.size.height / 2 + 2f
      )
    )

    /*
     * Hafif koyu alt baskı:
     * yazının platform yüzeyine gömülmüş hissini güçlendiriyor.
     */
    val engravedText = textMeasurer.measure(
      text = node.name,
      style = TextStyle(
        color = Color(0xFF0A315A).copy(
          alpha = 0.42f * progress
        ),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
      )
    )

    drawText(
      textLayoutResult = engravedText,
      topLeft = Offset(
        x = node.center.x - engravedText.size.width / 2,
        y = node.center.y - engravedText.size.height / 2 + 2f
      )
    )

    /*
     * Asıl mavi yazı.
     * Koyu baskının 2 px üstünde olduğu için hafif oyuk hissi oluşuyor.
     */
    val nodeText = textMeasurer.measure(
      text = node.name,
      style = TextStyle(
        color = PlatformBlue.copy(
          alpha = progress
        ),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
      )
    )

    drawText(
      textLayoutResult = nodeText,
      topLeft = Offset(
        x = node.center.x - nodeText.size.width / 2,
        y = node.center.y - nodeText.size.height / 2
      )
    )
}

private fun DrawScope.drawOriginCircle(
    node: OriginNode,
    progress: Float
) {
    drawArc(
      startAngle = 0f,
      sweepAngle = progress * 360f,
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

private fun DrawScope.drawOriginText(
    node: OriginNode,
    textMeasurer: TextMeasurer,
    progress: Float
) {
    val originText = textMeasurer.measure(
      text = node.name,
      style = TextStyle(
        color = Color.White.copy(
          alpha = progress
        ),
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
      )
    )

    val textOffsetY = 45f * (1f - progress)

    drawText(
      textLayoutResult = originText,
      topLeft = Offset(
        x = node.center.x - originText.size.width / 2,
        y = node.center.y - originText.size.height / 2 + textOffsetY
      )
    )
}

@Composable
fun OriginApp() {

  val lineStarted = remember { mutableStateOf(false) }
  val originCircleStarted = remember { mutableStateOf(false) }
  val originTextStarted = remember { mutableStateOf(false) }
  val nodeCircleStarted = remember { mutableStateOf(false) }
  val nodeTextStarted = remember { mutableStateOf(false) }

  val textMeasurer = rememberTextMeasurer()

  val lineProgress = animateFloatAsState(
    targetValue = if (lineStarted.value) {
      1f
    } else {
      0f
    },
    animationSpec = tween(durationMillis = 2800)
  )

  val originCircleProgress = animateFloatAsState(
    targetValue = if (originCircleStarted.value) {
      1f
    } else {
      0f
    },
    animationSpec = tween(durationMillis = 1900)
  )

  val originTextProgress = animateFloatAsState(
    targetValue = if (originTextStarted.value) {
      1f
    } else {
      0f
    },
    animationSpec = tween(durationMillis = 1600)
  )

  val nodeCircleProgress = animateFloatAsState(
    targetValue = if (nodeCircleStarted.value) {
      1f
    } else {
      0f
    },
    animationSpec = tween(durationMillis = 1000)
  )

  val nodeTextProgress = animateFloatAsState(
    targetValue = if (nodeTextStarted.value) {
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

    lineStarted.value = true
    delay(2800)

    nodeCircleStarted.value = true
    delay(1000)

    nodeTextStarted.value = true
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
        Canvas(
          modifier = Modifier.fillMaxSize()
        ) {
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

          val universe = OriginNode(
            name = "EVREN",
            center = Offset(
              x = size.width / 2 - 450f,
              y = size.height / 2 - 330f
            ),
            radius = 105f
          )

          val math = OriginNode(
            name = "MATEMATİK",
            center = Offset(
              x = size.width / 2 - 450f,
              y = size.height / 2 + 330f
            ),
            radius = 105f
          )

          val physics = OriginNode(
            name = "FİZİK",
            center = Offset(
              x = size.width / 2 + 450f,
              y = size.height / 2 + 330f
            ),
            radius = 105f
          )

          val lifeStartAngle = drawConnection(
            from = origin,
            to = life,
            progress = lineProgress.value
          )

          val universeStartAngle = drawConnection(
            from = origin,
            to = universe,
            progress = lineProgress.value
          )

          val mathStartAngle = drawConnection(
            from = origin,
            to = math,
            progress = lineProgress.value
          )

          val physicsStartAngle = drawConnection(
            from = origin,
            to = physics,
            progress = lineProgress.value
          )

          drawNodePlatform(
            node = life,
            startAngle = lifeStartAngle,
            progress = nodeCircleProgress.value
          )

          drawNodePlatform(
            node = universe,
            startAngle = universeStartAngle,
            progress = nodeCircleProgress.value
          )

          drawNodePlatform(
            node = math,
            startAngle = mathStartAngle,
            progress = nodeCircleProgress.value
          )

          drawNodePlatform(
            node = physics,
            startAngle = physicsStartAngle,
            progress = nodeCircleProgress.value
          )

          drawOriginCircle(
            node = origin,
            progress = originCircleProgress.value
          )

          drawNodeText(
            node = life,
            textMeasurer = textMeasurer,
            progress = nodeTextProgress.value
          )

          drawNodeText(
            node = universe,
            textMeasurer = textMeasurer,
            progress = nodeTextProgress.value
          )

          drawNodeText(
            node = math,
            textMeasurer = textMeasurer,
            progress = nodeTextProgress.value
          )

          drawNodeText(
            node = physics,
            textMeasurer = textMeasurer,
            progress = nodeTextProgress.value
          )

          drawOriginText(
            node = origin,
            textMeasurer = textMeasurer,
            progress = originTextProgress.value
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