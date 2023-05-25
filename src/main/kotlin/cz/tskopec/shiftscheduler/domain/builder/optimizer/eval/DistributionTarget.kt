package cz.tskopec.shiftscheduler.domain.builder.optimizer.eval

import cz.tskopec.shiftscheduler.domain.builder.optimizer.eval.ShiftDistribution.Companion.N_PERIODS
import cz.tskopec.shiftscheduler.domain.entities.ShiftType
import kotlin.math.roundToInt
/*
Describes the ideal distribution of shifts of a single employee, towards which the employee's actual distribution is optimized.
Total number of shifts, ratios between different shift types and ratios between number of shifts assigned in different parts
of the month are taken into account.
 */
class DistributionTarget(
	val shiftsPerEmployee: Int,
	val typeRatios: DoubleArray,
	val periodRatios: DoubleArray
) {

	companion object {

		fun averageDistributions(distributions: List<ShiftDistribution>): List<DistributionTarget> {

			val target = averageOf(distributions)
			return List(distributions.size){ target }
		}


		// Returns distribution target with parameters which are computed by averaging specified shift distributions.
		private fun averageOf(distributions: List<ShiftDistribution>): DistributionTarget {

			val shiftsSum = distributions.sumOf { it.nShifts }
			val shiftsPerEmp = (shiftsSum.toDouble() / distributions.size).roundToInt()

			fun computeTypeRatios(): DoubleArray =
				ShiftType.workShiftTypes.indices.map { i ->
					distributions.sumOf { it.byType[i] }.toDouble() / shiftsSum
				}.toDoubleArray()

			fun computePeriodRatios(): DoubleArray =
				(0 until N_PERIODS).map { periodIndex ->
					distributions.sumOf { it.byPeriod[periodIndex] }.toDouble() / shiftsSum
				}.toDoubleArray()

			return DistributionTarget(shiftsPerEmp, computeTypeRatios(), computePeriodRatios())
		}
	}
}