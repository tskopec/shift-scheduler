package cz.tskopec.shiftscheduler.domain.builder.optimizer.swap


import cz.tskopec.shiftscheduler.domain.builder.optimizer.eval.EmployeeStats
import cz.tskopec.shiftscheduler.domain.builder.optimizer.solution.Mutator
import cz.tskopec.shiftscheduler.domain.builder.optimizer.solution.Solution
import cz.tskopec.shiftscheduler.domain.constraints.ScheduleRules
import cz.tskopec.shiftscheduler.domain.constraints.StaffConstraints
import cz.tskopec.shiftscheduler.domain.entities.Employee
import cz.tskopec.shiftscheduler.domain.entities.ScheduleDay
import cz.tskopec.shiftscheduler.domain.entities.ShiftType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge

// generates mutations by swapping two employees from different shifts on each day
class Swapper(
	private val solution: Solution,
	private val constraints: StaffConstraints
): Mutator {


	override fun mutationsFlow(selectedIndices: List<Int>) = selectedIndices.map { mutationsOfDay(it) }.merge()

	private fun mutationsOfDay(dayIndex: Int): Flow<SwapMutation> = flow {

		val originalDay = solution.plan[dayIndex]
		SwapPair.allPairs(originalDay).mapNotNull { pair ->
			val mutatedDay = mutateDay(originalDay, pair) ?: return@mapNotNull null
			val updatedStats = updateStats(dayIndex, pair)
			val scoreDelta = computeScoreDelta(pair, updatedStats)
			return@mapNotNull SwapMutation(mutatedDay, updatedStats, scoreDelta)
		}.forEach { emit(it) }
	}


	private fun mutateDay(day: ScheduleDay, pair: SwapPair): ScheduleDay? {

		val newShiftA = pair.first.containingShift.swapEmployees(toAdd = pair.second.employee, toRemove = pair.first.employee)
		val newShiftB = pair.second.containingShift.swapEmployees(toAdd = pair.first.employee, toRemove = pair.second.employee)

		return if (validateSwap(day.index, newShiftA, newShiftB, solution.plan))
			day.updateShifts(newShiftA, newShiftB)
		else null
	}

	private fun updateStats(dayIndex: Int, pair: SwapPair): List<EmployeeStats> {

		val updatedStatsA = solution.statsOf(pair.first.employee)
			.update(dayIndex, increment = pair.secondShiftType, decrement = pair.firstShiftType)
		val updatedStatsB = solution.statsOf(pair.second.employee)
			.update(dayIndex, increment = pair.firstShiftType, decrement = pair.secondShiftType)
		return solution.stats.toMutableList().apply {
			this[pair.first.employee.index] = updatedStatsA
			this[pair.second.employee.index] = updatedStatsB
		}
	}

	private fun validateSwap(
		dayIndex: Int,
		shiftA: TypedStaffMap,
		shiftB: TypedStaffMap,
		plan: List<ScheduleDay>
	): Boolean {

		fun validateShift(typeAndStaff: TypedStaffMap): Boolean = typeAndStaff.let { (type, staff) ->
			return if (type == ShiftType.DAY_OFF)
				true
			else constraints.satisfiesVacationPlan(dayIndex, staff)
				&& ScheduleRules.isStaffMapValid(dayIndex, type, staff, plan)
		}
		return validateShift(shiftA) && validateShift(shiftB)
	}

	private fun ScheduleDay.updateShifts(
		newShiftA: TypedStaffMap,
		newShiftB: TypedStaffMap
	): ScheduleDay {

		val updatedShifts = shifts.toMutableMap().also {
			it[newShiftA.type] = newShiftA.staff
			it[newShiftB.type] = newShiftB.staff
		}
		return ScheduleDay(date, updatedShifts)
	}


	private fun computeScoreDelta(pair: SwapPair, updatedStats: List<EmployeeStats>): Double {

		fun compute(employee: Employee): Double {
			val oldStats = solution.statsOf(employee)
			val newStats = updatedStats[employee.index]
			return newStats.score - oldStats.score
		}
		return compute(pair.first.employee) + compute(pair.second.employee)
	}


}