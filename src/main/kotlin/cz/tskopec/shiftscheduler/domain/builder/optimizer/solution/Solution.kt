package cz.tskopec.shiftscheduler.domain.builder.optimizer.solution

import cz.tskopec.shiftscheduler.domain.builder.optimizer.eval.EmployeeStats
import cz.tskopec.shiftscheduler.domain.entities.Employee
import cz.tskopec.shiftscheduler.domain.entities.ScheduleDay

// Solution which is gradually mutated by the Optimizer algorithm
class Solution(
	val plan: List<ScheduleDay>,
	val stats: List<EmployeeStats>
) {

	fun statsOf(employee: Employee) = stats[employee.index]

	fun applyMutation(mutation: Mutation): Solution {

		val updatedPlan = plan.toMutableList().apply {
			this[mutation.mutatedDay.index] = mutation.mutatedDay
		}
		val updatedStats = stats.toMutableList().apply {
			mutation.updatedStats.forEach {
				this[it.employee.index] = it
			}
		}
		return Solution(updatedPlan, updatedStats)
	}
}