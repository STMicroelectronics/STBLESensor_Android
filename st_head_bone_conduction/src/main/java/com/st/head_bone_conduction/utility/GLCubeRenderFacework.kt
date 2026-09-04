package com.st.head_bone_conduction.utility

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import androidx.annotation.RawRes
import com.st.head_bone_conduction.R
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.roundToInt

internal data class ObjModelFacework(
    val vertices: FloatArray,
    val normals: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ObjModelFacework

        if (!vertices.contentEquals(other.vertices)) return false
        if (!normals.contentEquals(other.normals)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = vertices.contentHashCode()
        result = 31 * result + normals.contentHashCode()
        return result
    }
}

internal class GLCubeRenderFacework : GLSurfaceView.Renderer {

    private val mModelMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mProjectionMatrix = FloatArray(16)
    private val mMVPMatrix = FloatArray(16)
    private val mTempMultMatrix = FloatArray(16)

    private var mPositions: FloatBuffer
    private var mNormals: FloatBuffer
    private var vertexCount = 0

    private var mProgramHandle = 0
    private var mMVPMatrixHandle = 0
    private var mModelMatrixHandle = 0
    private var mPositionHandle = 0
    private var mNormalHandle = 0
    private var mMaterialKaHandle = 0
    private var mMaterialKdHandle = 0
    private var mMaterialKsHandle = 0
    private var mMaterialNsHandle = 0
    private var mLightDirectionHandle = 0
    private var mLightColorHandle = 0
    private var mAmbientStrengthHandle = 0

    private var mContext: Context? = null
    private var mBgColor = 0

    private val SCALE_CUBE_MAX = 0.9f
    private val SCALE_CUBE_MIN = 0.3f
    private val SCALE_CUBE_FACTOR = SCALE_CUBE_MAX - SCALE_CUBE_MIN
    private var scale_cube = SCALE_CUBE_MAX - SCALE_CUBE_MIN

    private val mRotationMatrix = FloatArray(16)
    private val mInverseRotationMatrix = FloatArray(16)

    private val MAX_HW_RENDERING_RATE_HZ = 60
    private var rendering_start_time: Long = 0
    private val RENDERING_RATE_WINDOW_SIZE = 16
    private val rendering_rate = IntArray(RENDERING_RATE_WINDOW_SIZE)
    private var rendering_rate_index = 0
    private var rendering_rate_avg = 0f

    private var materialKa = floatArrayOf(1.0f, 1.0f, 1.0f)
    private var materialKd = floatArrayOf(0.8f, 0.8f, 0.8f)
    private var materialKs = floatArrayOf(0.5f, 0.5f, 0.5f)
    private var materialNs = 225.0f

    private val lightDirection = floatArrayOf(0.0f, 0.0f, 1.0f)
    private val lightColor = floatArrayOf(1.0f, 1.0f, 1.0f)
    private val ambientStrength = 0.2f

    constructor(c: Context?, color: Int) {
        mBgColor = color
        mContext = c

        loadMtlFromRawResource(R.raw.free_head_swan3b_mtl)
        val model = loadObjFromRawResource(R.raw.free_head_swan3b_obj)

        vertexCount = model.vertices.size / 3

        mPositions = ByteBuffer.allocateDirect(model.vertices.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
                put(model.vertices)
                position(0)
            }

        mNormals = ByteBuffer.allocateDirect(model.normals.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
                put(model.normals)
                position(0)
            }

        Matrix.setIdentityM(mRotationMatrix, 0)
        Matrix.setIdentityM(mInverseRotationMatrix, 0)

        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 1f, 0f, 0f, -5f, 0f, 1f, 0f)
    }

    private fun readTextFileFromRawResource(context: Context?, @RawRes resourceId: Int): String {
        val inputStream = context!!.resources.openRawResource(resourceId)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val body = StringBuilder()
        try {
            var nextLine: String?
            while (bufferedReader.readLine().also { nextLine = it } != null) {
                body.append(nextLine).append('\n')
            }
        } catch (e: IOException) {
            throw RuntimeException("Error reading raw resource $resourceId", e)
        }
        return body.toString()
    }

    private fun compileShader(shaderType: Int, shaderSource: String): Int {
        var shaderHandle = GLES20.glCreateShader(shaderType)
        if (shaderHandle != 0) {
            GLES20.glShaderSource(shaderHandle, shaderSource)
            GLES20.glCompileShader(shaderHandle)
            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(shaderHandle)
                shaderHandle = 0
            }
        }
        return shaderHandle
    }

    private fun createAndLinkProgram(vHandle: Int, fHandle: Int, attrs: Array<String>): Int {
        var programHandle = GLES20.glCreateProgram()
        if (programHandle != 0) {
            GLES20.glAttachShader(programHandle, vHandle)
            GLES20.glAttachShader(programHandle, fHandle)
            for (i in attrs.indices) GLES20.glBindAttribLocation(programHandle, i, attrs[i])
            GLES20.glLinkProgram(programHandle)
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] == 0) {
                GLES20.glDeleteProgram(programHandle)
                programHandle = 0
            }
        }
        return programHandle
    }

    override fun onSurfaceCreated(glUnused: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(Color.red(mBgColor)/255f, Color.green(mBgColor)/255f, Color.blue(mBgColor)/255f, Color.alpha(mBgColor)/255f)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        val vShader = compileShader(GLES20.GL_VERTEX_SHADER, readTextFileFromRawResource(mContext, R.raw.per_pixel_vertex_shader_head))
        val fShader = compileShader(GLES20.GL_FRAGMENT_SHADER, readTextFileFromRawResource(mContext, R.raw.per_pixel_fragment_shader_head))
        mProgramHandle = createAndLinkProgram(vShader, fShader, arrayOf("a_Position", "a_Normal"))
    }

    override fun onSurfaceChanged(glUnused: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height.toFloat()
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1.0f, 1.0f, 1.0f, 10.0f)
    }
