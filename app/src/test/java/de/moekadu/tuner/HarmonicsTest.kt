package de.moekadu.tuner

import de.moekadu.tuner.notedetection.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Test
import kotlin.math.floor
import kotlin.math.roundToInt

class HarmonicsTest {
    @Test
    fun testGlobalMaximumIndex() {
        val size = 100
        val values = FloatArray(size) {it - 0.5f * size}
        val maximumIndex1 = 20
        val maximumIndex2 = 70
        values[maximumIndex1] = size.toFloat() + 3
        values[maximumIndex2] = size.toFloat()
        // begin and end cannot be a global max
        values[0] = size.toFloat() + 10
        values[size - 1] = size.toFloat() + 10

        assertEquals(maximumIndex1, findGlobalMaximumIndex(3, 50, values))
        assertEquals(maximumIndex2, findGlobalMaximumIndex(40, 80, values))
        assertEquals(maximumIndex1, findGlobalMaximumIndex(0, size, values))
        assertEquals(-1, findGlobalMaximumIndex(0, 0, values))
        assertEquals(-1, findGlobalMaximumIndex(0, 1, values))
        assertEquals(-1, findGlobalMaximumIndex(2, 6, values))
    }

    @Test
    fun testLocalMaximumIndex() {
        val size = 30
        val values = FloatArray(size) {10f}
        values[5] = 11f
        values[20] = 20f

        assertEquals(5, findLocalMaximumIndex(values, 4.5f, 0.7f, 1.01f, 3))
        assertEquals(5, findLocalMaximumIndex(values, 5.5f, 0.7f, 1.01f, 3))
        assertEquals(-1, findLocalMaximumIndex(values, 5.5f, 0.7f, 1.2f, 3))
        assertEquals(-1, findLocalMaximumIndex(values, 5.5f, 0.4f, 1.01f, 3))
        assertEquals(20, findLocalMaximumIndex(values, 18.5f, 1.8f, 1.2f, 3))
        assertEquals(20, findLocalMaximumIndex(values, 18.5f, 8f, 1.2f, 8))
        assertEquals(-1, findLocalMaximumIndex(values, 10.5f, 5f, 1.2f, 8))
    }

    @Test
    fun sortHarmonics() {
        val size = 100
        val harmonics = Harmonics(size)
        harmonics.addHarmonic(5, 1f, 1, 1f)
        harmonics.addHarmonic(2, 13f, 1, 5f)
        harmonics.addHarmonic(8, 3f, 3, 2f)
        harmonics.addHarmonic(1, 3f, 8, 2f)

        harmonics.sort()
        assertEquals(1,harmonics[0].harmonicNumber)
        assertEquals(2,harmonics[1].harmonicNumber)
        assertEquals(5,harmonics[2].harmonicNumber)
        assertEquals(8,harmonics[3].harmonicNumber)
    }

