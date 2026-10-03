package com.deniz0706.origin

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.atan2
import kotlin.math.sqrt

private val Background = Color(0xFFF8F8F6)

private val StoneTop = Color(0xFFFCFCFA)
private val StoneInner = Color(0xFFF4F4F1)
private val StoneSide = Color(0xFFD8D8D3)
private val StoneSideDark = Color(0xFFC8C8C2)
private val StoneEdge = Color(0xFFE2E2DD)

private val GroundShadow = Color(0xFF8C8C86)

private val TextBlue = Color(0xFF1769AA)
private val ConnectionColor = Color(0xFFB9B9B3)

private fun segmentProgress(
  progress: Float,
  start: Float,
  end: Float
): Float {
  if (end <= start) return 0f

  return ((progress - start) / (end - start))
    .coerceIn(0f, 1f)
}

private fun DrawScope.connectionAngle(
  from: OriginNode,
  to: OriginNode
): Float {
  val dx = to.center.x - from.center.x
  val dy = to.center.y - from.center.y

  return Math.toDegrees(
    atan2(
      dy.toDouble(),
      dx.toDouble()
    )
  ).toFloat()
}

private fun DrawScope.drawConnection(
  from: OriginNode,
  to: OriginNode,
  progress: Float
): Float {
  val dx = to.center.x - from.center.x
  val dy = to.center.y - from.center.y

  val distance = sqrt(
    dx * dx + dy * dy
  )

  val fromRatio = from.radius / distance
  val toRatio = to.radius / distance

  val lineStart = Offset(
    x = from.center.x + dx * fromRatio,
    y = from.center.y + dy * fromRatio
  )

  val lineEnd = Offset(
    x = to.center.x - dx * toRatio,
    y = to.center.y - dy * toRatio
  )

  val animatedEnd = Offset(
    x = lineStart.x +
      (lineEnd.x - lineStart.x) * progress,
    y = lineStart.y +
      (lineEnd.y - lineStart.y) * progress
  )

  if (progress > 0f) {
    drawLine(
      color = ConnectionColor.copy(
        alpha = 0.85f
      ),
      start = lineStart,
      end = animatedEnd,
      strokeWidth = 3f
    )
  }

  return Math.toDegrees(
    atan2(
      (lineEnd.y - to.center.y).toDouble(),
      (lineEnd.x - to.center.x).toDouble()
    )
  ).toFloat()
}

private fun DrawScope.drawStonePlatform(
  node: OriginNode,
  startAngle: Float,
  progress: Float,
  widthScale: Float = 1f
) {
  if (progress <= 0f) return

  /*
   * Platform ekrana "pat" diye gelmiyor.
   * Zeminden birkaç santim yükseliyormuş gibi.
   */
  val easedProgress = FastOutSlowInEasing.transform(
    progress.coerceIn(0f, 1f)
  )

  val radiusX = node.radius * widthScale
  val radiusY = node.radius * 0.72f

  val currentRadiusX =
    radiusX * (0.88f + 0.12f * easedProgress)

  val currentRadiusY =
    radiusY * (0.88f + 0.12f * easedProgress)

  /*
   * Kaidenin yüksekliği bilerek çok az.
   * Uzun sütun değil.
   */
  val height = 14f * easedProgress

  val center = Offset(
    x = node.center.x,
    y = node.center.y - height * 0.35f
  )

  val platformSize = Size(
    width = currentRadiusX * 2f,
    height = currentRadiusY * 2f
  )

  val topLeft = Offset(
    x = center.x - currentRadiusX,
    y = center.y - currentRadiusY
  )

  /*
   * YER GÖLGESİ
   *
   * Beyaz zemin + beyaz taş ayrımının ana parçalarından biri.
   */
  drawOval(
    color = GroundShadow.copy(
      alpha = 0.13f * easedProgress
    ),
    topLeft = Offset(
      x = center.x - currentRadiusX * 0.91f,
      y = center.y +
        currentRadiusY +
        height +
        5f
    ),
    size = Size(
      width = currentRadiusX * 1.82f,
      height = 17f
    )
  )

  /*
   * ALT BASAMAK / TAŞ GÖVDESİ
   */
  drawOval(
    color = StoneSideDark.copy(
      alpha = easedProgress
    ),
    topLeft = Offset(
      x = topLeft.x + 3f,
      y = topLeft.y + height + 4f
    ),
    size = Size(
      width = platformSize.width - 6f,
      height = platformSize.height
    )
  )

  /*
   * İKİNCİ, DAHA AÇIK KATMAN.
   *
   * Bu iki katman sayesinde kaide birkaç basamak
   * yüksekliğinde okunuyor.
   */
  drawOval(
    color = StoneSide.copy(
      alpha = easedProgress
    ),
    topLeft = Offset(
      x = topLeft.x + 1.5f,
      y = topLeft.y + height
    ),
    size = Size(
      width = platformSize.width - 3f,
      height = platformSize.height
    )
  )

  /*
   * TAŞIN ÜST YÜZEYİ
   */
  drawOval(
    color = StoneTop.copy(
      alpha = easedProgress
    ),
    topLeft = topLeft,
    size = platformSize
  )

  /*
   * İç yüzeyde çok hafif ton farkı.
   * Glow değil. Taşın yüzeyini dümdüz beyaz lekeden ayırıyor.
   */
  drawOval(
    color = StoneInner.copy(
      alpha = 0.42f * easedProgress
    ),
    topLeft = Offset(
      x = topLeft.x + 10f,
      y = topLeft.y + 7f
    ),
    size = Size(
      width = platformSize.width - 20f,
      height = platformSize.height - 14f
    )
  )

  /*
   * SMOOTH TAŞ KENARI
   */
  drawOval(
    color = StoneEdge.copy(
      alpha = 0.95f * easedProgress
    ),
    topLeft = topLeft,
    size = platformSize,
    style = Stroke(width = 2.5f)
  )

  /*
   * Çizginin geldiği noktadan iki yana yayılan
   * oluşum çizgisi.
   */
  drawArc(
    color = Color.White.copy(
      alpha = progress
    ),
    startAngle = startAngle,
    sweepAngle = progress * 180f,
    useCenter = false,
    topLeft = topLeft,
    size = platformSize,
    style = Stroke(width = 3.5f)
  )

  drawArc(
    color = Color.White.copy(
      alpha = progress
    ),
    startAngle = startAngle,
    sweepAngle = progress * -180f,
    useCenter = false,
    topLeft = topLeft,
    size = platformSize,
    style = Stroke(width = 3.5f)
  )
}

