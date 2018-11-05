import com.curiouscreature.kotlin.math.Float3

data class Material(val diffuse : Float3, val specularConstant : Float)
data class SceneObject(val shape : Shape, val material : Material) {
    fun intersects(ray : Ray) : SceneCollision {
        val hit = shape.intersects(ray)
        return when (hit) {
            is Collision.Miss -> SceneCollision.Miss
            is Collision.Hit -> SceneCollision.Hit(hit.time, this)
        }
    }
}

sealed class Collision {
    class Hit(val time : Float) : Collision()
    object Miss : Collision()
}

sealed class SceneCollision {
    class Hit(val time : Float, val obj : SceneObject) : SceneCollision()
    object Miss : SceneCollision()
}