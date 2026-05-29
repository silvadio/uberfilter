package com.driveq.domain

import com.driveq.model.EvaluationColor
import com.driveq.model.RideRecord
import com.driveq.model.RideStats
import kotlin.math.roundToInt

object RideStatsCalculator {

    fun compute(records: List<RideRecord>): RideStats {
        val redCount   = records.count { it.color == EvaluationColor.RED }
        val yellowCount = records.count { it.color == EvaluationColor.YELLOW }
        val greenCount  = records.count { it.color == EvaluationColor.GREEN }
        val total       = records.size

        return RideStats(
            redCount   = redCount,
            yellowCount = yellowCount,
            greenCount  = greenCount,
            redPct      = if (total > 0) (redCount.toDouble() / total * 100).roundToInt() else 0,
            yellowPct   = if (total > 0) (yellowCount.toDouble() / total * 100).roundToInt() else 0,
            greenPct    = if (total > 0) (greenCount.toDouble() / total * 100).roundToInt() else 0
        )
    }
}
