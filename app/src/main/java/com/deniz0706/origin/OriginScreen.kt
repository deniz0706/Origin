package com.deniz0706.origin

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import kotlinx.coroutines.delay
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/*
 * ORIGIN ana ekranı
 * =================
 *
 * Neredeyse tam tepeden görülen, beyaz bir yüzey üzerinde duran beş tek parça,
 * çok alçak taş platform. Kalınlık perspektifle değil; ince yan yüzey, bevel ve
 * temas gölgesiyle verilir. Yazılar taşın yüzeyine oyulmuştur.
 *
 * Animasyon tek bir saatle (ms) yürür; bağlantı zamanları elle verilmez:
 *   ORIGIN yükselir -> rim 12 yönünden saat yönünde çizilir -> her bağlantı
 *   noktasının rim üzerindeki açısal konumu geometriden hesaplanır (rimTrigger)
 *   -> rim oraya ulaşınca çizgi o noktadan büyür -> hedefe varınca hedefin sınırı
 *   temas noktasından iki yöne tamamlanır -> platform yükselir -> yazı belirir.
 */

// ---------------------------------------------------------------------------
// Palet
// ---------------------------------------------------------------------------

private val Paper = Color(0xFFF5F4F0)

private val TopLight = Color(0xFFFEFEFC)
private val TopShade = Color(0xFFF4F3EF)
private val EdgeShade = Color(0xFFE4E3DD)

private val SideLit = Color(0xFFEDECE7)
private val SideMid = Color(0xFFE2E1DB)
private val SideShade = Color(0xFFD6D5CE)
private val SideOcclusion = Color(0x14403F38)

private val ShadowTone = Color(0xFF55544D)

private val Ink = Color(0xFFB0AFA7)
private val GrooveDark = Color(0xFFC2C1B9)
private val GrooveLight = Color(0xFFFFFFFF)

private val Cobalt = Color(0xFF1E3A8A)
private val EngraveWall = Color(0xFF0E2160)
private val EngraveLit = Color(0xFFC9D2EA)
private val EngraveLip = Color(0xFFFFFFFF)
private val EngraveRecess = Color(0xFFD9D8D2)

// ---------------------------------------------------------------------------
// Ölçüler
// ---------------------------------------------------------------------------

/** Yukarıdan bakış: dairenin dikey ölçeği. 1f = tam üstten, küçüldükçe daha oval. */
private const val ViewSquash = 0.98f

/** Yazının üst yüzey çapına oranı (MATEMATİK en geniş yazı, ortak font ondan türer). */
private const val OuterLabelFill = 0.74f

/** Platform yüksekliği / yarıçap: yalnızca birkaç px. */
private const val StoneThickness = 0.052f

/** Pah genişliği / yarıçap. */
private const val StoneBevel = 0.04f

private const val OriginScale = 1.18f

// ---------------------------------------------------------------------------
// Zaman çizelgesi (ms)
// ---------------------------------------------------------------------------

private object Timing {
    const val ClockMs = 6000f

    const val OriginRiseMs = 800f
    const val OriginTextStart = 800f

    /** Rim çizimi: 12 yönünden başlar, saat yönünde 360° döner. */
    const val RimStart = 1100f
    const val RimMs = 2400f

    /** Bağlantı çizgisinin büyümesi. Başlangıcı rim'e bağlıdır, buradan verilmez. */
    const val LineMs = 700f

    /** Hedef dış sınırının temas noktasından iki yöne yayılması. */
    const val TraceMs = 450f

    /** Yükselme, dış sınır yayılırken (bu oranda) başlar. */
    const val RiseOverlap = 0.8f
    const val RiseMs = 700f

    const val TextMs = 450f

    /** Platform oluştuktan sonra rim çizgisinin kaybolma süresi. */
    const val ContourFadeMs = 600f
}

private fun phase(t: Float, start: Float, duration: Float): Float =
    ((t - start) / duration).coerceIn(0f, 1f)

