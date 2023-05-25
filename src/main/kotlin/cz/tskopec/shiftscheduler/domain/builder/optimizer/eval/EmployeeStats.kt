package cz.tskopec.shiftscheduler.domain.builder.optimizer.eval

import cz.tskopec.shiftscheduler.domain.entities.Employee
import cz.tskopec.shiftscheduler.domain.entities.ShiftType
/*
Contains shift distribution of a single employee, the target distribution towards which the Optimizer aims, and a score
which represents their similarity - 0.0 meaning they are the same and 3.0 (1.0 * number of evaluated aspects) meaning they
are as far apart as possible.
 */
class EmployeeStats(
	val employee: Employee,
	private val distribution: ShiftDistribution,
	private val target: DistributionTarget
) {

	val score: Double = Evaluator.getTotalScore(distribution, target)

	fun update(dayIndex: Int, increment: ShiftType, decrement: ShiftType): EmployeeStats {

		return EmployeeStats(employee, distribution.update(dayIndex, increment, decrement), target)
	}
}