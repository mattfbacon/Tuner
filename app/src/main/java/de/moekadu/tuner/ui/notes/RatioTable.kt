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
package de.moekadu.tuner.ui.notes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.moekadu.tuner.misc.StringOrResId
import de.moekadu.tuner.temperaments.RationalNumber
import de.moekadu.tuner.temperaments.NoteNames
import de.moekadu.tuner.temperaments.Temperament
import de.moekadu.tuner.temperaments.getSuitableNoteNames
import de.moekadu.tuner.ui.theme.TunerTheme

/** Table showing cents between musical scale notes.
 * @param temperament Temperament.
 * @param noteNames Note names to print.
 * @param rootNoteIndex Index of root (first) note within the note names.
 * @param notePrintOptions How to print the notes.
 * @param modifier Modifier.
 */
@Composable
fun RatioTable(
    temperament: Temperament,
    noteNames: NoteNames,
    rootNoteIndex: Int,
    notePrintOptions: NotePrintOptions,
    modifier: Modifier = Modifier) {

    LazyRow(modifier = modifier) {
        items(temperament.numberOfNotesPerOctave + 1) {
            val note = noteNames[(rootNoteIndex + it) % noteNames.size]

            Column(
                modifier = Modifier.width(IntrinsicSize.Max),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.height(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Note(
                        note,
                        notePrintOptions = notePrintOptions,
                        withOctave = false,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                val number = temperament.rationalNumbers?.get(it)

                Fraction(
                    number?.numerator,
                    number?.denominator,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 200)
@Composable
private fun RatioTablePreview() {
    TunerTheme {
        val notePrintOptions = remember {
            NotePrintOptions(
                helmholtzNotation = false,
                notationType = NotationType.Standard
            )
        }

        val temperament = remember {
            Temperament.create(
                StringOrResId("Test 1"),
                StringOrResId("A 1"),
                StringOrResId("Description 1"),
                (0..12).map { RationalNumber(it + 12, 12) }.toTypedArray(),
                1
            )
        }

        val noteNames = remember {
            getSuitableNoteNames(temperament.numberOfNotesPerOctave)!!
        }
        RatioTable(
            temperament,
            noteNames,
            9,
            notePrintOptions = notePrintOptions
        )
    }
}