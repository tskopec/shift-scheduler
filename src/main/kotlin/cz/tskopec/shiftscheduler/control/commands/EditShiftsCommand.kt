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
	Scheduler.plan,
	model.shiftsModel.selectedColumnIndices()) {

	override fun execute() {

		val newDays = model.shiftsModel.selectedColumns().map { col ->
			val selectedStaff = StaffMap(col.selectedRows())
			val updatedDay =  Scheduler.plan[col.index].assignShift(type, selectedStaff)
			updatedDay
		}
		Scheduler.updatePlan(newDays)
	}

	override fun undo() {
		Scheduler.updatePlan(oldValues.map { it.value })
	}
}