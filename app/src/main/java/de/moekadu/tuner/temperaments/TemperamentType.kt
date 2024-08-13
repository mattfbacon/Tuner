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
package de.moekadu.tuner.temperaments

import de.moekadu.tuner.R

enum class TemperamentType {
    EDO12,
    Pythagorean,
    Pure,
    QuarterCommaMeanTone,
    ExtendedQuarterCommaMeanTone,
    ThirdCommaMeanTone,
    FifthCommaMeanTone,
    WerckmeisterIII,
    WerckmeisterIV,
    WerckmeisterV,
    WerckmeisterVI,
    Kirnberger1,
    Kirnberger2,
    Kirnberger3,
    Neidhardt1,
    Neidhardt2,
    Neidhardt3,
    // Neidhardt4,
    Valotti,
    Young2,
    EDO17,
    EDO19,
    EDO22,
    EDO24,
    EDO27,
    EDO29,
    EDO31,
    EDO41,
    EDO53,
    Test
}

fun getTuningNameResourceId(temperamentType: TemperamentType) = when(temperamentType) {
    TemperamentType.EDO12 -> R.string.equal_temperament_12
    TemperamentType.Pythagorean -> R.string.pythagorean_tuning
    TemperamentType.Pure -> R.string.pure_tuning
    TemperamentType.QuarterCommaMeanTone -> R.string.quarter_comma_mean_tone
    TemperamentType.ExtendedQuarterCommaMeanTone -> R.string.extended_quarter_comma_mean_tone
    TemperamentType.ThirdCommaMeanTone -> R.string.third_comma_mean_tone
    TemperamentType.FifthCommaMeanTone -> R.string.fifth_comma_mean_tone
    TemperamentType.WerckmeisterIII -> R.string.werckmeister_iii
    TemperamentType.WerckmeisterIV -> R.string.werckmeister_iv
    TemperamentType.WerckmeisterV -> R.string.werckmeister_v
    TemperamentType.WerckmeisterVI -> R.string.werckmeister_vi
    TemperamentType.Kirnberger1 -> R.string.kirnberger1
    TemperamentType.Kirnberger2 -> R.string.kirnberger2
    TemperamentType.Kirnberger3 -> R.string.kirnberger3
    TemperamentType.Neidhardt1 -> R.string.neidhardt1
    TemperamentType.Neidhardt2 -> R.string.neidhardt2
    TemperamentType.Neidhardt3 -> R.string.neidhardt3
    TemperamentType.Valotti -> R.string.valotti
    TemperamentType.Young2 -> R.string.young2
    TemperamentType.EDO17 -> R.string.equal_temperament_17
    TemperamentType.EDO19 -> R.string.equal_temperament_19
    TemperamentType.EDO22 -> R.string.equal_temperament_22
    TemperamentType.EDO24 -> R.string.equal_temperament_24
    TemperamentType.EDO27 -> R.string.equal_temperament_27
    TemperamentType.EDO29 -> R.string.equal_temperament_29
    TemperamentType.EDO31 -> R.string.equal_temperament_31
    TemperamentType.EDO41 -> R.string.equal_temperament_41
    TemperamentType.EDO53 -> R.string.equal_temperament_53
    TemperamentType.Test -> R.string.test_tuning
}

