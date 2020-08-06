package wee.vietinbank.kiosk.gl3

import android.opengl.GLES31
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class OverlayRenderer : GLSurfaceView.Renderer {

    // abbreviation for "Model View Projection Matrix"
    private val mvpMatrix = FloatArray(16)

    private val projectionMatrix = FloatArray(16)

    private val viewMatrix = FloatArray(16)

    private val rotationMatrix = FloatArray(16)

    private var drawObj : Straight? = null

    private var width = 0

    private var height = 0

    private var angle = 0f

    //used the touch listener to move the cube up/down (y) and left/right (x)
    var y = 0f

    var x = 0f

    /**
     * [GLSurfaceView.Renderer] implement
     */
    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        //set the clear buffer color to light gray.
        GLES31.glClearColor(0f, 0f, 0f, 1f)
        //initialize the cube code for drawing.
        drawObj = Straight()
        //if we had other objects setup them up here as well.
    }

    // Draw a triangle using the shader pair created in onSurfaceCreated()
    override fun onDrawFrame(gl: GL10) {

        // Clear the color buffer  set above by glClearColor.
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT or GLES31.GL_DEPTH_BUFFER_BIT)

        //need this otherwise, it will over right stuff and the cube will look wrong!
        GLES31.glEnable(GLES31.GL_DEPTH_TEST)

        // Set the camera position (View matrix)  note Matrix is an include, not a declared method.
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        // Create a rotation and translation for the cube
        Matrix.setIdentityM(rotationMatrix, 0)

        //move the cube up/down and left/right
        Matrix.translateM(rotationMatrix, 0, x, y, 0f)

        //mangle is how fast, x,y,z which directions it rotates.
        Matrix.rotateM(rotationMatrix, 0, angle, 0.0f, 1.0f, 0.0f)

        // combine the model with the view matrix
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, rotationMatrix, 0)

        // combine the model-view with the projection matrix
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)
        drawObj?.draw(mvpMatrix)

        //change the angle, so the cube will spin.
        angle += .1f
    }

    // Handle surface changes
    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        this.width = width
        this.height = height
        // Set the viewport
        GLES31.glViewport(0, 0, this.width, this.height)
        val aspect = width.toFloat() / height
        // this projection matrix is applied to object coordinates
        // no idea why 53.13f, it was used in another example and it worked.
        Matrix.perspectiveM(projectionMatrix, 0, 53.13f, aspect, Z_NEAR, Z_FAR)
    }


}