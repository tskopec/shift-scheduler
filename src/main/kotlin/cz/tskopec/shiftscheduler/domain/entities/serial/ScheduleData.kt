package cz.tskopec.shiftscheduler.domain.entities.serial

import cz.tskopec.shiftscheduler.domain.Scheduler
import cz.tskopec.shiftscheduler.domain.entities.ShiftType
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleData(
	val yearMonthStr: String,
	val staffData: List<String>,
	val planData: List<Map<ShiftType, Set<Int>>>,
	val requirementsData: List<Map<ShiftType, Int>>,
	val vacationsData: List<Set<Int>>
	) {

	companion object {

		fun createFromCurrentState()
			= ScheduleData(
				Scheduler.schedule.month.toString(),
				Scheduler.staff.map { it.name },
				Scheduler.plan.map { it.shifts.mapValues { (_, staff) -> staff.toIndices() } },
				Scheduler.sizeRequirements,
				Scheduler.vacations.map{ it.toIndices() }
			)
	}
}