private fun ease(x: Float): Float = FastOutSlowInEasing.transform(x)

// ---------------------------------------------------------------------------
// Geometri: silüet
// ---------------------------------------------------------------------------

private fun exitEllipse(from: Offset, dir: Offset, center: Offset, rx: Float, ry: Float): Float {
    val px = (from.x - center.x) / rx
    val py = (from.y - center.y) / ry
    val dx = dir.x / rx
    val dy = dir.y / ry
    val a = dx * dx + dy * dy
    val b = 2f * (px * dx + py * dy)
    val c = px * px + py * py - 1f
    val disc = b * b - 4f * a * c
    if (disc < 0f) return 0f
    return max(0f, (-b + sqrt(disc)) / (2f * a))
}

private fun exitRect(
    from: Offset,
    dir: Offset,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
): Float {
    var t = Float.MAX_VALUE
    if (dir.x > 1e-6f) t = min(t, (right - from.x) / dir.x)
    else if (dir.x < -1e-6f) t = min(t, (left - from.x) / dir.x)
    if (dir.y > 1e-6f) t = min(t, (bottom - from.y) / dir.y)
    else if (dir.y < -1e-6f) t = min(t, (top - from.y) / dir.y)
    return max(0f, t)
}

/**
 * Bir kaidenin ekrandaki dış çizgisi.
 *
 * Silüet = üst elips ∪ alt elips ∪ aradaki dikdörtgen. Üçü de dışbükeydir ve
 * ortak bir iç noktaya sahiptir; bu yüzden bir ışının silüetten çıkış mesafesi
 * üç parçanın çıkış mesafelerinin en büyüğüdür.
 */
private class Silhouette(
    val base: Offset,
    val rx: Float,
    val ry: Float,
    val height: Float,
) {
    private val top = Offset(base.x, base.y - height)
    private val mid = Offset(base.x, base.y - height / 2f)

    private fun exitDistance(from: Offset, dir: Offset): Float = max(
        exitEllipse(from, dir, base, rx, ry),
        max(
            exitEllipse(from, dir, top, rx, ry),
            exitRect(from, dir, base.x - rx, top.y, base.x + rx, base.y),
        ),
    )

    /** Silüetin, orta noktadan [angleDeg] yönündeki noktası. 0° = sağ, 90° = aşağı. */
    fun pointAt(angleDeg: Float): Offset {
        val rad = Math.toRadians(angleDeg.toDouble())
        val dir = Offset(cos(rad).toFloat(), sin(rad).toFloat())
        return mid + dir * exitDistance(mid, dir)
    }

    /** Zemindeki bir hattın (taban merkezinden çıkan) silüeti terk ettiği nokta. */
    fun groundExit(dir: Offset): Offset = base + dir * exitDistance(base, dir)

    /** [pointAt] fonksiyonunun tersi: noktanın orta noktadan görünen açısı. */
    fun angleOf(p: Offset): Float = Math.toDegrees(
        atan2((p.y - mid.y).toDouble(), (p.x - mid.x).toDouble()),
    ).toFloat()

    /** Silüet çizgisinin [startDeg] noktasından geriye ve ileriye doğru bir parçası. */
    fun outline(startDeg: Float, backDeg: Float, forwardDeg: Float): Path {
        val from = startDeg - backDeg
        val span = backDeg + forwardDeg
        val steps = max(2, ceil(span / 2f).toInt())
        return Path().apply {
            for (i in 0..steps) {
                val p = pointAt(from + span * i / steps)
                if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
            }
            if (span >= 359.5f) close()
        }
    }
}

// ---------------------------------------------------------------------------
// Sahne modeli
// ---------------------------------------------------------------------------

/**
 * [OriginNode.center]  = kaidenin zeminle temas eden taban merkezi
 * [OriginNode.radius]  = taban yarıçapı (yatay)
 */