    @Test
    fun findHarmonics() {
        val size = 1000
        val harmonics = Harmonics(size)
        val frequency = 10f
        val frequencyMin = 1f
        val frequencyMax = 90f
        val df = 0.1f
        val spectrum = FrequencySpectrum(size, df)
        val spectrumFrequencies = AccurateSpectrumPeakFrequency(spectrum, null) // we omit the other spec, so the resulting values will be "index * df"

        // define the third harmonic
        val thirdHarmonicFreq = 3 * frequency
        val globMaxIdx = (thirdHarmonicFreq / df).roundToInt()
        spectrum.amplitudeSpectrumSquared[globMaxIdx] = 100f

        findHarmonicsFromSpectrum(harmonics, frequency, frequencyMin, frequencyMax, spectrum, spectrumFrequencies)
        assertEquals(1, harmonics.size) // we find only the third harmonic
        assertEquals(3, harmonics[0].harmonicNumber)
        assertEquals(globMaxIdx, harmonics[0].spectrumIndex)
        assertEquals(100f, harmonics[0].spectrumAmplitudeSquared)
        assertEquals(thirdHarmonicFreq, harmonics[0].frequency, 1e-5f * thirdHarmonicFreq)

        // define the second harmonic
        val secondHarmonicFreq = 2 * frequency
        val secondHarmonicIdx = (secondHarmonicFreq / df).roundToInt()
        spectrum.amplitudeSpectrumSquared[secondHarmonicIdx] = 80f

        findHarmonicsFromSpectrum(harmonics, frequency, frequencyMin, frequencyMax, spectrum, spectrumFrequencies)
        assertEquals(2, harmonics.size) // we find the second and third harmonic
        assertEquals(2, harmonics[1].harmonicNumber)
        assertEquals(secondHarmonicIdx, harmonics[1].spectrumIndex)
        assertEquals(80f, harmonics[1].spectrumAmplitudeSquared)
        assertEquals(secondHarmonicFreq, harmonics[1].frequency, 1e-5f * secondHarmonicFreq)

        // define the first harmonic
        val firstHarmonicIdx = (frequency / df).roundToInt()
        spectrum.amplitudeSpectrumSquared[firstHarmonicIdx] = 80f

        findHarmonicsFromSpectrum(harmonics, frequency, frequencyMin, frequencyMax, spectrum, spectrumFrequencies)
        assertEquals(3, harmonics.size) // we find first,  second and third harmonic
        assertEquals(1, harmonics[2].harmonicNumber)
        // reset first harmonic
        spectrum.amplitudeSpectrumSquared[firstHarmonicIdx] = 0f

        // define the 5th harmonic
        val fifthHarmonicFreq = 5 * frequency
        val fifthHarmonicIdx = (fifthHarmonicFreq / df).roundToInt()
        spectrum.amplitudeSpectrumSquared[fifthHarmonicIdx] = 60f

        findHarmonicsFromSpectrum(harmonics, frequency, frequencyMin, frequencyMax, spectrum, spectrumFrequencies)
        assertEquals(3, harmonics.size) // we find second, third and fifrth harmonic
        assertEquals(5, harmonics[2].harmonicNumber)

        findHarmonicsFromSpectrum(harmonics, frequency, frequencyMin, frequencyMax, spectrum, spectrumFrequencies, maxNumFail = 1)
        assertEquals(2, harmonics.size) // we find only second and third harmonic since due to the gap between 3rd and 5th

        // set the 5th harmonic slightly off
        spectrum.amplitudeSpectrumSquared[fifthHarmonicIdx] = 0f
        val fifthHarmonicIdxSlightlyOff = fifthHarmonicIdx + floor(0.18f * frequency / df).toInt()
        spectrum.amplitudeSpectrumSquared[fifthHarmonicIdxSlightlyOff] = 60f
        assertNotEquals(fifthHarmonicIdx, fifthHarmonicIdxSlightlyOff) // just make sure that they are different and we really test something slightly off

        findHarmonicsFromSpectrum(harmonics, frequency, frequencyMin, frequencyMax, spectrum, spectrumFrequencies, harmonicTolerance = 0.2f)
        assertEquals(3, harmonics.size) // we find only the third harmonic
        assertEquals(5, harmonics[2].harmonicNumber)

        findHarmonicsFromSpectrum(harmonics, frequency, frequencyMin, frequencyMax, spectrum, spectrumFrequencies, harmonicTolerance = 0.1f)
        assertEquals(2, harmonics.size) // we find only the second and third harmonic

        // set the 5th harmonic slightly off in other direction
        spectrum.amplitudeSpectrumSquared[fifthHarmonicIdxSlightlyOff] = 0f
        val fifthHarmonicIdxSlightlyOff2 = fifthHarmonicIdx - floor(0.18f * frequency / df).toInt()
        spectrum.amplitudeSpectrumSquared[fifthHarmonicIdxSlightlyOff2] = 60f
        assertNotEquals(fifthHarmonicIdx, fifthHarmonicIdxSlightlyOff2) // just make sure that they are different and we really test something slightly off

        findHarmonicsFromSpectrum(harmonics, frequency, frequencyMin, frequencyMax, spectrum, spectrumFrequencies, harmonicTolerance = 0.2f)
        assertEquals(3, harmonics.size) // we find only the third harmonic
        assertEquals(5, harmonics[2].harmonicNumber)

        findHarmonicsFromSpectrum(harmonics, frequency, frequencyMin, frequencyMax, spectrum, spectrumFrequencies, harmonicTolerance = 0.1f)
        assertEquals(2, harmonics.size) // we find only the second and third harmonic
    }

