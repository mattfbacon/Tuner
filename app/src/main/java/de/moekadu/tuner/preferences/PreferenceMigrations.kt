/*
* Copyright 2024 Michael Moessner
*
* This file is part of Tuner.
*
* Tuner is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Tuner is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Tuner.  If not, see <http://www.gnu.org/licenses/>.
*/
package de.moekadu.tuner.preferences

import android.content.Context
import de.moekadu.tuner.instruments.InstrumentResources
import de.moekadu.tuner.instruments.InstrumentResourcesOld
import de.moekadu.tuner.temperaments.TemperamentResources
import kotlin.math.roundToInt

fun migrateFromV6(
    context: Context,
    newPreferenceResources: PreferenceResources,
    newTemperamentResources: TemperamentResources,
    newInstrumentResources: InstrumentResources
    ): Boolean {
//    Log.v("Tuner", "PreferenceMigrations: complete = ${migrationsFromV6Complete.value}")
    if (newPreferenceResources.migrationsFromV6Complete.value) {
//        Log.v("Tuner", "PreferenceMigrations: Do not migrate, since already done")
        return false
    }
//    Log.v("Tuner", "PreferenceMigrations: Migrating preferences from v6")
    val from = PreferenceResourcesOld(context)
    from.appearance?.let {
        newPreferenceResources.writeAppearance(
            PreferenceResources.Appearance(
                it.mode,
                it.blackNightEnabled,
                it.useSystemColorAccents
            )
        )
    }
    from.scientificMode?.let { newPreferenceResources.writeScientificMode(it) }
    from.screenAlwaysOn?.let { newPreferenceResources.writeScreenAlwaysOn(it) }
    from.notePrintOptions?.let { newPreferenceResources.writeNotePrintOptions(it) }

    from.windowing?.let { newPreferenceResources.writeWindowing(it) }
    from.overlap?.let { newPreferenceResources.writeOverlap(it) }
    from.windowSizeExponent?.let { newPreferenceResources.writeWindowSize(it) }
    from.pitchHistoryDuration?.let {
        newPreferenceResources
            .writePitchHistoryDuration(((it / 0.25f).roundToInt() * 0.25f).coerceIn(0.25f, 10f))
    }
    from.pitchHistoryNumFaultyValues?.let {
        newPreferenceResources.writePitchHistoryNumFaultyValues(it)
    }
    from.numMovingAverage?.let { newPreferenceResources.writeNumMovingAverage(it) }
    from.sensitivity?.let { newPreferenceResources.writeSensitivity(it) }
    from.toleranceInCents?.let { newPreferenceResources.writeToleranceInCents(it) }
    from.waveWriterDurationInSeconds?.let {
        newPreferenceResources.writeWaveWriterDurationInSeconds(it)
    }

    newTemperamentResources.writeMusicalScale(
        from.temperament,
        from.referenceNote,
        from.rootNote,
        from.referenceFrequency?.toFloatOrNull()
    )

    val fromInstruments = InstrumentResourcesOld(context)
    fromInstruments.customInstrumentsExpanded?.let {
        newInstrumentResources.writeCustomInstrumentsExpanded(it)
    }
    fromInstruments.predefinedInstrumentsExpanded?.let {
        newInstrumentResources.writePredefinedInstrumentsExpanded(it)
    }
    fromInstruments.customInstruments?.let {
        newInstrumentResources.writeCustomInstruments(it)
    }
    fromInstruments.currentInstrument?.let {
        newInstrumentResources.writeCurrentInstrument(it)
    }

    newPreferenceResources.writeMigrationsFromV6Complete()
    return true
}