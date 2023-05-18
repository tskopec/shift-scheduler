package cz.tskopec.shiftscheduler.domain.builder.optimizer.eval

import cz.tskopec.shiftscheduler.domain.Scheduler
import kotlin.math.abs

object Evaluator {

	enum class Aspect {

		SHIFTS_QUANTITY {
			override fun computeScore(distribution: ShiftDistribution, target: DistributionTarget): Double =
				abs(distribution.nShifts + distribution.nVacationDays - target.shiftsPerEmployee).toDouble() / Scheduler.scheduleLength
		},
		SHIFT_TYPE_RATIOS {
			override fun computeScore(distribution: ShiftDistribution, target: DistributionTarget): Double =
				evaluateDistribution(distribution.nShifts, distribution.byType, target.typeRatios)
		},
		PERIOD_RATIOS {
			override fun computeScore(distribution: ShiftDistribution, target: DistributionTarget): Double =
				evaluateDistribution(distribution.nShifts, distribution.byPeriod, target.periodRatios)
		};

		// best =  0.0, worst = 1.0
		abstract fun computeScore(distribution: ShiftDistribution, target: DistributionTarget): Double

		protected fun evaluateDistribution(shiftsSum: Int, shiftCounts: IntArray, targetRatios: DoubleArray): Double {

			fun sumDifferences(a: DoubleArray, b: DoubleArray): Double = a.zip(b).sumOf { abs(it.first - it.second) }

			val evaluatedRatios = DoubleArray(shiftCounts.size) { shiftCounts[it].toDouble() / shiftsSum }
			val worstDiff = 2.0 - 2 * targetRatios.min()
			return sumDifferences(evaluatedRatios, targetRatios) / worstDiff
		}
	}


	fun getTotalScore(distribution: ShiftDistribution, target: DistributionTarget): Double =
		Aspect.values().sumOf { it.computeScore(distribution, target) }

}