package wee.vietinbank.kiosk.gl3

import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.opengl.GLES31
import android.util.Log
import wee.vietinbank.kiosk.App
import java.nio.*

val hasOpenGLES30: Boolean
    get() {
        val am = App.instance.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = am.deviceConfigurationInfo
        return info.reqGlEsVersion >= 0x30000
    }

fun newFloatBuffer(size: Int): FloatBuffer {
    val buffer = ByteBuffer.allocateDirect(size * 4)
    buffer.order(ByteOrder.nativeOrder())
    return buffer.asFloatBuffer()
}

fun newDoubleBuffer(size: Int): DoubleBuffer {
    val buffer = ByteBuffer.allocateDirect(size * 8)
    buffer.order(ByteOrder.nativeOrder())
    return buffer.asDoubleBuffer()
}

fun newByteBuffer(size: Int): ByteBuffer {
    val buffer = ByteBuffer.allocateDirect(size)
    buffer.order(ByteOrder.nativeOrder())
    return buffer
}

fun newShortBuffer(size: Int): ShortBuffer {
    val buffer = ByteBuffer.allocateDirect(size * 2)
    buffer.order(ByteOrder.nativeOrder())
    return buffer.asShortBuffer()
}

fun newCharBuffer(size: Int): CharBuffer {
    val buffer = ByteBuffer.allocateDirect(size * 2)
    buffer.order(ByteOrder.nativeOrder())
    return buffer.asCharBuffer()
}

fun newIntBuffer(size: Int): IntBuffer {
    val buffer = ByteBuffer.allocate(size * 4)
    buffer.order(ByteOrder.nativeOrder())
    return buffer.asIntBuffer()
}

fun newLongBuffer(size: Int): LongBuffer {
    val buffer = ByteBuffer.allocate(size * 8)
    buffer.order(ByteOrder.nativeOrder())
    return buffer.asLongBuffer()
}

val GL_WHITE = floatArrayOf(
        1f,
        1f,
        1f,
        1f
)

val GL_RED = floatArrayOf(
        Color.red(Color.RED) / 255f,
        Color.green(Color.RED) / 255f,
        Color.blue(Color.RED) / 255f,
        1f
)

val GL_GREEN = floatArrayOf(
        Color.red(Color.GREEN) / 255f,
        Color.green(Color.GREEN) / 255f,
        Color.blue(Color.GREEN) / 255f,
        1f
)

val GL_BLUE = floatArrayOf(
        Color.red(Color.BLUE) / 255f,
        Color.green(Color.BLUE) / 255f,
        Color.blue(Color.BLUE) / 255f,
        1f
)

val GL_YELLOW = floatArrayOf(
        Color.red(Color.YELLOW) / 255f,
        Color.green(Color.YELLOW) / 255f,
        Color.blue(Color.YELLOW) / 255f,
        1f
)

val GL_CYAN = floatArrayOf(
        Color.red(Color.CYAN) / 255f,
        Color.green(Color.CYAN) / 255f,
        Color.blue(Color.CYAN) / 255f,
        1f
)

val GL_GRAY = floatArrayOf(
        Color.red(Color.GRAY) / 255f,
        Color.green(Color.GRAY) / 255f,
        Color.blue(Color.GRAY) / 255f,
        1f
)


val width = 7
val height = 5

val pixelArr = intArrayOf(
        -3, 2, 0,
        -2, 2, 0,
        -1, 2, 0,
        0, 2, 0,
        1, 2, 0,
        2, 2, 0,
        3, 2, 0,    //nextXIndex: 21

        -3, 1, 0,
        -2, 1, 1,
        -1, 1, 1,
        0, 1, 1,
        1, 1, 1,
        2, 1, 1,
        3, 1, 0,    //nextXIndex: 42

        -3, 0, 0,
        -2, 0, 1,
        -1, 0, 2,
        0, 0, 2,
        1, 0, 2,
        2, 0, 1,
        3, 0, 0,    //nextXIndex: 63

        -3, -1, 0,
        -2, -1, 1,
        -1, -1, 1,
        0, -1, 1,
        1, -1, 1,
        2, -1, 1,
        3, -1, 0,   //nextXIndex: 84

        -3, -2, 0,
        -2, -2, 0,
        -1, -2, 0,
        0, -2, 0,
        1, -2, 0,
        2, -2, 0,
        3, -2, 0    //nextXIndex: 105
)
val scale = 0.1f

val pyramidPoints: FloatArray
    get() {
        val arr = mutableListOf<Float>()
        val rowSize = width * 3
        for (i in 0..pixelArr.lastIndex - 3 step 3) {

            val x1 = pixelArr[i] * scale
            val y1 = pixelArr[i + 1] * scale
            val z1 = pixelArr[i + 2] * scale

            val nextXIndex = i + 3
            //if current coordinate is not end of row add new horizontal line
            if (nextXIndex % rowSize != 0) {
                val x2 = pixelArr[i + 3] * scale
                val y2 = pixelArr[i + 4] * scale
                val z2 = pixelArr[i + 5] * scale
                Log.d("Pyramid", "($x1,$y1,$z1)->($x2,$y2,$z2)")
                arr.apply {
                    add(x1)
                    add(y1)
                    add(z1)
                    add(x2)
                    add(y2)
                    add(z2)
                }
            }
            //if current row is not last row add new vertical line

            if (i < width * 3 * (height - 1)) {
                val x3 = pixelArr[i + width * 3] * scale
                val y3 = pixelArr[i + width * 3 + 1] * scale
                val z3 = pixelArr[i + width * 3 + 2] * scale
                arr.apply {
                    add(x1)
                    add(y1)
                    add(z1)
                    add(x3)
                    add(y3)
                    add(z3)
                }
            }

        }


        return arr.toFloatArray()
    }

const val Z_NEAR = 1f

const val Z_FAR = 40f

/**
 * Create a shader object, load the shader source, an compile the shader.
 */
fun loadShader(type: Int, shaderCode: String?): Int {
    val compiled = IntArray(1)

    // Create the shader object
    val shader: Int = GLES31.glCreateShader(type)
    if (shader == 0) {
        return 0
    }

    // Load the shader source
    GLES31.glShaderSource(shader, shaderCode)

    // Compile the shader
    GLES31.glCompileShader(shader)

    // Check the compile status
    GLES31.glGetShaderiv(shader, GLES31.GL_COMPILE_STATUS, compiled, 0)
    if (compiled[0] == 0) {
        println("Erorr!!!!")
        println(GLES31.glGetShaderInfoLog(shader))
        GLES31.glDeleteShader(shader)
        return 0
    }
    return shader
}

/**
 * Utility method for debugging OpenGL calls. Provide the name of the call
 * just after making it:
 *
 * <pre>
 * mColorHandle = GLES31.glGetUniformLocation(mProgram, "vColor");
 * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
 *
 *
 * If the operation is not successful, the check throws an error.
 *
 * @param glOperation - Name of the OpenGL call to check.
 */
fun checkGlError(glOperation: String) {
    var error: Int
    while (GLES31.glGetError().also { error = it } != GLES31.GL_NO_ERROR) {
        println("$glOperation: glError $error")
        throw RuntimeException("$glOperation: glError $error")
    }
}