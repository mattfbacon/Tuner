package de.moekadu.tuner.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import de.moekadu.tuner.misc.StringOrResId
import de.moekadu.tuner.temperaments.BaseNote
import de.moekadu.tuner.temperaments.MusicalNote
import de.moekadu.tuner.temperaments.NoteModifier
import de.moekadu.tuner.temperaments.RationalNumber
import de.moekadu.tuner.temperaments2.NoteNames
import de.moekadu.tuner.temperaments2.Temperament
import de.moekadu.tuner.temperaments2.TemperamentResources
import de.moekadu.tuner.temperaments2.TemperamentValidityChecks
import de.moekadu.tuner.temperaments2.TemperamentWithNoteNames
import de.moekadu.tuner.temperaments2.getSuitableNoteNames
import de.moekadu.tuner.ui.screens.TemperamentEditorState
import de.moekadu.tuner.ui.temperaments.TemperamentTableLineState
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.toPersistentList

/** Create the table of temperament values and note names, based on a temperament.
 * @param temperament Temperament, based on which the table will be created.
 * @return List with fits the given temperament.
 */
private fun temperamentToTable(temperament: TemperamentWithNoteNames)
        : PersistentList<TemperamentTableLineState>
{
    val numberOfNotesPerOctave = temperament.temperament.numberOfNotesPerOctave
    val noteNames = temperament.noteNames ?: getSuitableNoteNames(numberOfNotesPerOctave)
    return temperament.temperament.cents.mapIndexed { index, cent ->
        val isOctaveLine = index == numberOfNotesPerOctave
        val octave = noteNames?.defaultReferenceNote?.octave ?: 4
        TemperamentTableLineState(
            note = if (isOctaveLine)
                noteNames?.getOrNull(0)?.copy(octave = octave + 1)
            else
                noteNames?.getOrNull(index)?.copy(octave = octave),
            cent = cent,
            ratio = temperament.temperament.rationalNumbers?.get(index),
            isFirstLine = index == 0,
            isOctaveLine = isOctaveLine,
            decreasingValueError = if (index == 0)
                false
            else
                cent <= temperament.temperament.cents[index-1],
            duplicateNoteError = false
        )
    }.toPersistentList()
}

/** Check cent/ratio values for correctness
 * @warning This will (un)set the decreasingValueError within the input list.
 * @param values List of temperament values. The function will set the decreasingValueError
 *   within this list.
 * @return Summary about if there were errors or not.
 */
private fun checkAndSetValueOrderingErrors(values: PersistentList<TemperamentTableLineState>)
: TemperamentValidityChecks.ValueOrdering {
    return TemperamentValidityChecks.checkValueOrderingErrors(
        values.size,
        { values[it].cent },
        { i, e -> values[i].changeDecreasingValueError(e) }
    )
}

private fun checkAndSetNoteNameErrors(values: PersistentList<TemperamentTableLineState>): TemperamentValidityChecks.NoteNameError {
    return TemperamentValidityChecks.checkNoteNameErrors(
        values.size, {values[it].note}, { i, e ->values[i].changeDuplicateNoteError(e) }
    )
}