private class Stone(val node: OriginNode, val label: TextLayoutResult) {
    val rx = node.radius
    val ry = node.radius * ViewSquash

    val thickness = node.radius * StoneThickness

    /** Yazının oyulduğu, tamamen yükselmiş üst yüzeyin merkezi. */
    val labelCenter = Offset(node.center.x, node.center.y - thickness)

    fun silhouette(rise: Float) = Silhouette(node.center, rx, ry, thickness * rise)
}

/** ORIGIN'den bir dış platforma giden bağlantı ve ondan türeyen tüm zamanlar. */
private class Link(
    val stone: Stone,
    val lineStart: Offset,
    val lineEnd: Offset,
    /** Hedef dış sınırının temas noktasındaki açısı. */
    val contactAngle: Float,
    /** ORIGIN rim'inde bu bağlantının bulunduğu konum (0..1). */
    val rimTrigger: Float,
) {
    val lineStartMs = Timing.RimStart + rimTrigger * Timing.RimMs
    val arriveMs = lineStartMs + Timing.LineMs
    val riseStartMs = arriveMs + Timing.TraceMs * Timing.RiseOverlap
    val textStartMs = riseStartMs + Timing.RiseMs
}

private class Piece(val stone: Stone, val link: Link?) {
    fun rise(ms: Float): Float = ease(
        if (link == null) phase(ms, 0f, Timing.OriginRiseMs)
        else phase(ms, link.riseStartMs, Timing.RiseMs),
    )

    fun labelAlpha(ms: Float): Float =
        if (link == null) phase(ms, Timing.OriginTextStart, Timing.TextMs)
        else phase(ms, link.textStartMs, Timing.TextMs)
}

private class Scene(val pieces: List<Piece>) {
    val links: List<Link> = pieces.mapNotNull { it.link }

    /** Arkadan öne: üstteki platformlar önce çizilir. */
    val drawOrder: List<Piece> = pieces.sortedBy { it.stone.node.center.y }
}

private fun buildLink(origin: Stone, target: Stone): Link {
    val delta = target.node.center - origin.node.center
    val dir = delta / delta.getDistance()

    // Çizgi zeminde yatar. ORIGIN'in görünen kenarında başlar...
    val originSilhouette = origin.silhouette(1f)
    val exitPoint = originSilhouette.groundExit(dir)

    // ...ve hedefin tabanına (zemindeki dış sınırına) kadar gider.
    val targetFootprint = target.silhouette(0f)
    val entryPoint = targetFootprint.groundExit(-dir)

    // Rim 12 yönünden (-90°) başlayıp saat yönünde ilerler. Bu noktaya
    // ulaştığı an = rim'in kat ettiği oran.
    val angle = originSilhouette.angleOf(exitPoint)
    val trigger = (((angle + 90f) % 360f) + 360f) % 360f / 360f

    return Link(
        stone = target,
        lineStart = exitPoint - dir * 1.5f, // taşın altında kalan 1.5px: boşluk bırakmaz
        lineEnd = entryPoint,
        contactAngle = targetFootprint.angleOf(entryPoint),
        rimTrigger = trigger,
    )
}

