package cz.tskopec.shiftscheduler.domain.builder.optimizer.swap

import cz.tskopec.shiftscheduler.domain.builder.optimizer.eval.EmployeeStats
import cz.tskopec.shiftscheduler.domain.builder.optimizer.solution.Mutation
import cz.tskopec.shiftscheduler.domain.entities.ScheduleDay

// contains state after swapping two employees from two different shifts. mutatedDay has updated staff maps,
// updatedStats holds the new shift distributions of affected employees, scoreDelta is difference in score from the original state
class SwapMutation(
	override val mutatedDay: ScheduleDay,
	override val updatedStats: List<EmployeeStats>,
	override val scoreDelta: Double
) : Mutation
