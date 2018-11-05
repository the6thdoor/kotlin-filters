import com.curiouscreature.kotlin.math.Float3

class Filter(vararg val kernel : Float) {
    val dimension = Math.sqrt(kernel.size.toDouble()).toInt()

    fun normalize() = Filter(*kernel.map { it / kernel.sum() }.toFloatArray())

    fun apply(image : Image) : Image {
        val result = Image(image.width - dimension + 1, image.height - dimension + 1)

        fun applyKernel(centerX : Int, centerY : Int) : Float3 =
            image[centerX - 1, centerY - 1].toRGBTriple() * kernel[0] +
                    image[centerX    , centerY - 1].toRGBTriple() * kernel[1] +
                    image[centerX + 1, centerY - 1].toRGBTriple() * kernel[2] +
                    image[centerX - 1, centerY    ].toRGBTriple() * kernel[3] +
                    image[centerX    , centerY    ].toRGBTriple() * kernel[4] +
                    image[centerX + 1, centerY    ].toRGBTriple() * kernel[5] +
                    image[centerX - 1, centerY + 1].toRGBTriple() * kernel[6] +
                    image[centerX    , centerY + 1].toRGBTriple() * kernel[7] +
                    image[centerX + 1, centerY + 1].toRGBTriple() * kernel[8]

        for (y in 0..image.height - dimension) {
            for (x in 0..image.width - dimension) {
                val centerX = x + 1
                val centerY = y + 1
                val color = applyKernel(centerX, centerY)
                result[x, y] = color.clamp(0f, 1f).toIntRGB()
            }
        }

        return result
    }
}

fun Float3.clamp(lower : Float, upper : Float) = this.transform { Math.max(lower, Math.min(upper, it)) }
fun Float3.toIntRGB() : Int {
    val shifted = this * 255f
    return (shifted.r.toInt() shl 16) or (shifted.g.toInt() shl 8) or (shifted.b.toInt())
}
fun Int.toRGBTriple() : Float3 = Float3(((this and 0xFF0000) shr 16).toFloat(), ((this and 0x00FF00) shr 8).toFloat(), (this and 0x0000FF).toFloat()) * (1f / 255f)