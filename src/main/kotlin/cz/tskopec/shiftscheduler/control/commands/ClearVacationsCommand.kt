package cz.tskopec.shiftscheduler.control.commands

import cz.tskopec.shiftscheduler.domain.Scheduler
import cz.tskopec.shiftscheduler.domain.entities.StaffMap
import cz.tskopec.shiftscheduler.domain.entities.StaffMaps
import cz.tskopec.shiftscheduler.ui.viewmodel.SchedulerModel

class ClearVacationsCommand(
	model: SchedulerModel
): EditScheduleCommand<StaffMap>(
	editedList = Scheduler.vacations,
	selectedIndices = model.dateCellsModel.selectedColumnIndices().ifEmpty { Scheduler.allPlanIndices }){


	override fun execute() {
		val blankMaps = editedIndices.map { IndexedValue(it, StaffMaps.EMPTY) }
		Scheduler.updateVacations(blankMaps)
	}

	override fun undo() {
		Scheduler.updateVacations(oldValues)
	}


}