private fun DrawScope.drawStoneText(
  node: OriginNode,
  textMeasurer: TextMeasurer,
  progress: Float,
  fontSize: Float = 19f
) {
  if (progress <= 0f) return

  /*
   * TEK YAZI.
   *
   * Glow yok.
   * İkinci kopya yok.
   * Glitch yok.
   * Taşa gömülme efekti yok.
   */
  val text = textMeasurer.measure(
    text = node.name,
    style = TextStyle(
      color = TextBlue.copy(
        alpha = progress
      ),
      fontSize = fontSize.sp,
      fontWeight = FontWeight.SemiBold,
      letterSpacing = 0.7.sp
    )
  )

  drawText(
    textLayoutResult = text,
    topLeft = Offset(
      x = node.center.x - text.size.width / 2,
      y = node.center.y -
        text.size.height / 2 -
        5f
    )
  )
}

private fun DrawScope.drawOriginFormation(
  node: OriginNode,
  progress: Float
) {
  if (progress <= 0f) return

  val radiusX = node.radius
  val radiusY = node.radius * 0.72f

  val height = 17f * progress

  val center = Offset(
    x = node.center.x,
    y = node.center.y - height * 0.35f
  )

  val platformSize = Size(
    width = radiusX * 2f,
    height = radiusY * 2f
  )

  val topLeft = Offset(
    x = center.x - radiusX,
    y = center.y - radiusY
  )

  drawOval(
    color = GroundShadow.copy(
      alpha = 0.15f * progress
    ),
    topLeft = Offset(
      x = center.x - radiusX * 0.91f,
      y = center.y + radiusY + height + 6f
    ),
    size = Size(
      width = radiusX * 1.82f,
      height = 19f
    )
  )

  drawOval(
    color = StoneSideDark.copy(
      alpha = progress
    ),
    topLeft = Offset(
      x = topLeft.x + 4f,
      y = topLeft.y + height + 4f
    ),
    size = Size(
      width = platformSize.width - 8f,
      height = platformSize.height
    )
  )

  drawOval(
    color = StoneSide.copy(
      alpha = progress
    ),
    topLeft = Offset(
      x = topLeft.x + 2f,
      y = topLeft.y + height
    ),
    size = Size(
      width = platformSize.width - 4f,
      height = platformSize.height
    )
  )

  drawOval(
    color = StoneTop.copy(
      alpha = progress
    ),
    topLeft = topLeft,
    size = platformSize
  )

  drawOval(
    color = StoneInner.copy(
      alpha = 0.42f * progress
    ),
    topLeft = Offset(
      x = topLeft.x + 13f,
      y = topLeft.y + 9f
    ),
    size = Size(
      width = platformSize.width - 26f,
      height = platformSize.height - 18f
    )
  )

  /*
   * ORIGIN'İN ÇEVRESİNİ ÇİZEN TEK ÇİZGİ.
   *
   * -90 dereceden başlıyor ve saat yönünde ilerliyor.
   * Bağlantıların zamanlamasını da bu çizimin konumu belirliyor.
   */
  drawArc(
    color = StoneEdge,
    startAngle = -90f,
    sweepAngle = progress * 360f,
    useCenter = false,
    topLeft = topLeft,
    size = platformSize,
    style = Stroke(width = 3.5f)
  )
}