private fun buildScene(
    width: Float,
    height: Float,
    density: Density,
    measurer: TextMeasurer,
): Scene {
    val cx = width / 2f
    val cy = height / 2f

    val dx = width * 0.285f
    val dy = min(height * 0.20f, dx * 1.5f)

    // Dört dış platform aynı boyutta. Yarıçap ekrana ve komşulara göre sınırlanır;
    // yazı boyutu ise bu yarıçaptan, en uzun kelimeye (MATEMATİK) göre türetilir.
    val outerRadius = minOf(width / 2f - dx - width * 0.05f, dx * 0.64f, dy * 0.46f)
    val originRadius = outerRadius * OriginScale

    fun style(px: Float) = TextStyle(
        color = Cobalt,
        fontSize = with(density) { px.toSp() },
        fontWeight = FontWeight.SemiBold,
        fontFamily = FontFamily.SansSerif,
        letterSpacing = 0.06f.em,
    )

    val refPx = 100f
    fun widthPerPx(text: String) = measurer.measure(text = text, style = style(refPx)).size.width / refPx

    val outerFont = outerRadius * 2f * OuterLabelFill / widthPerPx("MATEMATİK")
    val originFont = min(
        outerFont * 1.3f,
        originRadius * 2f * 0.6f / widthPerPx("ORIGIN"),
    )

    fun stone(name: String, center: Offset, radius: Float, fontPx: Float) = Stone(
        node = OriginNode(name = name, center = center, radius = radius),
        label = measurer.measure(text = name, style = style(fontPx)),
    )

    val origin = stone("ORIGIN", Offset(cx, cy), originRadius, originFont)

    val targets = listOf(
        stone("YAŞAM", Offset(cx + dx, cy - dy), outerRadius, outerFont),
        stone("FİZİK", Offset(cx + dx, cy + dy), outerRadius, outerFont),
        stone("MATEMATİK", Offset(cx - dx, cy + dy), outerRadius, outerFont),
        stone("EVREN", Offset(cx - dx, cy - dy), outerRadius, outerFont),
    )

    return Scene(
        pieces = listOf(Piece(origin, null)) +
            targets.map { Piece(it, buildLink(origin, it)) },
    )
}

// ---------------------------------------------------------------------------
// Çizim: zemin katmanı
// ---------------------------------------------------------------------------

/** Yumuşak kenarlı, dikey olarak ezilmiş disk. Blur kullanmadan temas gölgesi verir. */
private fun DrawScope.softDisc(
    center: Offset,
    radius: Float,
    color: Color,
    alpha: Float,
    solidUntil: Float,
) {
    if (alpha <= 0f) return
    val brush = Brush.radialGradient(
        0f to color.copy(alpha = alpha),
        solidUntil to color.copy(alpha = alpha),
        1f to color.copy(alpha = 0f),
        center = center,
        radius = radius,
    )
    withTransform({ scale(1f, ViewSquash, pivot = center) }) {
        drawCircle(brush = brush, radius = radius, center = center)
    }
}

/** Taşın hemen dibindeki gölge. Platform havada durmaz, zemine oturur. */
private fun DrawScope.drawContactShadow(stone: Stone, rise: Float) {
    if (rise <= 0f) return
    val c = stone.node.center
    val r = stone.rx

    // geniş, çok hafif, her yöne eşit yayılır
    softDisc(
        center = c + Offset(r * 0.004f, r * 0.012f),
        radius = r * 1.09f,
        color = ShadowTone,
        alpha = 0.06f * rise,
        solidUntil = 0.88f,
    )
    // dar: taşın dibindeki temas gölgesi
    softDisc(
        center = c + Offset(r * 0.004f, r * 0.010f),
        radius = r * 1.03f,
        color = ShadowTone,
        alpha = 0.14f * rise,
        solidUntil = 0.94f,
    )
}

/** Zemine oyulmuş ince bağlantı: koyu çizgi + altında çok hafif ışık. */
private fun DrawScope.drawLink(link: Link, ms: Float) {
    val progress = ease(phase(ms, link.lineStartMs, Timing.LineMs))
    if (progress <= 0f) return

    val end = lerp(link.lineStart, link.lineEnd, progress)
    val width = 1.4.dp.toPx()
    val lift = Offset(0f, 0.9.dp.toPx())

    drawLine(
        color = GrooveLight,
        start = link.lineStart + lift,
        end = end + lift,
        strokeWidth = width,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = GrooveDark,
        start = link.lineStart,
        end = end,
        strokeWidth = width,
        cap = StrokeCap.Round,
    )
}

// ---------------------------------------------------------------------------
// Çizim: taş
// ---------------------------------------------------------------------------

