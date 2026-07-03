import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

// A small Spring utility inspired by Remotion's spring physics.
// Usage: call Spring.valueAtFrame(...) each frame (frame index starting at 0).
object Spring {
    data class Config(
        val stiffness: Double = 100.0, // k
        val damping: Double = 10.0, // c
        val mass: Double = 1.0, // m
    )

    enum class Preset(
        val cfg: Config,
    ) {
        GENTLE(Config(stiffness = 120.0, damping = 14.0, mass = 1.0)),
        WOBBLY(Config(stiffness = 180.0, damping = 12.0, mass = 1.0)),
        STIFF(Config(stiffness = 300.0, damping = 30.0, mass = 1.0)),
        SLOW(Config(stiffness = 70.0, damping = 10.0, mass = 1.0)),
    }

    /**
     * Compute the spring animated value at a given frame.
     *
     * @param frame Zero-based frame number.
     * @param fps Frames per second.
     * @param from Start value.
     * @param to End value.
     * @param config Physics config (stiffness k, damping c, mass m).
     * @param initialVelocity Initial velocity in units-per-second (same units as value).
     *
     * Returns interpolated value at time t = frame / fps.
     */
    fun valueAtFrame(
        frame: Int,
        fps: Double = 30.0,
        from: Double = 0.0,
        to: Double = 1.0,
        config: Config = Config(),
        initialVelocity: Double = 0.0,
    ): Double {
        if (from == to) return from
        val t = frame.coerceAtLeast(0) / fps
        val k = config.stiffness
        val c = config.damping
        val m = config.mass

        // natural frequency
        val omega0 = sqrt(k / m)
        // damping ratio
        val zeta = c / (2.0 * sqrt(k * m))

        // normalize so final target = 1; we'll map back to [from,to]
        val delta = to - from
        // normalized initial velocity (units of normalized value per second)
        val v0 = initialVelocity / delta

        val xNormalized =
            when {
                zeta < 1.0 -> {
                    // underdamped
                    val omegaD = omega0 * sqrt(1.0 - zeta * zeta)
                    // avoid division by zero if very small
                    if (omegaD.isFinite() && omegaD > 1e-12) {
                        val expTerm = exp(-zeta * omega0 * t)
                        val cosTerm = cos(omegaD * t)
                        val sinTerm = sin(omegaD * t)
                        1.0 - (expTerm / omegaD) * ((v0 + zeta * omega0) * sinTerm + omegaD * cosTerm)
                    } else {
                        // fallback to simple exponential
                        1.0 - exp(-omega0 * t)
                    }
                }

                zeta == 1.0 -> {
                    // critically damped: double root at -omega0
                    val expTerm = exp(-omega0 * t)
                    // formula for unit-step response with x(0)=0, x'(0)=v0:
                    1.0 - expTerm * (1.0 + (v0 + omega0) * t)
                }

                else -> {
                    // overdamped
                    val sqrtTerm = sqrt(zeta * zeta - 1.0)
                    val r1 = -omega0 * (zeta - sqrtTerm)
                    val r2 = -omega0 * (zeta + sqrtTerm)
                    // solve for coefficients A and B for homogeneous solution that satisfies initial conditions
                    // unit-step response => steady-state = 1
                    // x(t) = 1 + A*exp(r1*t) + B*exp(r2*t)
                    // initial:
                    // x(0) = 0 => 1 + A + B = 0 => A + B = -1
                    // x'(0) = v0 => A*r1 + B*r2 = v0
                    val denom = (r1 - r2)
                    if (abs(denom) < 1e-12) {
                        // numerically degenerate; fallback
                        1.0 - exp(-omega0 * t)
                    } else {
                        val a = (v0 - r2 * (-1.0)) / (r1 - r2) // solving linear system
                        val b = -1.0 - a
                        1.0 + a * exp(r1 * t) + b * exp(r2 * t)
                    }
                }
            }

        // map normalized [0..1] back to [from..to]
        return from + delta * xNormalized
    }

    /**
     * Generate values per frame for a maximum number of frames or until settled.
     *
     * @param maxFrames Maximum frames to generate.
     * @param settleThreshold Consider settled if abs(value - to) < settleThreshold.
     * @param velocityThreshold optional - not currently computed; uses value threshold only.
     */
    fun generateFrames(
        fps: Double = 30.0,
        from: Double = 0.0,
        to: Double = 1.0,
        config: Config = Config(),
        initialVelocity: Double = 0.0,
        maxFrames: Int = 300,
        settleThreshold: Double = 1e-3,
    ): List<Double> {
        val out = ArrayList<Double>(min(maxFrames, 1000))
        for (frame in 0 until maxFrames) {
            val v = valueAtFrame(frame, fps, from, to, config, initialVelocity)
            out += v
            if (abs(v - to) <= settleThreshold && frame > 3) break
        }
        return out
    }
}
