import com.curiouscreature.kotlin.math.Float2
import com.curiouscreature.kotlin.math.Float3
import kotlin.random.Random

fun main() {
    val random = Random(System.nanoTime())
    val image = Image.create(800, 600) { x, y ->
        val xRange = x / 800.toFloat()
        val yRange = y / 600.toFloat()
        val distance = xRange * xRange + yRange * yRange
        Float3(distanceMap(distance), distanceMap(distance), distanceMap(distance)).toIntRGB()
    }

    val averageFilter = Filter(
        1f, 1f, 1f,
        1f, 1f, 1f,
        1f, 1f, 1f
    ).normalize()

    val edgeDetection1 = Filter(
        1f, 0f, -1f,
        0f, 0f, 0f,
        -1f, 0f, 1f
    )

    val sharpen = Filter(
        0f, -1f, 0f,
        -1f, 5f, -1f,
        0f, -1f, 0f
    )

    image.save("original")
    image.apply(averageFilter).save("averageFilter")
    image.apply(edgeDetection1).apply(averageFilter).apply(edgeDetection1).save("edgeDetection1")
    image.apply(sharpen, 5).save("sharpen")

    val scene = RayTracingScene()

    scene.addShape(SceneObject(
        shape = Sphere(5f, Float3(0f, 0f, 30f)),
        material = Material(
            diffuse = Float3(0.25f, 0.75f, 0.4f),
            specularConstant = 0.5f
        )
    ))

    scene.addShape(SceneObject(
        shape = Sphere(3f, Float3(8f, 0f, 30f)),
        material = Material(
            diffuse = Float3(0.75f, 0.4f, 0.25f),
            specularConstant = 0.5f
        )
    ))

    scene.addShape(SceneObject(
        shape = Sphere(3f, Float3(-8f, 0f, 30f)),
        material = Material(
            diffuse = Float3(0.25f, 0.4f, 0.75f),
            specularConstant = 0.5f
        )
    ))

//    scene.addShape(SceneObject(
//        shape = Sphere(2f, Float3(-10f, 0f, 20f)),
//        material = Material(
//            diffuse = Float3(0.5f, 0.75f, 0.5f),
//            specularConstant = 0.2f
//        )
//    ))

//    scene.addShape(SceneObject(
//        shape = Sphere(2f, Float3(0f, 0f, 20f)),
//        material = Material(
//            diffuse = Float3(0.5f, 0.75f, 0.5f),
//            specularConstant = 0.2f
//        )
//    ))
//
//    scene.addShape(SceneObject(
//        shape = Sphere(2f, Float3(10f, 0f, 20f)),
//        material = Material(
//            diffuse = Float3(0.5f, 0.75f, 0.5f),
//            specularConstant = 0.2f
//        )
//    ))
//
//    scene.addShape(SceneObject(
//        shape = Sphere(2f, Float3(-10f, 0f, 20f)),
//        material = Material(
//            diffuse = Float3(0.5f, 0.75f, 0.5f),
//            specularConstant = 0.2f
//        )
//    ))

    scene.addShape(SceneObject(
        shape = Plane(-1f, Float3(0f, 1f, 0f)),
        material = Material(
            diffuse = Float3(1f, 0f, 0f),
            specularConstant = 0.2f
        )
    ))

    scene.addLight(PointLight(
        position = Float3(0f, 2f, -4f),
        color = Float3(1f, 1f, 1f),
        brightness = 0.8f
    ))

//    val rayTracingImage = Image.renderScene(800, 600, scene)
//    rayTracingImage.save("rayTracingImage")
//    rayTracingImage.apply(sharpen, 5).apply(edgeDetection1).save("garbledRayTracing")

    val curve = Quadratic(1f, 0f, 0f) // y = x^2
    drawCurve(Float2(0f, -5f), Float2(10f, 10f), curve).save("quadratic")
    drawCurve(Float2(0f, 0f), Float2(10f, 10f), Cubic(1f, 1f, 1f, 1f)).save("cubic")
}

fun distanceMap(x : Float) : Float = if (x <= 1.0f) x else 0f