@HiltViewModel (assistedFactory = TemperamentEditorViewModel.Factory::class)
class TemperamentEditorViewModel @AssistedInject constructor(
    @Assisted temperament: TemperamentWithNoteNames,
    val pref: TemperamentResources
) : ViewModel(), TemperamentEditorState {
    @AssistedFactory
    interface Factory {
        fun create(temperament: TemperamentWithNoteNames): TemperamentEditorViewModel
    }

    private val stableId = temperament.stableId

    private var useDefaultNoteNames = (temperament.noteNames == null) // TODO: add condition, that it exists!
    private val _name = mutableStateOf(temperament.temperament.name)
    override val name: State<StringOrResId> get() = _name

    private val _abbreviation = mutableStateOf(temperament.temperament.abbreviation)
    override val abbreviation: State<StringOrResId> get() = _abbreviation

    private val _description = mutableStateOf(temperament.temperament.description)
    override val description: State<StringOrResId> get() = _description

    private val _numberOfValues = mutableIntStateOf(temperament.temperament.numberOfNotesPerOctave)
    override val numberOfValues: State<Int> get() = _numberOfValues

    private val _temperamentValues = mutableStateOf(temperamentToTable(temperament))
    override val temperamentValues: State<PersistentList<TemperamentTableLineState>>
        get() = _temperamentValues

    // Warning: The checkAndSetValueOrderingFunction also sets the increasingValueInfo
    //  within the temperamentValues.
    private var valueOrderingError = checkAndSetValueOrderingErrors(temperamentValues.value)
    private var noteNameError = checkAndSetNoteNameErrors(temperamentValues.value)
    private var _hasErrors = mutableStateOf(
        valueOrderingError != TemperamentValidityChecks.ValueOrdering.Increasing
                || noteNameError != TemperamentValidityChecks.NoteNameError.None
    )
    override val hasErrors: State<Boolean>
        get() = _hasErrors

    override fun onCentValueChanged(index: Int, value: String) {
        temperamentValues.value.getOrNull(index)?.changeCentOrRatio(value)
        valueOrderingError = checkAndSetValueOrderingErrors(temperamentValues.value)
//        Log.v("Tuner", "TemperamentEditorViewModel.modifyCentOrRatioValue: valueOrdering=$valueOrdering")
        checkAndSetError()
    }

    override fun onNoteNameClicked(index: Int, enharmonic: Boolean) {
        temperamentValues.value.forEachIndexed { i, value ->
            value.setNoteEditor(
                if (i != index
                    || (!enharmonic && value.noteEditorState == TemperamentTableLineState.NoteEditorState.Standard)
                    || (enharmonic && value.noteEditorState == TemperamentTableLineState.NoteEditorState.Enharmonic)
                ) {
                    TemperamentTableLineState.NoteEditorState.Off
                } else if (enharmonic) {
                    TemperamentTableLineState.NoteEditorState.Enharmonic
                } else {
                    TemperamentTableLineState.NoteEditorState.Standard
                }
            )
        }
    }

    override fun onCloseNoteEditorClicked(index: Int) {
        temperamentValues.value.getOrNull(index)?.setNoteEditor(
            TemperamentTableLineState.NoteEditorState.Off
        )
    }

    private fun checkAndSetError() {
        _hasErrors.value = (
                valueOrderingError != TemperamentValidityChecks.ValueOrdering.Increasing
                        || noteNameError != TemperamentValidityChecks.NoteNameError.None
                )
    }

    fun changeNumberOfValues(numberOfValues: Int) {
        val oldTemperamentValues  = temperamentValues.value
        val oldNumValues = oldTemperamentValues.size - 1 // num notes per octave + octave note
        _temperamentValues.value = oldTemperamentValues.mutate { mutated ->
            val octaveLine = oldTemperamentValues.last()
            if (numberOfValues < oldNumValues) {
                mutated.subList(numberOfValues + 1, oldNumValues + 1).clear()
                mutated[numberOfValues] = octaveLine
            } else {
                val defaultNoteNames = if (useDefaultNoteNames)
                    getSuitableNoteNames(numberOfValues)
                else
                    null
                for (i in oldNumValues until numberOfValues) {
                    mutated.add(i, TemperamentTableLineState(
                        note = defaultNoteNames?.getOrNull(i),
                        cent = null,
                        ratio = null,
                        isFirstLine = false,
                        isOctaveLine = false,
                        decreasingValueError = false,
                        duplicateNoteError = false
                    ))
                }
                mutated[numberOfValues] = octaveLine
            }
        }
        _numberOfValues.intValue = numberOfValues

        valueOrderingError = checkAndSetValueOrderingErrors(temperamentValues.value)
        noteNameError = checkAndSetNoteNameErrors(temperamentValues.value)
        checkAndSetError()
    }

    override fun modifyName(value: String) {
        _name.value = StringOrResId(value)
    }

    override fun modifyAbbreviation(value: String) {
        _abbreviation.value = StringOrResId(value)
    }

    override fun modifyDescription(value: String) {
        _description.value = StringOrResId(value)
    }

    override fun modifyNote(index: Int, note: MusicalNote) { // }, isReferenceNote: Boolean) {
//        Log.v("Tuner", "TemperamentEditorViewModel.modifyNote: index = $index, note = $note")
        useDefaultNoteNames = false
        temperamentValues.value.getOrNull(index)?.let {
            it.note = note
            // it.isReferenceNote = isReferenceNote
        }
        // also set octave value, if first value ist set
        if (index == 0) {
            temperamentValues.value.lastOrNull()?.let {
                it.note = note.copy(octave = (it.note?.octave ?: 4) + 1)
            }
        }
        noteNameError = checkAndSetNoteNameErrors(temperamentValues.value)
        checkAndSetError()
    }

    fun saveTemperament(): Boolean {
        val values = temperamentValues.value
        val noteNameList = values.map {
            it.note ?: return false
        }
        val predefinedNoteNames = getSuitableNoteNames(values.size - 1)

        var useDefaultNoteNames = true
        if (predefinedNoteNames == null) {
            useDefaultNoteNames = false
        } else {
            for (i in 0 until values.size - 1) {
                if (!MusicalNote.notesEqualIgnoreOctave(predefinedNoteNames[i], noteNameList[i])) {
                    useDefaultNoteNames = false
                    break
                }
            }
        }

        val defaultReferenceNote = values.firstOrNull {
            (it.note?.base == BaseNote.A && it.note?.modifier == NoteModifier.None)
                    || (it.note?.enharmonicBase == BaseNote.A && it.note?.enharmonicModifier == NoteModifier.None)
        }?.note ?: values.firstOrNull{ it.note != null }?.note ?: return false

        val noteNames = if (useDefaultNoteNames) {
            predefinedNoteNames
        } else {
            NoteNames(
                notes = noteNameList.toTypedArray(),
                defaultReferenceNote =  defaultReferenceNote
            )
        }

        val possibleRatios = values.map { it.obtainRatio() }
        val hasRatios = !possibleRatios.contains(null)
        val ratios = if (hasRatios) {
            possibleRatios.map { it ?: RationalNumber(1,1) }
        } else {
            null
        }
        // TODO: we must ensure that we have valid number, otherwise saveTemperament is not allowed to be called
        val cents = ratios?.map { it.toDouble() }
            ?: values.map { it.obtainCent() ?: 0.0 }

        var hasEqualDivisions = true
        for (i in 0 until cents.size - 2)
            if (2 * cents[i+1] - cents[i] - cents[i+2] > 0.01)
                hasEqualDivisions = false

        val temperament = Temperament(
            name = name.value,
            abbreviation = abbreviation.value,
            description = description.value,
            cents = cents.toDoubleArray(),
            rationalNumbers = ratios?.toTypedArray(),
            circleOfFifths = null,
            equalOctaveDivision = if (hasEqualDivisions) cents.size - 1 else null,
            stableId = stableId
        )

        pref.addNewOrReplaceTemperament(
            TemperamentWithNoteNames(temperament, noteNames)
        )
        return true
    }

}
