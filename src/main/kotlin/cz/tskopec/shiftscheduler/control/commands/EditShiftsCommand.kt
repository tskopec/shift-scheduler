package cz.tskopec.shiftscheduler.control.commands

import cz.tskopec.shiftscheduler.domain.Scheduler
import cz.tskopec.shiftscheduler.domain.entities.ScheduleDay
import cz.tskopec.shiftscheduler.domain.entities.ScheduleDay.Companion.assignShift
import cz.tskopec.shiftscheduler.domain.entities.ShiftType
import cz.tskopec.shiftscheduler.domain.entities.StaffMap
import cz.tskopec.shiftscheduler.ui.viewmodel.SchedulerModel

class EditShiftsCommand(
	private val model: SchedulerModel,
	private val type: ShiftType
): EditScheduleCommand<ScheduleDay>(
	editedList = Scheduler.plan,
	selectedIndices = model.shiftsModel.selectedColumnIndices()) {

	override fun execute() {

		val newDays = model.shiftsModel.selectedColumns().map { column ->
			val selectedStaff = StaffMap(column.selectedRows())
			val updatedDay =  Scheduler.plan[column.index].assignShift(type, selectedStaff)
			updatedDay
		}
		Scheduler.updatePlan(newDays)
	}

	override fun undo() {
		Scheduler.updatePlan(oldValues.map { it.value })
	}
}