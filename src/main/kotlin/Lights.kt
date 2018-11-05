import com.curiouscreature.kotlin.math.Float3

sealed class Light(val position : Float3, val color : Float3)

class PointLight(position : Float3, color : Float3, val brightness : Float) : Light(position, color)