@Composable
fun OriginApp() {

  val introStarted = remember {
    mutableStateOf(false)
  }

  val textMeasurer = rememberTextMeasurer()

  /*
   * Tek intro saati.
   *
   * Toplam yaklaşık 5.5 saniye.
   */
  val introProgress = animateFloatAsState(
    targetValue = if (introStarted.value) {
      1f
    } else {
      0f
    },
    animationSpec = tween(
      durationMillis = 5500,
      easing = LinearEasing
    )
  )

  LaunchedEffect(Unit) {
    delay(150)
    introStarted.value = true
  }

  val master = introProgress.value

  /*
   * İlk iş ORIGIN.
   */
  val originFormation = segmentProgress(
    master,
    0.00f,
    0.34f
  )

  val originText = segmentProgress(
    master,
    0.08f,
    0.27f
  )

  /*
   * ORIGIN çemberi yukarıdan başlayıp saat yönünde dönüyor.
   *
   * Sağ üst  -> YAŞAM
   * Sağ alt  -> FİZİK
   * Sol alt  -> MATEMATİK
   * Sol üst  -> EVREN
   *
   * O yüzden bağlantılar da bu sırayla doğuyor.
   */
  val lifeLine = segmentProgress(
    originFormation,
    0.10f,
    0.72f
  )

  val physicsLine = segmentProgress(
    originFormation,
    0.34f,
    0.96f
  )

  val mathLine = segmentProgress(
    originFormation,
    0.58f,
    1.00f
  )

  val universeLine = segmentProgress(
    originFormation,
    0.82f,
    1.00f
  )

  /*
   * Dış platformlar çizgi hedefe yaklaştıkça oluşuyor.
   */
  val lifePlatform = segmentProgress(
    master,
    0.25f,
    0.48f
  )

  val physicsPlatform = segmentProgress(
    master,
    0.39f,
    0.62f
  )

  val mathPlatform = segmentProgress(
    master,
    0.53f,
    0.76f
  )

  val universePlatform = segmentProgress(
    master,
    0.67f,
    0.90f
  )

  val lifeText = segmentProgress(
    master,
    0.41f,
    0.53f
  )

  val physicsText = segmentProgress(
    master,
    0.55f,
    0.67f
  )

  val mathText = segmentProgress(
    master,
    0.69f,
    0.81f
  )

  val universeText = segmentProgress(
    master,
    0.83f,
    0.95f
  )

  MaterialTheme {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = Background
    ) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
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

          /*
           * MATEMATİK için merkez aynı,
           * fakat taş biraz daha geniş çizilecek.
           */
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
           * BAĞLANTILAR
           *
           * Her biri ORIGIN çevresindeki çizim kendi
           * bağlantı noktasına ulaştığında doğuyor.
           */
          val lifeStartAngle = drawConnection(
            from = origin,
            to = life,
            progress = lifeLine
          )

          val physicsStartAngle = drawConnection(
            from = origin,
            to = physics,
            progress = physicsLine
          )

          val mathStartAngle = drawConnection(
            from = origin,
            to = math,
            progress = mathLine
          )

          val universeStartAngle = drawConnection(
            from = origin,
            to = universe,
            progress = universeLine
          )

          /*
           * DIŞ TAŞ KAİDELER
           */
          drawStonePlatform(
            node = life,
            startAngle = lifeStartAngle,
            progress = lifePlatform
          )

          drawStonePlatform(
            node = physics,
            startAngle = physicsStartAngle,
            progress = physicsPlatform
          )

          drawStonePlatform(
            node = math,
            startAngle = mathStartAngle,
            progress = mathPlatform,
            widthScale = 1.22f
          )

          drawStonePlatform(
            node = universe,
            startAngle = universeStartAngle,
            progress = universePlatform
          )

          /*
           * MERKEZ TAŞ
           */
          drawOriginFormation(
            node = origin,
            progress = originFormation
          )

          /*
           * DIŞ YAZILAR
           */
          drawStoneText(
            node = life,
            textMeasurer = textMeasurer,
            progress = lifeText
          )

          drawStoneText(
            node = physics,
            textMeasurer = textMeasurer,
            progress = physicsText
          )

          drawStoneText(
            node = math,
            textMeasurer = textMeasurer,
            progress = mathText,
            fontSize = 18f
          )

          drawStoneText(
            node = universe,
            textMeasurer = textMeasurer,
            progress = universeText
          )

          /*
           * ORIGIN YAZISI
           */
          drawStoneText(
            node = origin,
            textMeasurer = textMeasurer,
            progress = originText,
            fontSize = 27f
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