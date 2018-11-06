import com.curiouscreature.kotlin.math.Float2

sealed class Curve

open class ExplicitCurve(private val f : (Float) -> Float) : Curve() {
    operator fun invoke(x : Float) = f(x)
}
open class ImplicitCurve(private val test : (Float, Float) -> Boolean) : Curve()

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
val size = 800
val color = 0xFF0000 // Red
// END CONSTANTS

fun drawCurve(origin : Float2, scale : Float2, curve : Curve) : Image = when (curve) {
    is ExplicitCurve -> drawExplicitCurve(origin, scale, curve, scale.x / size)
    is ImplicitCurve -> TODO("Implement drawing implicit curves via testing")
}

fun drawExplicitCurve(origin : Float2, scale : Float2, curve : ExplicitCurve, dt : Float) : Image {
    // Find points along the curve inside window, draw those points to the image

    val image = Image(size, size, IntArray(size * size) { 0xFFFFFF }) // Blank white image
    val halfSize = scale * 0.5f

    val (start, end) = origin - halfSize to origin + halfSize
    val range = floatingRange(start.x, end.x, dt)

    for (x in range) {
        val y = curve(x) // Find f(x)
        val pixelPosition = mapToPixel(size, size, origin, scale, Float2(x, y))
        val i = pixelPosition.first + pixelPosition.second * size
        if (i < size * size && i >= 0) image[pixelPosition.first, pixelPosition.second] = color
    }

    return image
}

fun floatingRange(start : Float, end : Float, dt : Float) : Sequence<Float> {
    val steps = ((end - start) / dt).toInt()
    return (0 until steps).map { start + it * dt }.asSequence()
}

fun mapToPixel(width : Int, height : Int, origin : Float2, scale : Float2, point : Float2) : Pair<Int, Int> {
    // 0, 0 maps to origin - scale
    // width, height maps to origin + scale
    val size = Float2(width.toFloat(), height.toFloat())
    val newPoint = (((((point - (origin * Float2(1f, -1f))) * Float2(1f, -1f)) / scale) + Float2(0.5f, 0.5f)) * size)

//    println("width: $width, height: $height, origin: $origin, scale: $scale, point: $point, result: $newPoint")
    return (newPoint.x.toInt()) to (newPoint.y.toInt())
}