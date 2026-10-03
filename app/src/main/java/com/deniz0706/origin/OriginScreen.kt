package com.deniz0706.origin

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min

private val OriginBlue = Color(0xFF1677FF)
private val OriginBlueBright = Color(0xFF67B7FF)
private val OriginBlueDark = Color(0xFF073B73)

private val PlatformSurface = Color(0xFFF3F6F8)
private val PlatformInner = Color(0xFFDCE4EA)
private val PlatformSide = Color(0xFF6D7780)
private val PlatformShadow = Color(0xFF05080D)

private fun segmentProgress(
  progress: Float,
  start: Float,
  end: Float
): Float {
  if (end <= start) return 0f

  return ((progress - start) / (end - start))
    .coerceIn(0f, 1f)
}

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

  if (progress > 0f) {

    drawLine(
      color = OriginBlue.copy(
        alpha = 0.12f * progress
      ),
      start = lineStart,
      end = animatedEnd,
      strokeWidth = 11f
    )

    drawLine(
      color = Color.White.copy(
        alpha = 0.82f * progress
      ),
      start = lineStart,
      end = animatedEnd,
      strokeWidth = 3f
    )

    drawCircle(
      color = OriginBlueBright.copy(
        alpha = 0.50f * progress
      ),
      radius = 5f,
      center = animatedEnd
    )

    drawCircle(
      color = Color.White.copy(
        alpha = 0.90f * progress
      ),
      radius = 2f,
      center = animatedEnd
    )
  }

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
  circleProgress: Float,
  surfaceProgress: Float
) {
  if (circleProgress <= 0f && surfaceProgress <= 0f) return

  val diameter = node.radius * 2f

  val platformSize = Size(
    width = diameter,
    height = diameter
  )

  val topLeft = Offset(
    x = node.center.x - node.radius,
    y = node.center.y - node.radius
  )

  val depth = 9f

  /*
   * UZAK GÖLGE
   */
  drawOval(
    color = PlatformShadow.copy(
      alpha = 0.48f * surfaceProgress
    ),
    topLeft = Offset(
      x = topLeft.x - 5f,
      y = topLeft.y + depth + 7f
    ),
    size = Size(
      width = platformSize.width + 10f,
      height = platformSize.height + 10f
    )
  )

  /*
   * PLATFORMUN ALT GÖVDESİ
   */
  drawOval(
    color = PlatformSide.copy(
      alpha = surfaceProgress
    ),
    topLeft = Offset(
      x = topLeft.x,
      y = topLeft.y + depth
    ),
    size = platformSize
  )

  /*
   * ÜST YÜZEY
   */
  drawOval(
    color = PlatformSurface.copy(
      alpha = surfaceProgress
    ),
    topLeft = topLeft,
    size = platformSize
  )

  /*
   * İÇ YÜZEY.
   * Tamamen düz beyaz disk görünümünü kırıyor.
   */
  drawCircle(
    color = PlatformInner.copy(
      alpha = 0.35f * surfaceProgress
    ),
    radius = node.radius - 10f,
    center = Offset(
      x = node.center.x,
      y = node.center.y + 2f
    )
  )

  /*
   * ÇOK HAFİF MAVİ İÇ HALKA
   */
  drawCircle(
    color = OriginBlue.copy(
      alpha = 0.13f * surfaceProgress
    ),
    radius = node.radius - 10f,
    center = node.center,
    style = Stroke(width = 3f)
  )

  /*
   * DIŞ ÇEMBER:
   * bağlantının platforma değdiği noktadan iki yöne açılıyor.
   */
  drawArc(
    color = Color.White.copy(
      alpha = circleProgress
    ),
    startAngle = startAngle,
    sweepAngle = circleProgress * 180f,
    useCenter = false,
    topLeft = topLeft,
    size = platformSize,
    style = Stroke(width = 5f)
  )

  drawArc(
    color = Color.White.copy(
      alpha = circleProgress
    ),
    startAngle = startAngle,
    sweepAngle = circleProgress * -180f,
    useCenter = false,
    topLeft = topLeft,
    size = platformSize,
    style = Stroke(width = 5f)
  )

  /*
   * ÜST KENARDA ÇOK HAFİF IŞIK
   */
  drawArc(
    color = Color.White.copy(
      alpha = 0.48f * surfaceProgress
    ),
    startAngle = 200f,
    sweepAngle = 140f,
    useCenter = false,
    topLeft = Offset(
      x = topLeft.x + 5f,
      y = topLeft.y + 5f
    ),
    size = Size(
      width = platformSize.width - 10f,
      height = platformSize.height - 10f
    ),
    style = Stroke(width = 2f)
  )
}

