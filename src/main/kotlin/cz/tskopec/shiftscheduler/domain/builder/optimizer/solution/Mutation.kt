package cz.tskopec.shiftscheduler.domain.builder.optimizer.solution

import cz.tskopec.shiftscheduler.domain.entities.ScheduleDay
import cz.tskopec.shiftscheduler.domain.builder.optimizer.eval.EmployeeStats
import kotlinx.coroutines.flow.Flow


interface Mutation {
	val scoreDelta: Double
	val mutatedDay: ScheduleDay
	val updatedStats: List<EmployeeStats>
}

interface Mutator {
	// only days on the selected indices are mutated
	fun mutationsFlow(selectedIndices: List<Int>): Flow<Mutation>
}
