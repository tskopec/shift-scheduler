package cz.tskopec.shiftscheduler.domain.builder.optimizer

import cz.tskopec.shiftscheduler.domain.Scheduler
import cz.tskopec.shiftscheduler.domain.builder.optimizer.eval.DistributionTarget
import cz.tskopec.shiftscheduler.domain.builder.optimizer.eval.EmployeeStats
import cz.tskopec.shiftscheduler.domain.builder.optimizer.eval.ShiftDistribution
import cz.tskopec.shiftscheduler.domain.builder.optimizer.solution.Mutation
import cz.tskopec.shiftscheduler.domain.builder.optimizer.solution.Solution
import cz.tskopec.shiftscheduler.domain.builder.optimizer.swap.Swapper
import cz.tskopec.shiftscheduler.domain.constraints.StaffConstraints
import cz.tskopec.shiftscheduler.domain.entities.ScheduleDay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.yield
/*
Uses simple hill-climbing algorithm to optimize given plan. By default, the aim is for the shift distributions of individual
employees to be as similar as possible to each other by approximating the average distribution.
*/
class Optimizer(
	val plan: List<ScheduleDay>,
	val constraints: StaffConstraints,
	private val selectedIndices: List<Int>
) {


	suspend fun optimizedPlan(): List<ScheduleDay> {

		var currentSolution = initialSolutionFromPlan(plan, constraints, DistributionTarget::averageDistributions)

		// loop stops when no mutation that can reduce the score exists
		while(true){
			yield()
			val mutator = Swapper(currentSolution, constraints)
			val bestMutation = mutator.mutationsFlow(selectedIndices)
				.filter { it.scoreDelta < 0.0 }
				.fold(null){ currentBest: Mutation?, next: Mutation ->
					minOf(currentBest ?: next, next, Comparator.comparing { it.scoreDelta })
				}

			bestMutation?.let { currentSolution = currentSolution.applyMutation(it) } ?: break
		}
		return currentSolution.plan
	}

	private fun initialSolutionFromPlan(
		plan: List<ScheduleDay>,
		constraints: StaffConstraints,
		targetsFinder: (List<ShiftDistribution>) -> List<DistributionTarget>
	): Solution {

		val distributions = Scheduler.staff.map { ShiftDistribution.of(it, plan, constraints) }
		val targets = targetsFinder.invoke(distributions)
		val stats = Scheduler.staff.map { EmployeeStats(it, distributions[it.index], targets[it.index]) }
		return Solution(plan.map { it }, stats)
	}
}
