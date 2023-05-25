package cz.tskopec.shiftscheduler.domain.builder.optimizer.eval

import cz.tskopec.shiftscheduler.domain.Scheduler
import kotlin.math.abs

object Evaluator {

	// Aspects which are taken into account during evaluating the similarity of a given shift distribution and its target
	enum class Aspect {

		// total number of assigned shifts
		SHIFTS_QUANTITY {
			override fun computeScore(distribution: ShiftDistribution, target: DistributionTarget): Double =
				abs(distribution.nShifts + distribution.nVacationDays - target.shiftsPerEmployee).toDouble() / Scheduler.scheduleLength
		},
		// ratios between different shift types
		SHIFT_TYPE_RATIOS {
			override fun computeScore(distribution: ShiftDistribution, target: DistributionTarget): Double =
				evaluateDistribution(distribution.nShifts, distribution.byType, target.typeRatios)
		},
		// ratios between numbers of shifts assigned in different parts of the month
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