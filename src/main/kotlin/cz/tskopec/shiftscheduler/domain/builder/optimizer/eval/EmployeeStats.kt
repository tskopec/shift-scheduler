package cz.tskopec.shiftscheduler.domain.builder.optimizer.eval

import cz.tskopec.shiftscheduler.domain.entities.Employee
import cz.tskopec.shiftscheduler.domain.entities.ShiftType

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