/**
 * Tek kademe: gerçek yan yüzey + pah (bevel) halkası + üst yüzey.
 *
 * Yan yüzey tek bir kapalı Path'tir:
 *   üst elipsin sağ noktası → aşağı → alt elipsin ön yarısı → yukarı →
 *   üst elipsin ön yarısı (geri) → kapat.
 */
private fun DrawScope.drawTier(
    base: Offset,
    rx: Float,
    ry: Float,
    height: Float,
    bevel: Float,
    alpha: Float,
) {
    val top = Offset(base.x, base.y - height)

    val side = Path().apply {
        moveTo(base.x + rx, top.y)
        lineTo(base.x + rx, base.y)
        arcTo(Rect(base.x - rx, base.y - ry, base.x + rx, base.y + ry), 0f, 180f, false)
        lineTo(base.x - rx, top.y)
        arcTo(Rect(top.x - rx, top.y - ry, top.x + rx, top.y + ry), 180f, -180f, false)
        close()
    }

    // Yan yüzey: soldan sağa ton farkı (ışık sol üstten).
    drawPath(
        path = side,
        brush = Brush.horizontalGradient(
            0f to SideLit,
            0.45f to SideMid,
            1f to SideShade,
            startX = base.x - rx,
            endX = base.x + rx,
        ),
        alpha = alpha,
    )
    // Tabana doğru hafif kararma (zemine yakın köşe gölgesi).
    drawPath(
        path = side,
        brush = Brush.verticalGradient(
            0f to Color.Transparent,
            0.55f to Color.Transparent,
            1f to SideOcclusion,
            startY = top.y,
            endY = base.y + ry,
        ),
        alpha = alpha,
    )

    // Üst yüzey: çok hafif doğrusal ton farkı (radyal değil, yastık gibi şişmez).
    drawOval(
        brush = Brush.linearGradient(
            colors = listOf(TopLight, TopShade),
            start = Offset(top.x - rx, top.y - ry),
            end = Offset(top.x + rx, top.y + ry),
        ),
        topLeft = Offset(top.x - rx, top.y - ry),
        size = Size(rx * 2f, ry * 2f),
        alpha = alpha,
    )

    // Bevel: sadece kenar boyunca dar bir band. Sol-üstte açık, sağ-altta biraz koyu.
    val band = bevel * 0.7f
    val inset = band / 2f
    drawOval(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFFFFFF), TopShade, EdgeShade),
            start = Offset(top.x - rx * 0.7f, top.y - ry * 0.7f),
            end = Offset(top.x + rx * 0.7f, top.y + ry * 0.7f),
        ),
        topLeft = Offset(top.x - rx + inset, top.y - ry + inset),
        size = Size((rx - inset) * 2f, (ry - inset) * 2f),
        alpha = alpha,
        style = Stroke(width = band),
    )
}

private fun DrawScope.drawStoneBody(stone: Stone, rise: Float) {
    if (rise <= 0f) return

    drawTier(
        base = stone.node.center,
        rx = stone.rx,
        ry = stone.ry,
        height = stone.thickness * rise,
        bevel = stone.rx * StoneBevel,
        alpha = min(1f, rise * 6f),
    )
}

// ---------------------------------------------------------------------------
// Çizim: dış çizgi (rim) ve yazı
// ---------------------------------------------------------------------------

