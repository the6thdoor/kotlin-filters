import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.dot
import com.curiouscreature.kotlin.math.normalize

data class Ray(val origin : Float3, val direction : Float3)

abstract class Shape {
    abstract fun intersects(ray : Ray) : Collision
    abstract fun getNormal(point : Float3) : Float3
}

data class Sphere(val radius : Float, val center : Float3) : Shape() {
    override fun intersects(ray : Ray) : Collision {
        val b = 2 * dot(ray.direction, ray.origin - center)
        val c = dot(ray.origin - center, ray.origin - center) - (radius * radius)
        val delta = b * b - 4 * c
        val epsilon = 1e-4f

        if (delta < epsilon) {
            return Collision.Miss
        }

        val t1 = (-b + Math.sqrt(delta.toDouble()).toFloat()) / 2f
        val t2 = (-b - Math.sqrt(delta.toDouble()).toFloat()) / 2f

        val result = Math.min(t1, t2)

        if (result < epsilon) {
            return Collision.Miss
        }

        return Collision.Hit(result)
    }

    override fun getNormal(point: Float3) = normalize(point - center)
}

data class Plane(val origin : Float3, val normal : Float3) : Shape() {
    override fun intersects(ray: Ray): Collision {
        val denom = dot(ray.direction, normal)
        val epsilon = 1e-6f
        if (denom > epsilon) {
            val t = dot(origin - ray.origin, normal) / denom
            if (t >= 0) return Collision.Hit(t)
        }

        return Collision.Miss
    }

    override fun getNormal(point: Float3) = normal
}