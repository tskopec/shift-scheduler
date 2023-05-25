package cz.tskopec.shiftscheduler.domain.entities

import javafx.collections.ObservableList
import java.time.YearMonth

// Complete schedule containing concerned month, staff, shifts plan, vacations plan and staff size requirements
class Schedule(
	val month: YearMonth,
	val staff: List<Employee>,
	val plan: ObservableList<ScheduleDay>,
	val sizeRequirements: ObservableList<Map<ShiftType, Int>>,
	val vacations: ObservableList<StaffMap>
) {

	companion object {
		const val MAX_STAFF_SIZE = Integer.SIZE - 2
	}
}