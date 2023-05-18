package cz.tskopec.shiftscheduler.domain.constraints

import java.util.*


enum class ScheduleErrorType(val msg: String) {

	ROW_LIMIT("Too many shifts in a row."),
	ILLEGAL_ORDER("Illegal shift order."),
	UNDERSTAFFED("Not enough staff."),
	VACATION_CONFLICT("Shift conflicting with vacation."),
	IMPOSSIBLE_CONSTRAINTS("More staff required than available");

	companion object {

		val all: EnumSet<ScheduleErrorType> = EnumSet.allOf(ScheduleErrorType::class.java)

		// error types to be tested after editing various parts of the schedule
		val planUpdateRelevant: EnumSet<ScheduleErrorType> = EnumSet.of(ROW_LIMIT, ILLEGAL_ORDER, UNDERSTAFFED, VACATION_CONFLICT)
		val requirementsUpdateRelevant: EnumSet<ScheduleErrorType> = EnumSet.of(IMPOSSIBLE_CONSTRAINTS)
		val vacationUpdateRelevant: EnumSet<ScheduleErrorType> = EnumSet.of(VACATION_CONFLICT, IMPOSSIBLE_CONSTRAINTS)
	}
}

