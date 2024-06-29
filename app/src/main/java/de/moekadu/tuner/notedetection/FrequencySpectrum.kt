package de.moekadu.tuner.notedetection

/** Result of data in frequency space.
 * @param size Number of different frequencies. This is normally "time_domain_samples + 1"
 * @param df Frequency resolution.
 */
class FrequencySpectrum(
    val size: Int,
    val df: Float,
) {
    /** Array with a frequency for each spectrum value. */
    val frequencies = FloatArray(size) { df * it }
    /** Spectrum, where 2*i is the real part and 2*i+1 is the imaginary part. */
    val spectrum = FloatArray(2 * size)
    /** Squared amplitudes of the spectrum (re*re + im*im) / (numberOfInputSamples * 2f)**2 .*/
    val amplitudeSpectrumSquared = FloatArray(size)

    /** Normalized spectrum for plotting. */
    val plottingSpectrumNormalized = FloatArray(size)

    /** Return the real part of a spectrum value.
     * @param index Index where the value is needed.
     * @return Real part of spectrum at given index.
     */
    fun real(index: Int): Float {
        return spectrum[2 * index]
    }
    /** Return the imaginary part of a spectrum value.
     * @param index Index where the value is needed.
     * @return Imaginary part of spectrum at given index.
     */
    fun imag(index: Int): Float {
        return spectrum[2 * index + 1]
    }
}