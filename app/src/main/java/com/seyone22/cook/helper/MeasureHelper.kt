package com.seyone22.cook.helper

import android.content.Context
import com.seyone22.cook.data.model.Measure
import com.seyone22.cook.data.model.MeasureType
import com.seyone22.cook.data.repository.measure.MeasureRepository
import com.seyone22.cook.data.repository.measureConversion.MeasureConversionRepository
import kotlinx.coroutines.flow.first

class MeasureHelper(private val context: Context) {
    suspend fun convertToBase(
        value: Double,
        fromMeasure: Measure,
        conversionRepository: MeasureConversionRepository,
        measuresRepository: MeasureRepository
    ): Double {
        when (enumValueOf<MeasureType>(fromMeasure.type)) {
            MeasureType.WEIGHT -> {
                val base = measuresRepository.getMeasureByName("gram").first()

                if (base != null) {
                    val rate = conversionRepository.getRateFor(fromMeasure.id, base.id)
                    if (rate != null) {
                        return value * rate
                    }
                }
            }

            MeasureType.VOLUME -> {
                val base = measuresRepository.getMeasureByName("liter").first()

                if (base != null) {
                    val rate = conversionRepository.getRateFor(fromMeasure.id, base.id)
                    if (rate != null) {
                        return value * rate
                    }
                }
            }

            MeasureType.COUNT -> TODO()
            MeasureType.LENGTH -> TODO()
            MeasureType.TEMPERATURE -> TODO()
            MeasureType.TIME -> TODO()
        }
        return -1.0
    }
}