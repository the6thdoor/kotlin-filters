import com.curiouscreature.kotlin.math.*
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

abstract class Scene {
    abstract fun render(width : Int, height : Int) : IntArray
}

class RayTracingScene : Scene() {
    val objects = mutableListOf<SceneObject>()
    val lights = mutableListOf<Light>()
    val maxDepth = 20

    val scale = 5f

    fun addShape(obj : SceneObject) {
        objects.add(obj)
    }

    fun addLight(light : Light) {
        lights.add(light)
    }

    override fun render(width : Int, height : Int) : IntArray = runBlocking {
        var pixels = IntArray(width * height)

        val time = measureTimeMillis {
            pixels = renderAsync2(width, height)
//            pixels = renderBlocking(width, height)
        }

        val timeElapsedInSeconds = time / 1000.0
        println("Finished rendering scene in $timeElapsedInSeconds seconds.")

        pixels
    }

    suspend fun renderAsync(width : Int, height : Int) : IntArray = runBlocking {
        val jobs = List(width * height) {
            async {
                val (x, y) = it % width to it / width
                val eye = Float3(0f, 0f, -1f)

                val screenPos = Float2(x.toFloat(), (height - y).toFloat())
                val screenSize = Float2(width.toFloat(), height.toFloat())
                val centeredPos = screenPos - (screenSize * 0.5f)
                val screenPoint = centeredPos / width.toFloat()

                val ray = Ray(eye, normalize(Float3(screenPoint, 0f) - eye))
                trace(ray, 0).clamp(0f, 1f).toIntRGB()
            }
        }

        jobs.awaitAll().toIntArray()
    }

    fun renderAsync2(width : Int, height : Int) : IntArray = runBlocking {
        val pixels = IntArray(width * height)
        repeat(width * height) { i ->
            launch {
                val (x, y) = i % width to i / width
                val eye = Float3(0f, 0f, -1f)

                val screenPos = Float2(x.toFloat(), (height - y).toFloat())
                val screenSize = Float2(width.toFloat(), height.toFloat())
                val centeredPos = screenPos - (screenSize * 0.5f)
                val screenPoint = centeredPos / width.toFloat()

                val ray = Ray(eye, normalize(Float3(screenPoint, 0f) - eye))
                pixels[i] = trace(ray, 0).clamp(0f, 1f).toIntRGB()
            }
        }

        pixels
    }

    fun renderBlocking(width : Int, height : Int) : IntArray {
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val eye = Float3(0f, 0f, -1f)

                val screenPos = Float2(x.toFloat(), (height - y).toFloat())
                val screenSize = Float2(width.toFloat(), height.toFloat())
                val centeredPos = screenPos - (screenSize * 0.5f)
                val screenPoint = centeredPos / width.toFloat()

                val ray = Ray(eye, normalize(Float3(screenPoint, 0f) - eye))
                pixels[x + y * width] = trace(ray, 0).clamp(0f, 1f).toIntRGB()
            }
        }

        return pixels
    }

    fun trace(ray : Ray, depth : Int) : Float3 {
        if (depth >= maxDepth) {
            return Float3(0.25f, 0.25f, 1.0f)
        }

        var nearestHit : SceneCollision = SceneCollision.Miss
        val ambient = Float3(0.1f, 0.1f, 0.1f)
        for (obj in objects) {
            val hit = obj.intersects(ray)
            if (hit is SceneCollision.Hit && (nearestHit is SceneCollision.Miss || nearestHit is SceneCollision.Hit && hit.time < nearestHit.time))
                nearestHit = hit
        }

        if (nearestHit is SceneCollision.Hit) {
            var diffuseStrength = 0f
            var specular = Float3(0f, 0f, 0f)
            val hitPoint = ray.origin + (ray.direction * nearestHit.time)
            val normal = nearestHit.obj.shape.getNormal(hitPoint)
            val epsilon = 1e-4f

            for (light in lights) {
                if (light is PointLight) {
                    val toLight = normalize(light.position - hitPoint)
                    val shadowRay = Ray(hitPoint + (toLight * epsilon), toLight)
                    if (objects.all { it.shape.intersects(shadowRay) is Collision.Miss }) {
                        val k = Math.max(0f, dot(toLight, normal))
                        diffuseStrength += light.brightness * k

                        val reflectionDir = normalize(reflect(ray.direction, normal))
                        val specularRay = Ray(hitPoint + (reflectionDir * epsilon), reflectionDir)
                        specular += trace(specularRay, depth + 1) * nearestHit.obj.material.specularConstant
                    }
                }
            }

            return (ambient * nearestHit.obj.material.diffuse) + (nearestHit.obj.material.diffuse * diffuseStrength) + specular
        }

        return Float3(0.25f, 0.25f, 1.0f)
    }
}