package de.moekadu.tuner.notedetection2

class TimeSeries(val size: Int, val dt: Float) {
    var framePosition = 0
    val values = FloatArray(size)
    operator fun get(index: Int) = values[index]
}
