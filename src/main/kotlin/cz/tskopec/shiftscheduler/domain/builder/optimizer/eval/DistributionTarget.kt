package cz.tskopec.shiftscheduler.domain.builder.optimizer.eval

import cz.tskopec.shiftscheduler.domain.builder.optimizer.eval.ShiftDistribution.Companion.N_PERIODS
import cz.tskopec.shiftscheduler.domain.entities.ShiftType
import kotlin.math.roundToInt

// describes the ideal distribution of shifts towards which the employee's actual distribution is optimized
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