package de.moekadu.tuner.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.moekadu.tuner.instruments.Instrument
import de.moekadu.tuner.instruments.InstrumentResources
import de.moekadu.tuner.instruments.InstrumentResources2
import de.moekadu.tuner.notedetection.FrequencyDetectionCollectedResults
import de.moekadu.tuner.notedetection.FrequencyEvaluationResult
import de.moekadu.tuner.notedetection.SortedAndDistinctInstrumentStrings
import de.moekadu.tuner.notedetection.TuningState
import de.moekadu.tuner.notedetection.checkTuning
import de.moekadu.tuner.preferences.PreferenceResources2
import de.moekadu.tuner.temperaments.MusicalNote
import de.moekadu.tuner.temperaments.MusicalScale
import de.moekadu.tuner.tuner.Tuner
import de.moekadu.tuner.ui.instruments.StringWithInfo
import de.moekadu.tuner.ui.instruments.StringsState
import de.moekadu.tuner.ui.notes.NotePrintOptions
import de.moekadu.tuner.ui.plot.GestureBasedViewPort
import de.moekadu.tuner.ui.screens.InstrumentTunerData
import de.moekadu.tuner.ui.tuning.PitchHistoryState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InstrumentTunerViewModel @Inject constructor (
    val pref: PreferenceResources2,
    val instruments: InstrumentResources2
) : ViewModel(), InstrumentTunerData {
    private val tuner = Tuner(
        pref,
        instruments.currentInstrument.value,
        viewModelScope,
        onResultAvailableListener = object : Tuner.OnResultAvailableListener {
            override fun onFrequencyDetected(result: FrequencyDetectionCollectedResults) { }

            override fun onFrequencyEvaluated(result: FrequencyEvaluationResult) {
                if (result.smoothedFrequency > 0f) {
                    pitchHistoryState.addFrequency(result.smoothedFrequency)
                    currentSmoothedFrequency = result.smoothedFrequency

                    result.target?.let { tuningTarget ->
                        handleTargetNoteOnAutodetectChange(tuningTarget.note)
                    }
                }
                //Log.v("Tuner", "ScientificTunerViewModel: dt = ${result.timeSinceThereIsNoFrequencyDetectionResult}")
                resetTuningState(result.timeSinceThereIsNoFrequencyDetectionResult)
            }
        }
    )

    private var autodetectedTargetNote = musicalScale.value.referenceNote
    private var currentSmoothedFrequency = musicalScale.value.referenceFrequency

    override val musicalScale: StateFlow<MusicalScale> get() = pref.musicalScale
    override val notePrintOptions: StateFlow<NotePrintOptions> get() = pref.notePrintOptions
    override val toleranceInCents: StateFlow<Int> get() = pref.toleranceInCents

    override var pitchHistoryState: PitchHistoryState = PitchHistoryState(
        computePitchHistorySize()
    )
        private set

    override val pitchHistoryGestureBasedViewPort: GestureBasedViewPort
            = GestureBasedViewPort()
    override var tuningState by mutableStateOf(TuningState.Unknown)
        private set

    override var targetNote by mutableStateOf(autodetectedTargetNote)
        private set
    override var targetNoteForLockButton: MusicalNote by mutableStateOf(autodetectedTargetNote)
        private set

    override var selectedNoteKey by mutableStateOf<Int?>(null)
        private set

    override val instrument: StateFlow<Instrument> get() = instruments.currentInstrument

    override var strings by mutableStateOf<ImmutableList<StringWithInfo>?>(
        instrument.value.strings.mapIndexed { index, note ->
            StringWithInfo(note, index) //, musicalScale.value.getNoteIndex(note))
        }.toImmutableList()
    )
        private set

    override val stringsState = StringsState(-musicalScale.value.noteIndexBegin)

    override fun onStringClicked(key: Int, note: MusicalNote) {
        selectedNoteKey = if (selectedNoteKey == key) null else key
        handleTargetNoteOnSelectionChange(selectedNoteKey)
        // leave tuningState unknown in case, this should only be changed by the
        // frequencyEvaluator callback
        resetTuningState(null)
    }

    override fun onClearFixedTargetClicked() {
        selectedNoteKey = null
        handleTargetNoteOnSelectionChange(selectedNoteKey)
        resetTuningState(null)
    }

    init {
        viewModelScope.launch {
            pref.pitchHistoryDuration.collect {
                pitchHistoryState.resize(computePitchHistorySize())
            }
        }
        viewModelScope.launch {
            pref.windowSize.collect {
                pitchHistoryState.resize(computePitchHistorySize())
            }
        }
        viewModelScope.launch {
            pref.overlap.collect {
                pitchHistoryState.resize(computePitchHistorySize())
            }
        }

        viewModelScope.launch {
            instruments.currentInstrument.collect {
                strings = it.strings.mapIndexed { index, note ->
                    StringWithInfo(note, index) //, musicalScale.value.getNoteIndex(note))
                }.toImmutableList()
                tuner.changeInstrument(it)
            }
        }
    }
    fun startTuner() {
        tuner.connect()
    }
    fun stopTuner() {
        tuner.disconnect()
    }

    private fun handleTargetNoteOnSelectionChange(
        selectedNoteKey: Int? = null,
    ) {
        this.selectedNoteKey = selectedNoteKey
        targetNote = if (selectedNoteKey == null) {
            autodetectedTargetNote
        } else if (instrument.value.isChromatic) {
            musicalScale.value.getNote(
                musicalScale.value.noteIndexBegin + selectedNoteKey
            )
        } else {
            strings?.find { it.key == selectedNoteKey }?.note ?: autodetectedTargetNote
        }

        if (selectedNoteKey != null) {
            targetNoteForLockButton = targetNote
        }
    }

    private fun handleTargetNoteOnAutodetectChange(
        autodetectedTargetNote: MusicalNote
    ) {
        this.autodetectedTargetNote = autodetectedTargetNote
        if (selectedNoteKey == null)
            targetNote = autodetectedTargetNote
    }

    /** Only provide the timeWithoutFreqDetectionResult if available */
    private fun resetTuningState(timeSinceThereIsNoFrequencyDetectionResult: Float? = null) {
        tuningState = if (timeSinceThereIsNoFrequencyDetectionResult == null
            && tuningState == TuningState.Unknown) {
            TuningState.Unknown
        } else if (timeSinceThereIsNoFrequencyDetectionResult != null
            && timeSinceThereIsNoFrequencyDetectionResult > DURATION_FOR_MARKING_NOTEDETECTION_AS_INACTIVE) {
            TuningState.Unknown
        } else {
            val noteIndex = musicalScale.value.getNoteIndex(targetNote)
            checkTuning(
                currentSmoothedFrequency,
                musicalScale.value.getNoteFrequency(noteIndex),
                toleranceInCents.value.toFloat()
            )
        }
    }

    /** Compute number of samples to be stored in pitch history. */
    private fun computePitchHistorySize() = PitchHistoryState.computePitchHistorySize(
        pref.pitchHistoryDuration.value,
        pref.sampleRate, pref.windowSize.value,
        pref.overlap.value
    )

    companion object {
        const val DURATION_FOR_MARKING_NOTEDETECTION_AS_INACTIVE = 0.5f // in seconds
    }

}