package wee.vietinbank.kiosk.gl3

import android.opengl.GLES31
import java.nio.FloatBuffer

private const val width = 69
private const val height = 69
private const val scale = 0.01f

val pixelArr: IntArray by lazy {
    val arr = mutableListOf<Int>()
    var alpha = 0
    for (y in height / 2 downTo height / -2) {
        var z = 0
        //var s = ""
        for (x in width / -2..width / 2) {
            //s += " $z"
            arr.add(x)
            arr.add(y)
            arr.add(z)
            when {
                x < width / -2 + alpha -> {
                    z++
                }
                x > width / 2 - alpha - 1 -> {
                    z--
                }
            }
        }
        //d(s)
        if (y > 0) {
            alpha += 1
        } else {
            alpha -= 1
        }
    }
    arr.toIntArray()
}

private val pyramidPoints: FloatArray by lazy {
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
            //d("Pyramid", "($x1,$y1,$z1)->($x2,$y2,$z2)")
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
    arr.toFloatArray()
}

class Straight {

    companion object {

        private const val COORDINATES_PER_VERTEX = 3

        private const val VERTEX_SHADER_CODE = """
            #version 300 es
            uniform mat4 uMVPMatrix;
            in vec4 vPosition;
            void main() {
               gl_Position = uMVPMatrix * vPosition;
            }
        """

        private const val FRAGMENT_SHADER_CODE = """
            #version 300 es
            precision mediump float;
            uniform vec4 vColor;
            out vec4 fragColor;
            void main() {
              fragColor = vColor;
            }
        """

    }
    // this is the initial data, which will need to translated into the mVertices variable in the consturctor.
    private val verticesBuffer: FloatBuffer

    private var verticesData = pyramidPoints

    private var programHandle: Int = 0

    private var mvpMatrixHandle: Int = 0

    private var colorHandle: Int = 0

    init {
        //first setup the mVertices correctly.
        verticesBuffer = newFloatBuffer(verticesData.size)
                .put(verticesData)
        verticesBuffer.position(0)
        initProgram()
    }

    /**
     * init program and copy vertices from cpu to the gpu
     */
    private fun initProgram() {

        programHandle = GLES31.glCreateProgram()
        if (programHandle == 0) {
            println("some kind of error")
            return
        }

        // Load the vertex/fragment shader
        val vertexShader: Int = loadShader(GLES31.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        val fragmentShader: Int = loadShader(GLES31.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)
        GLES31.glAttachShader(programHandle, vertexShader)
        GLES31.glAttachShader(programHandle, fragmentShader)

        // Bind vPosition to attribute 0
        GLES31.glBindAttribLocation(programHandle, 0, "vPosition")

        GLES31.glLinkProgram(programHandle)
    }

    fun draw(mvpMatrix: FloatArray?) {

        // Use the program object
        GLES31.glUseProgram(programHandle)

        // get handle to shape's transformation matrix
        mvpMatrixHandle = GLES31.glGetUniformLocation(programHandle, "uMVPMatrix")
        checkGlError("glGetUniformLocation")

        // get handle to fragment shader's vColor member
        colorHandle = GLES31.glGetUniformLocation(programHandle, "vColor")

        // Apply the projection and view transformation
        GLES31.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        checkGlError("glUniformMatrix4fv")

        // Enable vertex
        val vertexPosIndex = 0
        verticesBuffer.position(vertexPosIndex) //just in case.  We did it already though.
        //add all the points to the space, so they can be correct by the transformations.
        //would need to do this even if there were no transformations actually.
        GLES31.glVertexAttribPointer(
                vertexPosIndex,
                3,
                GLES31.GL_FLOAT,
                false,
                0,
                verticesBuffer
        )
        GLES31.glEnableVertexAttribArray(vertexPosIndex)

        val vertexCount = verticesData.size / COORDINATES_PER_VERTEX
        onDraw(vertexCount)
    }

    private fun onDraw(vertexCount: Int) {
        GLES31.glUniform4fv(colorHandle, 1, GL_WHITE, 0)
        GLES31.glDrawArrays(GLES31.GL_LINES, 0, vertexCount)
    }

}