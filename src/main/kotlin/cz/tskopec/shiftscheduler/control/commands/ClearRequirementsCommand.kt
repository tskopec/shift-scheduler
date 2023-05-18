package cz.tskopec.shiftscheduler.control.commands

import cz.tskopec.shiftscheduler.Settings
import cz.tskopec.shiftscheduler.domain.Scheduler
import cz.tskopec.shiftscheduler.domain.entities.ShiftType
import cz.tskopec.shiftscheduler.ui.viewmodel.SchedulerModel


class ClearRequirementsCommand(
	model: SchedulerModel
): EditScheduleCommand<Map<ShiftType, Int>>(
	Scheduler.sizeRequirements,
	model.dateCellsModel.selectedColumnIndices().ifEmpty { Scheduler.allPlanIndices }){


	override fun execute() {
		val defaultMaps = editedIndices.map { IndexedValue(it, Settings.defaultRequirements) }
		Scheduler.updateRequirements(defaultMaps)
	}

	override fun undo() {
		Scheduler.updateRequirements(oldValues)
	}
}