private fun DrawScope.drawNodeText(
  node: OriginNode,
  textMeasurer: TextMeasurer,
  progress: Float
) {
  if (progress <= 0f) return

  /*
   * YAZININ ALTINDAKİ OYUK/GÖLGE.
   */
  val engravedText = textMeasurer.measure(
    text = node.name,
    style = TextStyle(
      color = OriginBlueDark.copy(
        alpha = 0.70f * progress
      ),
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.2.sp
    )
  )

  drawText(
    textLayoutResult = engravedText,
    topLeft = Offset(
      x = node.center.x - engravedText.size.width / 2,
      y = node.center.y - engravedText.size.height / 2 + 2.5f
    )
  )

  /*
   * GENİŞ, ÇOK HAFİF MAVİ IŞIK.
   */
  val glowText = textMeasurer.measure(
    text = node.name,
    style = TextStyle(
      color = OriginBlueBright.copy(
        alpha = 0.18f * progress
      ),
      fontSize = 23.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.5.sp
    )
  )

  drawText(
    textLayoutResult = glowText,
    topLeft = Offset(
      x = node.center.x - glowText.size.width / 2,
      y = node.center.y - glowText.size.height / 2
    )
  )

  /*
   * ASIL YAZI.
   */
  val nodeText = textMeasurer.measure(
    text = node.name,
    style = TextStyle(
      color = OriginBlue.copy(
        alpha = progress
      ),
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.2.sp
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

private fun DrawScope.drawOriginHalo(
  node: OriginNode,
  progress: Float,
  pulse: Float
) {
  if (progress <= 0f) return

  val haloAlpha = 0.05f + (pulse * 0.05f)

  drawCircle(
    color = OriginBlue.copy(
      alpha = haloAlpha * progress
    ),
    radius = node.radius + 34f + (pulse * 4f),
    center = node.center,
    style = Stroke(width = 16f)
  )

  drawCircle(
    color = OriginBlueBright.copy(
      alpha = (0.12f + pulse * 0.06f) * progress
    ),
    radius = node.radius + 17f + (pulse * 2f),
    center = node.center,
    style = Stroke(width = 2f)
  )
}

private fun DrawScope.drawOriginCircle(
  node: OriginNode,
  progress: Float
) {
  drawArc(
    startAngle = -90f,
    sweepAngle = progress * 360f,
    style = Stroke(width = 4f),
    size = Size(
      width = node.radius * 2,
      height = node.radius * 2
    ),
    color = Color.White.copy(
      alpha = 0.95f
    ),
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
  val glowText = textMeasurer.measure(
    text = node.name,
    style = TextStyle(
      color = OriginBlueBright.copy(
        alpha = 0.16f * progress
      ),
      fontSize = 36.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 2.5.sp
    )
  )

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

  val textOffsetY = 38f * (1f - progress)

  drawText(
    textLayoutResult = glowText,
    topLeft = Offset(
      x = node.center.x - glowText.size.width / 2,
      y = node.center.y - glowText.size.height / 2 + textOffsetY
    )
  )

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

  val introStarted = remember {
    mutableStateOf(false)
  }

  val textMeasurer = rememberTextMeasurer()

  /*
   * Tek bir ana intro değeri:
   *
   * 0 ------------------------------------------------------ 1
   *
   * Bunun farklı parçalarını aşağıda farklı animasyonlara
   * dönüştürüyoruz.
   */
  val introProgress = animateFloatAsState(
    targetValue = if (introStarted.value) {
      1f
    } else {
      0f
    },
    animationSpec = tween(
      durationMillis = 5200,
      easing = LinearEasing
    )
  )

  /*
   * Açılıştan sonra ORIGIN çok hafif nefes alıyor.
   */
  val infiniteTransition = rememberInfiniteTransition()

  val originPulse = infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(
        durationMillis = 2600,
        easing = EaseInOutCubic
      ),
      repeatMode = RepeatMode.Reverse
    )
  )

  LaunchedEffect(Unit) {
    delay(180)
    introStarted.value = true
  }

  /*
   * INTRO ZAMAN ÇİZELGESİ
   *
   * 0.00 - 0.25  ORIGIN yazısı
   * 0.16 - 0.39  ORIGIN çemberi
   * 0.34 - 0.70  bağlantılar
   * 0.64 - 0.84  platform çemberleri
   * 0.70 - 0.90  platform yüzeyleri
   * 0.82 - 1.00  node yazıları
   */

  val masterProgress = introProgress.value

  val originTextProgress = segmentProgress(
    masterProgress,
    0.00f,
    0.25f
  )

  val originCircleProgress = segmentProgress(
    masterProgress,
    0.16f,
    0.39f
  )

  val originHaloProgress = segmentProgress(
    masterProgress,
    0.28f,
    0.46f
  )

  /*
   * Bağlantılara küçük farklar verdim.
   * Hepsi aynı milisaniyede fırlamıyor.
   */
  val lifeLineProgress = segmentProgress(
    masterProgress,
    0.34f,
    0.66f
  )

  val universeLineProgress = segmentProgress(
    masterProgress,
    0.37f,
    0.69f
  )

  val mathLineProgress = segmentProgress(
    masterProgress,
    0.40f,
    0.72f
  )

  val physicsLineProgress = segmentProgress(
    masterProgress,
    0.43f,
    0.75f
  )

  val lifeCircleProgress = segmentProgress(
    masterProgress,
    0.62f,
    0.79f
  )

  val universeCircleProgress = segmentProgress(
    masterProgress,
    0.65f,
    0.82f
  )

  val mathCircleProgress = segmentProgress(
    masterProgress,
    0.68f,
    0.85f
  )

  val physicsCircleProgress = segmentProgress(
    masterProgress,
    0.71f,
    0.88f
  )

  val lifeSurfaceProgress = segmentProgress(
    masterProgress,
    0.68f,
    0.82f
  )

  val universeSurfaceProgress = segmentProgress(
    masterProgress,
    0.71f,
    0.85f
  )

  val mathSurfaceProgress = segmentProgress(
    masterProgress,
    0.74f,
    0.88f
  )

  val physicsSurfaceProgress = segmentProgress(
    masterProgress,
    0.77f,
    0.91f
  )

  val lifeTextProgress = segmentProgress(
    masterProgress,
    0.78f,
    0.91f
  )

  val universeTextProgress = segmentProgress(
    masterProgress,
    0.81f,
    0.94f
  )

  val mathTextProgress = segmentProgress(
    masterProgress,
    0.84f,
    0.97f
  )

  val physicsTextProgress = segmentProgress(
    masterProgress,
    0.87f,
    1.00f
  )

  MaterialTheme {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = Color(0xFF020305)
    ) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
      ) {
        Canvas(
          modifier = Modifier.fillMaxSize()
        ) {

          /*
           * ÇOK HAFİF ARKA PLAN DERİNLİĞİ
           */
          drawCircle(
            color = OriginBlue.copy(
              alpha = 0.018f * originHaloProgress
            ),
            radius = min(size.width, size.height) * 0.48f,
            center = Offset(
              x = size.width / 2,
              y = size.height / 2
            )
          )

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

          /*
           * ORIGIN HALO
           */
          drawOriginHalo(
            node = origin,
            progress = originHaloProgress,
            pulse = originPulse.value
          )

          /*
           * BAĞLANTILAR
           */
          val lifeStartAngle = drawConnection(
            from = origin,
            to = life,
            progress = lifeLineProgress
          )

          val universeStartAngle = drawConnection(
            from = origin,
            to = universe,
            progress = universeLineProgress
          )

          val mathStartAngle = drawConnection(
            from = origin,
            to = math,
            progress = mathLineProgress
          )

          val physicsStartAngle = drawConnection(
            from = origin,
            to = physics,
            progress = physicsLineProgress
          )

          /*
           * PLATFORMLAR
           */
          drawNodePlatform(
            node = life,
            startAngle = lifeStartAngle,
            circleProgress = lifeCircleProgress,
            surfaceProgress = lifeSurfaceProgress
          )

          drawNodePlatform(
            node = universe,
            startAngle = universeStartAngle,
            circleProgress = universeCircleProgress,
            surfaceProgress = universeSurfaceProgress
          )

          drawNodePlatform(
            node = math,
            startAngle = mathStartAngle,
            circleProgress = mathCircleProgress,
            surfaceProgress = mathSurfaceProgress
          )

          drawNodePlatform(
            node = physics,
            startAngle = physicsStartAngle,
            circleProgress = physicsCircleProgress,
            surfaceProgress = physicsSurfaceProgress
          )

          /*
           * ORIGIN
           */
          drawOriginCircle(
            node = origin,
            progress = originCircleProgress
          )

          /*
           * NODE YAZILARI
           */
          drawNodeText(
            node = life,
            textMeasurer = textMeasurer,
            progress = lifeTextProgress
          )

          drawNodeText(
            node = universe,
            textMeasurer = textMeasurer,
            progress = universeTextProgress
          )

          drawNodeText(
            node = math,
            textMeasurer = textMeasurer,
            progress = mathTextProgress
          )

          drawNodeText(
            node = physics,
            textMeasurer = textMeasurer,
            progress = physicsTextProgress
          )

          drawOriginText(
            node = origin,
            textMeasurer = textMeasurer,
            progress = originTextProgress
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