package com.tatasky.binge.customviews

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Message
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.*


internal enum class AnimateState {
    asReady, asRunning, asPause
}

internal class Rocket(a: Int, b: Int, g: Int) {
    var sleep = true
    private var energy = 0f
    private var length = 0f
    private val mx: Float
    private val my: Float
    private val gravity: Float
    private var ox = 0f
    private var oy = 0f
    private var x = 0f
    private var y = 0f
    private var t = 0f
    private lateinit var vx: FloatArray
    private lateinit var vy: FloatArray
    private var patch = 0
    private var red = 0
    private var green = 0
    private var blue = 0
    private var random: Random? = null
    fun init(e: Int, p: Int, l: Int, seed: Long) {
        energy = e.toFloat()
        patch = p + 20
        length = l.toFloat()
        random = Random(seed)
        vx = FloatArray(patch)
        vy = FloatArray(patch)
        red = (random!!.nextFloat() * 128).toInt() + 128
        blue = (random!!.nextFloat() * 128).toInt() + 128
        green = (random!!.nextFloat() * 128).toInt() + 128
        ox = random!!.nextFloat() * mx / 2 + mx / 4
        oy = random!!.nextFloat() * my / 2 + my / 4
        for (i in 0 until patch) {
            vx[i] =
                (random!!.nextFloat() + random!!.nextFloat() / 2) * energy - energy / (random!!.nextInt(
                    2
                ) + 1)
            vy[i] =
                (random!!.nextFloat() + random!!.nextFloat() / 2) * energy * 7 / 8 - energy / (random!!.nextInt(
                    5
                ) + 4)
        }
    }

    fun start() {
        t = 0f
        sleep = false
    }

    fun doDraw(canvas: Canvas?, paint: Paint) {
        if (!sleep) {
            if (t < length) {
                var i: Int
                val cr: Int
                val cg: Int
                val cb: Int
                var s: Double
                cr = (random!!.nextDouble() * 64).toInt() - 32 + red
                cg = (random!!.nextDouble() * 64).toInt() - 32 + green
                cb = (random!!.nextDouble() * 64).toInt() - 32 + blue
                if (cr >= 0 && cr <= 256) red = cr
                if (cg >= 0 && cg <= 256) green = cg
                if (cb >= 0 && cb <= 256) blue = cb
                val _red = if (red == 256) 255 else red
                val _green = if (green == 256) 255 else green
                val _blue = if (blue == 256) 255 else blue
                val color = Color.rgb(_red, _green, _blue)
                paint.color = color
                i = 0
                while (i < patch) {
                    s = t.toDouble() / 100
                    x = (vx[i] * s).toFloat()
                    y = (vy[i] * s - gravity * s * s).toFloat()
                    canvas!!.drawCircle(ox + x, oy - y, 2f, paint)
                    ++i
                }
                paint.color = Color.BLACK
                i = 0
                while (i < patch) {
                    if (t >= length / 2) {
                        for (j in 0..1) {
                            s = ((t - length / 2) * 2 + j).toDouble() / 100
                            x = (vx[i] * s).toFloat()
                            y = (vy[i] * s - gravity * s * s).toFloat()
                            canvas!!.drawCircle(ox + x, oy - y, 2f, paint)
                        }
                    }
                    ++i
                }
                ++t
            } else sleep = true
        }
    }

    init {
        mx = a.toFloat()
        my = b.toFloat()
        gravity = g.toFloat()
    }
}

internal class Fireworks(private var width: Int, private var height: Int) {
    /**
     * Maximum number of rockets.
     */
    var MaxRocketNumber = 9

    /**
     * Controls "energy" of firwork explosion. Default value 850.
     */
    var MaxRocketExplosionEnergy = 950

    /**
     * Controls the density of the firework burst. Larger numbers give higher density.
     * Default value 90.
     */
    var MaxRocketPatchNumber = 90

    /**
     * Controls the radius of the firework burst. Larger numbers give larger radius.
     * Default value 68.
     */
    var MaxRocketPatchLength = 68

    /**
     * Controls gravity of the firework simulation.
     * Default value 400.
     */
    var Gravity = 400

