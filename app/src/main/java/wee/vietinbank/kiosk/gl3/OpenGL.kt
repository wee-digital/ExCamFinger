package wee.vietinbank.kiosk.gl3

import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.opengl.GLES31
import android.util.Log
import wee.digital.library.extension.parse
import wee.digital.library.extension.readAsset
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




val facePoint: FloatArray
    get() {
        return readAsset("vertices.json")
                ?.parse(Array<Float>::class.java)
                ?.toFloatArray() ?: floatArrayOf()
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