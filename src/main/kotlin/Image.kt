import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

data class Image(val width : Int, val height : Int, val pixels : IntArray = IntArray(width * height)) {
    operator fun set(x : Int, y : Int, color : Int) {
        pixels[x + y * width] = color
    }

    operator fun get(x : Int, y : Int) = pixels[x + y * width]

    fun transform(function : (Int) -> Int) {
        for (i in 0..(width*height - 1)) {
            pixels[i] = function(pixels[i])
        }
    }

    fun save(filename : String) {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        image.setRGB(0, 0, width, height, pixels, 0, width)
        ImageIO.write(image, "png", File("src/main/resources/$filename.png"))
    }

    fun apply(filter : Filter) = filter.apply(this)
    fun apply(filter : Filter, times : Int) : Image {
        var image = this
        for (i in 1..times) {
            image = image.apply(filter)
        }

        return image
    }

    companion object {
        fun create(width : Int, height : Int, renderer : (Int, Int) -> Int) : Image {
            val pixels = (0..(width*height - 1)).map { i -> renderer(i % width, i / width) }.toIntArray()
            return Image(width, height, pixels)
        }

        fun save(filename : String, width : Int, height : Int, renderer : (Int, Int) -> Int) {
            create(width, height, renderer).save(filename)
        }

        fun renderScene(width : Int, height : Int, scene : Scene) = Image(width, height, scene.render(width, height))
    }
}