private fun DrawScope.drawContour(piece: Piece, ms: Float) {
    val stone = piece.stone
    val link = piece.link

    val path: Path
    val visibility: Float
    if (link == null) {
        // ORIGIN: 12 yönünden başlar, saat yönünde tek yönde ilerler.
        val sweep = 360f * phase(ms, Timing.RimStart, Timing.RimMs)
        if (sweep <= 0f) return
        path = stone.silhouette(1f).outline(startDeg = -90f, backDeg = 0f, forwardDeg = sweep)
        // Rim tamamlanınca çizgi geri çekilir.
        visibility = 1f - ease(phase(ms, Timing.RimStart + Timing.RimMs, Timing.ContourFadeMs))
    } else {
        // Hedef: çizginin değdiği noktadan iki zıt yönde ilerler.
        val trace = ease(phase(ms, link.arriveMs, Timing.TraceMs))
        if (trace <= 0f) return
        val half = 180f * trace
        path = stone.silhouette(piece.rise(ms)).outline(
            startDeg = link.contactAngle,
            backDeg = half,
            forwardDeg = half,
        )
        // Taş yükselirken çizgi geri çekilir; final ekranda keskin çember kalmaz.
        visibility = 1f - ease(
            phase(ms, link.riseStartMs + Timing.RiseMs * 0.4f, Timing.ContourFadeMs),
        )
    }
    if (visibility <= 0.01f) return

    drawPath(
        path = path,
        color = Ink,
        alpha = visibility,
        style = Stroke(
            width = 0.9.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        ),
    )
}

/**
 * Taşa işlenmiş yazı (letterpress): dolu kobalt gövde + altında yarım pikselik
 * tek bir taş tonu highlight. Gövdenin içi boşaltılmaz, offset bir pikselden küçüktür.
 */
private fun DrawScope.drawLabel(piece: Piece, ms: Float) {
    val alpha = piece.labelAlpha(ms)
    if (alpha <= 0f) return

    val stone = piece.stone
    val size = stone.label.size
    val topLeft = Offset(
        x = stone.labelCenter.x - size.width / 2f,
        y = stone.labelCenter.y - size.height / 2f,
    )
    drawText(
        textLayoutResult = stone.label,
        color = EngraveLip.copy(alpha = 0.95f * alpha),
        topLeft = topLeft + Offset(0.5f, 1f),
    )
    drawText(
        textLayoutResult = stone.label,
        color = Cobalt.copy(alpha = alpha),
        topLeft = topLeft,
    )
}

private fun DrawScope.drawScene(scene: Scene, ms: Float) {
    // 1) Zemin: gölgeler, sonra zemine oyulmuş bağlantılar
    scene.drawOrder.forEach { drawContactShadow(it.stone, it.rise(ms)) }
    scene.links.forEach { drawLink(it, ms) }

    // 2) Taşlar (bağlantıların üstünde; çizgi taşın arkasından çıkar)
    scene.drawOrder.forEach { drawStoneBody(it.stone, it.rise(ms)) }

    // 3) Dış çizgiler
    scene.drawOrder.forEach { drawContour(it, ms) }

    // 4) Yazılar
    scene.drawOrder.forEach { drawLabel(it, ms) }
}

// ---------------------------------------------------------------------------
// Compose
// ---------------------------------------------------------------------------

@Composable
fun OriginApp() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Paper
        ) {
            OriginScreen()
        }
    }
}

/** Animasyonu başlatan uygulama girişi. */
@Composable
fun OriginPrototypeApp() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Paper,
        ) {
            OriginScreen()
        }
    }
}

@Composable
fun OriginScreen(modifier: Modifier = Modifier) {
    val clock = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(200)
        clock.animateTo(
            targetValue = Timing.ClockMs,
            animationSpec = tween(
                durationMillis = Timing.ClockMs.toInt(),
                easing = LinearEasing,
            ),
        )
    }

    // Saat sadece çizim aşamasında okunur: animasyon recomposition üretmez.
    OriginCanvas(timeMs = { clock.value }, modifier = modifier)
}

@Composable
fun OriginCanvas(
    timeMs: () -> Float,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()

    BoxWithConstraints(modifier = modifier.fillMaxSize().safeDrawingPadding()) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        val scene = remember(widthPx, heightPx, density.density, density.fontScale, measurer) {
            buildScene(widthPx, heightPx, density, measurer)
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawScene(scene, timeMs())
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F4F0, widthDp = 411, heightDp = 891)
@Composable
private fun OriginScreenPreview() {
    // Animasyonun bittiği son hal.
    OriginCanvas(timeMs = { Timing.ClockMs })
}