    @Transient
    private lateinit var rocket: Array<Rocket?>

    @Transient
    private var rocketsCreated = false
    fun createRockets() {
        rocketsCreated = true
        val tempRocket = arrayOfNulls<Rocket>(MaxRocketNumber)
        for (i in 0 until MaxRocketNumber) tempRocket[i] = Rocket(width, height, Gravity)
        rocket = tempRocket
    }

    @Synchronized
    fun reshape(width: Int, height: Int) {
        this.width = width
        this.height = height
        rocketsCreated = false
    }

    fun doDraw(canvas: Canvas?, paint: Paint) {
        canvas!!.drawColor(Color.BLACK)
        var i: Int
        var e: Int
        var p: Int
        var l: Int
        var s: Long
        var sleep: Boolean
        if (!rocketsCreated) {
            createRockets()
        }
        if (rocketsCreated) {
            sleep = true
            i = 0
            while (i < MaxRocketNumber) {
                sleep = sleep && rocket[i]!!.sleep
                i++
            }
            i = 0
            while (i < MaxRocketNumber) {
                e =
                    (Math.random() * MaxRocketExplosionEnergy * 3 / 4).toInt() + MaxRocketExplosionEnergy / 4 + 1
                p =
                    (Math.random() * MaxRocketPatchNumber * 3 / 4).toInt() + MaxRocketPatchNumber / 4 + 1
                l =
                    (Math.random() * MaxRocketPatchLength * 3 / 4).toInt() + MaxRocketPatchLength / 4 + 1
                s = (Math.random() * 10000).toLong()
                val r = rocket[i]
                if (r!!.sleep && Math.random() * MaxRocketNumber * l < 2) {
                    r.init(e, p, l, s)
                    r.start()
                }
                if (rocketsCreated) r.doDraw(canvas, paint)
                ++i
            }
        }
    }
}

class FireworkLayout @SuppressLint("HandlerLeak") constructor(context: Context?) :
    SurfaceView(context), SurfaceHolder.Callback {
    internal inner class GameThread(
        private val surfaceHolder: SurfaceHolder,
        private val context: Context,
        private val handler: Handler
    ) :
        Thread() {
        private var mRun = false
        private var state: AnimateState? = null
        private val paint: Paint
        var fireworks: Fireworks
        fun doStart() {
            synchronized(surfaceHolder) { setState(AnimateState.asRunning) }
        }

        fun pause() {
            synchronized(surfaceHolder) { if (state == AnimateState.asRunning) setState(AnimateState.asPause) }
        }

        fun unpause() {
            setState(AnimateState.asRunning)
        }

        override fun run() {
            while (mRun) {
                var c: Canvas? = null
                try {
                    c = surfaceHolder.lockCanvas(null)
                    synchronized(surfaceHolder) { if (state == AnimateState.asRunning) doDraw(c) }
                } finally {
                    if (c != null) {
                        surfaceHolder.unlockCanvasAndPost(c)
                    }
                }
            }
        }

        fun setRunning(b: Boolean) {
            mRun = b
        }

        fun setState(state: AnimateState?) {
            synchronized(surfaceHolder) { this.state = state }
        }

        fun doDraw(canvas: Canvas?) {
            fireworks.doDraw(canvas, paint)
        }

        fun setSurfaceSize(width: Int, height: Int) {
            synchronized(surfaceHolder) { fireworks.reshape(width, height) }
        }

        init {
            fireworks = Fireworks(width, height)
            paint = Paint()
            paint.strokeWidth = 2 / resources.displayMetrics.density
            paint.color = Color.BLACK
            paint.isAntiAlias = true
        }
    }

    private val thread: GameThread
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        thread.setSurfaceSize(width, height)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        thread.setRunning(true)
        thread.doStart()
        thread.start()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        thread.setRunning(false)
        while (retry) {
            try {
                thread.join()
                retry = false
            } catch (e: InterruptedException) {
            }
        }
    }

    init {
        val holder = holder
        holder.addCallback(this)
        getHolder().addCallback(this)
        thread = GameThread(holder, context!!, object : Handler() {
            override fun handleMessage(m: Message) {}
        })
        isFocusable = true
    }
}