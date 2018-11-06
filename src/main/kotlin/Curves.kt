import com.curiouscreature.kotlin.math.Float2
import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.normalize
import kotlin.math.roundToInt

sealed class Curve

open class ExplicitCurve(private val f : (Float) -> Float) : Curve() {
    operator fun invoke(x : Float) = f(x)
}

open class ImplicitCurve(private val test : (Float, Float) -> Boolean) : Curve()

//open class ParametricCurve(private val x : (Float) -> Float, private val y : (Float) -> Float) : Curve() {
//    operator fun invoke(t : Float) = Float2(x(t), y(t))
//}

// examples
open class Polynomial(vararg val coeffs : Float) : ExplicitCurve({ x ->
    coeffs.reversed().asSequence().reduceIndexed { i, acc, c ->
        acc + c * Math.pow(x.toDouble(), i.toDouble()).toFloat()
    }
}) {
    val order : Int
        get() = coeffs.size - 1
}


class Quadratic(val a : Float, val b : Float, val c : Float) : Polynomial(a, b, c)
class Cubic(val a : Float, val b : Float, val c : Float, val d : Float) : Polynomial(a, b, c, d)

class Circle(val radius : Double, val xCenter : Double, val yCenter : Double) : ImplicitCurve({ x, y ->
    eqFloating(Math.pow(x - xCenter, 2.0) + Math.pow(y - yCenter, 2.0), Math.pow(radius, 2.0), 1e-6)
})

fun eqFloating(a : Double, b : Double, epsilon : Double) : Boolean = Math.abs(a - b) < epsilon

// CONSTANTS
val color = 0xFF0000 // Red
// END CONSTANTS

fun Image.drawCurve(origin : Float2, scale : Float2, curve : Curve, blur : Boolean) : Image {
    var image = when (curve) {
        is ExplicitCurve -> drawExplicitCurve(origin, scale, curve, this)
        is ImplicitCurve -> TODO("Implement drawing implicit curves via testing")
    }

    if (blur) image = image.apply(gaussianFilter, 3)

    drawCoordinateAxes(image.width, image.height, origin, scale, image)
    return image
}

fun drawExplicitCurve(
    origin: Float2,
    scale: Float2,
    curve: ExplicitCurve,
    image : Image
) : Image {
    // Find points along the curve inside window, draw those points to the image

    val halfSize = scale * 0.5f

    val (start, end) = origin - halfSize to origin + halfSize
//    val range = floatingRange(start.x, end.x, dt)

//    val points = floatingRange(start.x, end.x, dt).map { Float2(it, curve(it)) }.zipWithNext().forEach { (p1, p2) ->
//        drawSection(0.1f, size, size, origin, scale, p1, p2, curve, image)
//    }

    drawSection(3, image.height, image.height, origin, scale, origin - (scale * 0.5f), origin + (scale * 0.5f), curve, image)

//    for (x in range) {
//        val y = curve(x) // Find f(x)
//        val pixelPosition = mapToPixel(size, size, origin, scale, Float2(x, y))
//        val i = pixelPosition.first + pixelPosition.second * size
//        if (i < size * size && i >= 0) image[pixelPosition.first, pixelPosition.second] = color
//    }

    return image
}

fun drawSection(
    lineWidth : Int,
    width : Int,
    height : Int,
    origin : Float2,
    scale : Float2,
    start : Float2,
    end : Float2,
    curve : ExplicitCurve,
    image : Image
) {
    val dt = Math.min(scale.x, scale.y) / Math.max(width, height).toFloat()

    for (x in floatingRange(start.x, end.x, dt)) {
        val sol1 = curve(x)
        val sol2 = curve(x + dt)
        drawLineNaive(width, height, origin, scale, Float2(x, sol1), Float2(x + dt, sol2), image)
//        drawRegion(width, height, origin, scale, Float2(x, sol1), Float2(x + dt, sol2), curve, image)
    }
}

fun drawLineNaive(width : Int, height : Int, origin : Float2, scale : Float2, start : Float2, end : Float2, image : Image) {
    drawLineDDA(mapToPixel(width, height, origin, scale, start), mapToPixel(width, height, origin, scale, end), image)
}

fun drawCoordinateAxes(width : Int, height : Int, origin : Float2, scale : Float2, image : Image) {
    val screenOrigin = mapToPixel(width, height, origin, scale, origin)

    for (x in 0 until width) {
        image[x, screenOrigin.second] = 0
    }

    for (y in 0 until height) {
        image[screenOrigin.first, y] = 0
    }

}

fun drawRegion(width : Int, height : Int, origin : Float2, scale : Float2, p1 : Float2, p2 : Float2, curve: ExplicitCurve, image : Image) {
    val startPixel = mapToPixel(width, height, origin, scale, p1)
    val endPixel = mapToPixel(width, height, origin, scale, p2)

    for (y in startPixel.second..endPixel.second) {
        for (x in startPixel.first..endPixel.first) {
            val worldSpacePoint = mapToPoint(image.width, image.height, origin, scale, x to y)
            val strength = 1 - Math.abs((y - curve(worldSpacePoint.x)) / curve(worldSpacePoint.x))
            image[x, y] = (normalize(((Float3(1f, 0f, 0f) * strength + Float3(1f, 1f, 1f) * (1 - strength)))) * 255f).toIntRGB()
        }
    }
}

fun drawLineDDA(startPixel : Pair<Int, Int>, endPixel : Pair<Int, Int>, image : Image) {
    var dx = endPixel.first - startPixel.first
    var dy = endPixel.second - startPixel.second
    val step = if (Math.abs(dx) >= Math.abs(dy)) Math.abs(dx) else Math.abs(dy)
    dx /= step
    dy /= step
    var x = startPixel.first
    var y = startPixel.second
    var i = 1
    while (i <= step) {
        image[x, y] = color
        x += dx
        y += dy
        i++
    }
}

fun drawLine(startPixel : Pair<Int, Int>, endPixel : Pair<Int, Int>, image : Image) {
    val dx = endPixel.first - startPixel.first
    val dy = endPixel.second - startPixel.second
    val dErr = Math.abs(dy.toFloat() / dx.toFloat())
    var err = 0.0
    var y = startPixel.second

    for (x in startPixel.first..endPixel.first) {
        image[x, y] = color
        err += dErr
        if (err >= 0.5) {
            y += Math.signum(dy.toDouble()).roundToInt()
            err -= 1.0
        }
    }
}

fun floatingRange(start : Float, end : Float, dt : Float) : Sequence<Float> {
    val steps = ((end - start) / dt).toInt()
    val sgn = Math.signum(end - start)
    return (0 until steps).map { start + it * sgn * dt }.asSequence()
}

fun mapToPixel(width : Int, height : Int, origin : Float2, scale : Float2, point : Float2) : Pair<Int, Int> {
    // 0, 0 maps to origin - scale
    // width, height maps to origin + scale
    val size = Float2(width.toFloat(), height.toFloat())
    val newPoint = (((((point - (origin * Float2(1f, -1f))) * Float2(1f, -1f)) / scale) + Float2(0.5f, 0.5f)) * size)

//    println("width: $width, height: $height, origin: $origin, scale: $scale, point: $point, result: $newPoint")
    return (newPoint.x.roundToInt()) to (newPoint.y.roundToInt())
}

fun mapToPoint(width : Int, height : Int, origin : Float2, scale : Float2, point : Pair<Int, Int>) : Float2 {
    val p = Float2(point.first.toFloat(), (height - point.second).toFloat())
    val screenSize = Float2(width.toFloat(), height.toFloat())
    return (((p / screenSize) * scale) - (scale * 0.5f)) + origin
}