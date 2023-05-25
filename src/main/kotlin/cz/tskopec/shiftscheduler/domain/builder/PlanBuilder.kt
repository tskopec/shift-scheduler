package cz.tskopec.shiftscheduler.domain.builder


import cz.tskopec.shiftscheduler.domain.Scheduler
import cz.tskopec.shiftscheduler.domain.builder.generator.GeneratorResult
import cz.tskopec.shiftscheduler.domain.builder.generator.PlanGenerator
import cz.tskopec.shiftscheduler.domain.builder.optimizer.Optimizer
import cz.tskopec.shiftscheduler.domain.constraints.StaffConstraints
import cz.tskopec.shiftscheduler.domain.entities.ScheduleDay
import javafx.beans.property.SimpleBooleanProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// Runs a coroutine that auto-generates a shifts plan. At first a random plan conforming to the rules
// and constraints is generated, this plan is then optimized to approach the ideal shift distribution.
// Only days at selectedIndices are modified, the rest retains their state from originalPlan
object PlanBuilder {

	sealed class Result
	class Success(val plan: List<ScheduleDay>) : Result()
	class Failure(val reason: String = "Unknown failure") : Result()

	val planBuildInProgress = SimpleBooleanProperty(false)
	private var buildJob: Job? = null

	fun build(
		originalPlan: List<ScheduleDay>,
		constraints: StaffConstraints,
		selectedIndices: List<Int>,
	): Result {

		if (selectedIndices.isEmpty())
			return Success(originalPlan)
		if (constraints.areImpossible())
			return Failure("Impossible constraints")

		val skippedIndicesMap = Array(Scheduler.scheduleLength) { !selectedIndices.contains(it) }
		var result: Result = Success(emptyList())

		runBlocking(Dispatchers.Default) {

			buildJob = launch {

				val generator = PlanGenerator(originalPlan, constraints, skippedIndicesMap)
				when (val generatorResult = generator.generateRandomPlan()) {
					is GeneratorResult.Success -> {
						result = Success(generatorResult.plan)
						val optimizer = Optimizer(generatorResult.plan, constraints, selectedIndices)
						result = Success(optimizer.optimizedPlan())
					}
					GeneratorResult.DeadEnd -> result = Failure("No solution found")
				}
			}
		}
		return result
	}


	fun cancelBuildJob() {
		if (planBuildInProgress.value){
			buildJob?.cancel()
			planBuildInProgress.set(false)
		}
	}
}