fun getTuningDescriptionResourceId(temperamentType: TemperamentType) = when(temperamentType) {
    TemperamentType.EDO12 -> R.string.equal_temperament_12_desc
    TemperamentType.Pythagorean -> null
    TemperamentType.Pure -> R.string.pure_tuning_desc
    TemperamentType.QuarterCommaMeanTone -> R.string.quarter_comma_mean_tone_desc
    TemperamentType.ExtendedQuarterCommaMeanTone -> R.string.extended_quarter_comma_mean_tone_desc
    TemperamentType.ThirdCommaMeanTone -> R.string.third_comma_mean_tone_desc
    TemperamentType.FifthCommaMeanTone -> R.string.fifth_comma_mean_tone_desc
    TemperamentType.WerckmeisterIII -> R.string.werckmeister_iii_desc
    TemperamentType.WerckmeisterIV -> R.string.werckmeister_iv_desc
    TemperamentType.WerckmeisterV -> R.string.werckmeister_v_desc
    TemperamentType.WerckmeisterVI -> R.string.werckmeister_vi_desc
    TemperamentType.Kirnberger1 -> R.string.kirnberger1_desc
    TemperamentType.Kirnberger2 -> R.string.kirnberger2_desc
    TemperamentType.Kirnberger3 -> R.string.kirnberger3_desc
    TemperamentType.Neidhardt1 -> R.string.neidhardt1_desc
    TemperamentType.Neidhardt2 -> R.string.neidhardt2_desc
    TemperamentType.Neidhardt3 -> R.string.neidhardt3_desc
    TemperamentType.Valotti -> null
    TemperamentType.Young2 -> null
    TemperamentType.EDO17 -> R.string.equal_temperament_17_desc
    TemperamentType.EDO19 -> R.string.equal_temperament_19_desc
    TemperamentType.EDO22 -> R.string.equal_temperament_22_desc
    TemperamentType.EDO24 -> R.string.equal_temperament_24_desc
    TemperamentType.EDO27 -> R.string.equal_temperament_27_desc
    TemperamentType.EDO29 -> R.string.equal_temperament_29_desc
    TemperamentType.EDO31 -> R.string.equal_temperament_31_desc
    TemperamentType.EDO41 -> R.string.equal_temperament_41_desc
    TemperamentType.EDO53 -> R.string.equal_temperament_53_desc
    TemperamentType.Test -> null
}

fun getTuningNameAbbrResourceId(temperamentType: TemperamentType) = when(temperamentType) {
    TemperamentType.EDO12 -> R.string.equal_temperament_12_abbr
    TemperamentType.Pythagorean -> R.string.pythagorean_tuning_abbr
    TemperamentType.Pure -> R.string.pure_tuning_abbr
    TemperamentType.QuarterCommaMeanTone -> R.string.quarter_comma_mean_tone_abbr
    TemperamentType.ExtendedQuarterCommaMeanTone -> R.string.extended_quarter_comma_mean_tone_abbr
    TemperamentType.ThirdCommaMeanTone -> R.string.third_comma_mean_tone_abbr
    TemperamentType.FifthCommaMeanTone -> R.string.fifth_comma_mean_tone_abbr
    TemperamentType.WerckmeisterIII -> R.string.werckmeister_iii_abbr
    TemperamentType.WerckmeisterIV -> R.string.werckmeister_iv_abbr
    TemperamentType.WerckmeisterV -> R.string.werckmeister_v_abbr
    TemperamentType.WerckmeisterVI -> R.string.werckmeister_vi_abbr
    TemperamentType.Kirnberger1 -> R.string.kirnberger1_abbr
    TemperamentType.Kirnberger2 -> R.string.kirnberger2_abbr
    TemperamentType.Kirnberger3 -> R.string.kirnberger3_abbr
    TemperamentType.Neidhardt1 -> R.string.neidhardt1_abbr
    TemperamentType.Neidhardt2 -> R.string.neidhardt2_abbr
    TemperamentType.Neidhardt3 -> R.string.neidhardt3_abbr
    TemperamentType.Valotti -> R.string.valotti_abbr
    TemperamentType.Young2 -> R.string.young2_abbr
    TemperamentType.EDO17 -> R.string.equal_temperament_17_abbr
    TemperamentType.EDO19 -> R.string.equal_temperament_19_abbr
    TemperamentType.EDO22 -> R.string.equal_temperament_22_abbr
    TemperamentType.EDO24 -> R.string.equal_temperament_24_abbr
    TemperamentType.EDO27 -> R.string.equal_temperament_27_abbr
    TemperamentType.EDO29 -> R.string.equal_temperament_29_abbr
    TemperamentType.EDO31 -> R.string.equal_temperament_31_abbr
    TemperamentType.EDO41 -> R.string.equal_temperament_41_abbr
    TemperamentType.EDO53 -> R.string.equal_temperament_53_abbr
    TemperamentType.Test -> R.string.test_tuning_abbr
}
