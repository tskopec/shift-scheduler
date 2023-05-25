package cz.tskopec.shiftscheduler.domain.builder.optimizer.solution

import cz.tskopec.shiftscheduler.domain.builder.optimizer.eval.EmployeeStats
import cz.tskopec.shiftscheduler.domain.entities.ScheduleDay
import kotlinx.coroutines.flow.Flow
/*
Represents single mutation of the solution and contains the changed day, employee statistics and a sum of differences in scores
of the affected employees.
 */
interface Mutation {
	val scoreDelta: Double
	val mutatedDay: ScheduleDay
	val updatedStats: List<EmployeeStats>
}

interface Mutator {
	// only days on the selected indices are mutated
	fun mutationsFlow(selectedIndices: List<Int>): Flow<Mutation>
}