override fun onDrawFrame(glUnused: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(mProgramHandle)

        // Recupero uniform e attributi (meglio farlo in onSurfaceCreated, ma lo lascio come nel tuo codice)
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix")
        mModelMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_ModelMatrix")
        mMaterialKaHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MaterialKa")
        mMaterialKdHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MaterialKd")
        mMaterialKsHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MaterialKs")
        mMaterialNsHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MaterialNs")
        mLightDirectionHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_LightDirection")
        mLightColorHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_LightColor")
        mAmbientStrengthHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_AmbientStrength")
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position")
        mNormalHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Normal")

        // Materiali e luci
        GLES20.glUniform3f(mMaterialKaHandle, materialKa[0], materialKa[1], materialKa[2])
        GLES20.glUniform3f(mMaterialKdHandle, materialKd[0], materialKd[1], materialKd[2])
        GLES20.glUniform3f(mMaterialKsHandle, materialKs[0], materialKs[1], materialKs[2])
        GLES20.glUniform1f(mMaterialNsHandle, materialNs)
        GLES20.glUniform3f(mLightDirectionHandle, lightDirection[0], lightDirection[1], lightDirection[2])
        GLES20.glUniform3f(mLightColorHandle, lightColor[0], lightColor[1], lightColor[2])
        GLES20.glUniform1f(mAmbientStrengthHandle, ambientStrength)

        // MODEL MATRIX di base
        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -3.0f)

        // 1. CORREZIONE BASE: raddrizza il modello OBJ
        Matrix.rotateM(mModelMatrix, 0, -180f, 1.0f, 0.0f, 0.0f)

        // 2. ROTAZIONE SENSORE (movimento relativo rispetto al reset)
        synchronized(this) {
            // R_rel = R_attuale * R_reset^T (qui mInverseRotationMatrix memorizza la "reset")
            Matrix.multiplyMM(mTempMultMatrix, 0, mRotationMatrix, 0, mInverseRotationMatrix, 0)

            val currentM = mModelMatrix.clone()
            Matrix.multiplyMM(mModelMatrix, 0, currentM, 0, mTempMultMatrix, 0)
        }

        // 3. SCALA
        val modelBaseScale = 1.5f
        Matrix.scaleM(
            mModelMatrix,
            0,
            scale_cube * modelBaseScale,
            scale_cube * modelBaseScale,
            scale_cube * modelBaseScale
        )

        // Passaggio matrici agli shader
        GLES20.glUniformMatrix4fv(mModelMatrixHandle, 1, false, mModelMatrix, 0)
        Matrix.multiplyMM(mTempMultMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mTempMultMatrix, 0)
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0)

        // Disegno
        drawModel()
        updateRenderingRate()
    }

    private fun drawModel() {
        mPositions.position(0)
        GLES20.glVertexAttribPointer(
            mPositionHandle,
            3,
            GLES20.GL_FLOAT,
            false,
            0,
            mPositions
        )
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        mNormals.position(0)
        GLES20.glVertexAttribPointer(
            mNormalHandle,
            3,
            GLES20.GL_FLOAT,
            false,
            0,
            mNormals
        )
        GLES20.glEnableVertexAttribArray(mNormalHandle)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
    }

    private fun updateRenderingRate() {
        val now = System.currentTimeMillis()
        val period = now - rendering_start_time
        val currentRate =
            if (period == 0L) MAX_HW_RENDERING_RATE_HZ else (1000 / period).toInt()
        rendering_start_time = now

        rendering_rate_avg += (currentRate - rendering_rate[rendering_rate_index]).toFloat()
        rendering_rate[rendering_rate_index] = currentRate
        rendering_rate_index = (rendering_rate_index + 1) % RENDERING_RATE_WINDOW_SIZE
    }

    fun getRenderingRate(): Int =
        minOf(
            (rendering_rate_avg / RENDERING_RATE_WINDOW_SIZE).roundToInt(),
            MAX_HW_RENDERING_RATE_HZ
        )

    fun setScaleCube(scale: Float) {
        scale_cube = SCALE_CUBE_MIN + SCALE_CUBE_FACTOR * scale
    }

    /**
     * Set della rotazione a partire da un quaternione (qi, qj, qk, qs)
     */
    fun setRotation(qi: Float, qj: Float, qk: Float, qs: Float) {
        //così è ruotato di 45° con movimenti corretti
        //val qx = qj
        //val qy = qi
        //val qz = qk
        ///val qw = qs
        //test ora
        val qx = qk
        val qy = qj
        val qz = qi
        val qw = qs

        val rot = FloatArray(16)

        // Conversione quaternione -> matrice 4x4
        val xx = qx * qx
        val yy = qy * qy
        val zz = qz * qz
        val xy = qx * qy
        val xz = qx * qz
        val yz = qy * qz
        val wx = qw * qx
        val wy = qw * qy
        val wz = qw * qz

        rot[0] = 1f - 2f * (yy + zz)
        rot[1] = 2f * (xy + wz)
        rot[2] = 2f * (xz - wy)
        rot[3] = 0f

        rot[4] = 2f * (xy - wz)
        rot[5] = 1f - 2f * (xx + zz)
        rot[6] = 2f * (yz + wx)
        rot[7] = 0f

        rot[8] = 2f * (xz + wy)
        rot[9] = 2f * (yz - wx)
        rot[10] = 1f - 2f * (xx + yy)
        rot[11] = 0f

        rot[12] = 0f
        rot[13] = 0f
        rot[14] = 0f
        rot[15] = 1f

        synchronized(this) {
            System.arraycopy(rot, 0, mRotationMatrix, 0, 16)
        }
    }

    fun resetCube() {
        synchronized(this) {
            // Reset: memorizzare la rotazione attuale invertita (per rotazione relativa)
            Matrix.transposeM(mInverseRotationMatrix, 0, mRotationMatrix, 0)
        }
    }

    private fun loadObjFromRawResource(@RawRes resId: Int): ObjModel {
        val reader = BufferedReader(
            InputStreamReader(
                mContext!!.resources.openRawResource(resId)
            )
        )

        val tempV = mutableListOf<Float>()
        val tempN = mutableListOf<Float>()
        val finalV = mutableListOf<Float>()
        val finalN = mutableListOf<Float>()

        reader.useLines { lines ->
            lines.forEach { line ->
                val p = line.trim().split("\\s+".toRegex())
                if (p.size < 4) return@forEach
                when (p[0]) {
                    "v" -> {
                        tempV.add(p[1].toFloat())
                        tempV.add(p[2].toFloat())
                        tempV.add(p[3].toFloat())
                    }

                    "vn" -> {
                        tempN.add(p[1].toFloat())
                        tempN.add(p[2].toFloat())
                        tempN.add(p[3].toFloat())
                    }

                    "f" -> {
                        for (i in 1 until p.size - 2) {
                            addV(p[1], tempV, tempN, finalV, finalN)
                            addV(p[i + 1], tempV, tempN, finalV, finalN)
                            addV(p[i + 2], tempV, tempN, finalV, finalN)
                        }
                    }
                }
            }
        }

        return ObjModel(finalV.toFloatArray(), finalN.toFloatArray())
    }

    private fun addV(
        tok: String,
        tv: List<Float>,
        tn: List<Float>,
        fv: MutableList<Float>,
        fn: MutableList<Float>
    ) {
        val idx = tok.split('/')
        val vI = idx[0].toInt() - 1

        fv.add(tv[vI * 3])
        fv.add(tv[vI * 3 + 1])
        fv.add(tv[vI * 3 + 2])

        if (idx.size > 2 && idx[2].isNotEmpty()) {
            val nI = idx[2].toInt() - 1
            fn.add(tn[nI * 3])
            fn.add(tn[nI * 3 + 1])
            fn.add(tn[nI * 3 + 2])
        } else {
            fn.add(0f)
            fn.add(0f)
            fn.add(1f)
        }
    }

    private fun loadMtlFromRawResource(@RawRes resId: Int) {
        val reader = BufferedReader(
            InputStreamReader(
                mContext!!.resources.openRawResource(resId)
            )
        )

        reader.useLines { lines ->
            lines.forEach { line ->
                val p = line.trim().split("\\s+".toRegex())
                if (p.size < 2) return@forEach
                when (p[0]) {
                    "Ka" -> if (p.size >= 4) {
                        materialKa[0] = p[1].toFloat()
                        materialKa[1] = p[2].toFloat()
                        materialKa[2] = p[3].toFloat()
                    }

                    "Kd" -> if (p.size >= 4) {
                        materialKd[0] = p[1].toFloat()
                        materialKd[1] = p[2].toFloat()
                        materialKd[2] = p[3].toFloat()
                    }

                    "Ks" -> if (p.size >= 4) {
                        materialKs[0] = p[1].toFloat()
                        materialKs[1] = p[2].toFloat()
                        materialKs[2] = p[3].toFloat()
                    }

                    "Ns" -> if (p.size >= 2) {
                        materialNs = p[1].toFloat()
                    }
                }
            }
        }
    }
}