    @Test
    fun findHarmonics2() {
        val size = 1000
        val harmonics = Harmonics(size)
        val frequency = 10f
        val frequencyMin = 1f
        val frequencyMax = 90f
        val df = 0.1f
        val spectrum = FrequencySpectrum(size, df)
        val spectrumFrequencies = AccurateSpectrumPeakFrequency(spectrum, null) // we omit the other spec, so the resulting values will be "index * df"

        // define the third harmonic
        val thirdHarmonicFreq = 3 * frequency
        val globMaxIdx = (thirdHarmonicFreq / df).roundToInt()
        spectrum.amplitudeSpectrumSquared[globMaxIdx] = 100f

        val initialHarmonic = Harmonic(
            3,
            thirdHarmonicFreq,
            globMaxIdx,
            spectrum.amplitudeSpectrumSquared[globMaxIdx]
        )


        harmonics.findHarmonicsFromSpectrum2(
            initialHarmonic,
            frequencyMin,
            frequencyMax,
            spectrum,
            findGlobalMaximumIndex(1, spectrum.size -1, spectrum.amplitudeSpectrumSquared),
            spectrumFrequencies
        )
        assertEquals(1, harmonics.size) // we find only the third harmonic
        assertEquals(3, harmonics[0].harmonicNumber)
        assertEquals(globMaxIdx, harmonics[0].spectrumIndex)
        assertEquals(100f, harmonics[0].spectrumAmplitudeSquared)
        assertEquals(thirdHarmonicFreq, harmonics[0].frequency, 1e-5f * thirdHarmonicFreq)

        // define the second harmonic
        val secondHarmonicFreq = 2 * frequency
        val secondHarmonicIdx = (secondHarmonicFreq / df).roundToInt()
        spectrum.amplitudeSpectrumSquared[secondHarmonicIdx] = 80f

        harmonics.findHarmonicsFromSpectrum2(
            initialHarmonic,
            frequencyMin,
            frequencyMax,
            spectrum,
            findGlobalMaximumIndex(1, spectrum.size -1, spectrum.amplitudeSpectrumSquared),
            spectrumFrequencies
        )
        assertEquals(2, harmonics.size) // we find the second and third harmonic
        assertEquals(2, harmonics[1].harmonicNumber)
        assertEquals(secondHarmonicIdx, harmonics[1].spectrumIndex)
        assertEquals(80f, harmonics[1].spectrumAmplitudeSquared)
        assertEquals(secondHarmonicFreq, harmonics[1].frequency, 1e-5f * secondHarmonicFreq)

        // define the first harmonic
        val firstHarmonicIdx = (frequency / df).roundToInt()
        spectrum.amplitudeSpectrumSquared[firstHarmonicIdx] = 80f

        harmonics.findHarmonicsFromSpectrum2(
            initialHarmonic,
            frequencyMin,
            frequencyMax,
            spectrum,
            findGlobalMaximumIndex(1, spectrum.size -1, spectrum.amplitudeSpectrumSquared),
            spectrumFrequencies
        )
        assertEquals(3, harmonics.size) // we find first,  second and third harmonic
        assertEquals(1, harmonics[2].harmonicNumber)
        // reset first harmonic
        spectrum.amplitudeSpectrumSquared[firstHarmonicIdx] = 0f

        // define the 5th harmonic
        val fifthHarmonicFreq = 5 * frequency
        val fifthHarmonicIdx = (fifthHarmonicFreq / df).roundToInt()
        spectrum.amplitudeSpectrumSquared[fifthHarmonicIdx] = 60f

        harmonics.findHarmonicsFromSpectrum2(
            initialHarmonic,
            frequencyMin,
            frequencyMax,
            spectrum,
            findGlobalMaximumIndex(1, spectrum.size -1, spectrum.amplitudeSpectrumSquared),
            spectrumFrequencies
        )
        assertEquals(3, harmonics.size) // we find second, third and fifth harmonic
        assertEquals(5, harmonics[2].harmonicNumber)

        harmonics.findHarmonicsFromSpectrum2(
            initialHarmonic,
            frequencyMin,
            frequencyMax,
            spectrum,
            findGlobalMaximumIndex(1, spectrum.size -1, spectrum.amplitudeSpectrumSquared),
            spectrumFrequencies,
            maxNumFail = 1
        )
        assertEquals(2, harmonics.size) // we find only second and third harmonic since due to the gap between 3rd and 5th

        // set the 5th harmonic slightly off
        spectrum.amplitudeSpectrumSquared[fifthHarmonicIdx] = 0f
        val fifthHarmonicIdxSlightlyOff = fifthHarmonicIdx + floor(0.18f * frequency / df).toInt()
        spectrum.amplitudeSpectrumSquared[fifthHarmonicIdxSlightlyOff] = 60f
        assertNotEquals(fifthHarmonicIdx, fifthHarmonicIdxSlightlyOff) // just make sure that they are different and we really test something slightly off

        harmonics.findHarmonicsFromSpectrum2(
            initialHarmonic,
            frequencyMin,
            frequencyMax,
            spectrum,
            findGlobalMaximumIndex(1, spectrum.size -1, spectrum.amplitudeSpectrumSquared),
            spectrumFrequencies,
            harmonicTolerance = 0.2f
        )
        assertEquals(3, harmonics.size) // we find only the third harmonic
        assertEquals(5, harmonics[2].harmonicNumber)

        harmonics.findHarmonicsFromSpectrum2(
            initialHarmonic,
            frequencyMin,
            frequencyMax,
            spectrum,
            findGlobalMaximumIndex(1, spectrum.size -1, spectrum.amplitudeSpectrumSquared),
            spectrumFrequencies,
            harmonicTolerance = 0.1f
        )
        assertEquals(2, harmonics.size) // we find only the second and third harmonic

        // set the 5th harmonic slightly off in other direction
        spectrum.amplitudeSpectrumSquared[fifthHarmonicIdxSlightlyOff] = 0f
        val fifthHarmonicIdxSlightlyOff2 = fifthHarmonicIdx - floor(0.18f * frequency / df).toInt()
        spectrum.amplitudeSpectrumSquared[fifthHarmonicIdxSlightlyOff2] = 60f
        assertNotEquals(fifthHarmonicIdx, fifthHarmonicIdxSlightlyOff2) // just make sure that they are different and we really test something slightly off

        harmonics.findHarmonicsFromSpectrum2(
            initialHarmonic,
            frequencyMin,
            frequencyMax,
            spectrum,
            findGlobalMaximumIndex(1, spectrum.size -1, spectrum.amplitudeSpectrumSquared),
            spectrumFrequencies,
            harmonicTolerance = 0.2f
        )
        assertEquals(3, harmonics.size) // we find only the third harmonic
        assertEquals(5, harmonics[2].harmonicNumber)

        harmonics.findHarmonicsFromSpectrum2(
            initialHarmonic,
            frequencyMin,
            frequencyMax,
            spectrum,
            findGlobalMaximumIndex(1, spectrum.size -1, spectrum.amplitudeSpectrumSquared),
            spectrumFrequencies,
            harmonicTolerance = 0.1f
        )
        assertEquals(2, harmonics.size) // we find only the second and third harmonic
    }


    @Test
    fun checkForCommonDivisorTest() {
        val size = 100
        val harmonics = Harmonics(size)
        harmonics.addHarmonic(2, 2f, 2, 1f)
        harmonics.addHarmonic(6, 8f, 6, 3f)
        harmonics.addHarmonic(8, 8f, 8, 2f)

        var hasCommonDivisor = harmonics.hasCommonDivisors()
        assert(hasCommonDivisor)

        harmonics.addHarmonic(9, 9f, 9, 2f)
        hasCommonDivisor = harmonics.hasCommonDivisors()
        assertFalse(hasCommonDivisor)

        harmonics.clear()
        harmonics.addHarmonic(9, 9f, 9, 2f)
        harmonics.addHarmonic(15, 15f, 15, 2f)
        hasCommonDivisor = harmonics.hasCommonDivisors()
        assert(hasCommonDivisor)

        harmonics.clear()
        harmonics.addHarmonic(19, 19f, 19, 2f)
        assert(hasCommonDivisor)

        harmonics.clear()
        harmonics.addHarmonic(1, 1f, 1, 2f)
        hasCommonDivisor = harmonics.hasCommonDivisors()
        assertFalse(hasCommonDivisor)
    }
}