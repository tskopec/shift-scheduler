package cz.tskopec.shiftscheduler.control.commands

import cz.tskopec.shiftscheduler.domain.Scheduler
import cz.tskopec.shiftscheduler.domain.entities.ShiftType
import cz.tskopec.shiftscheduler.ui.viewmodel.SchedulerModel

class EditRequirementsCommand(
	model: SchedulerModel,
	private val editedType: ShiftType,
	private val delta: Int
): EditScheduleCommand<Map<ShiftType, Int>>(
	Scheduler.sizeRequirements,
	model.requirementsModel.selectedColumnIndices()) {

	override fun execute() {

		val newRequirements = editedIndices.mapNotNull { i ->
			val oldMap = editedList[i]
			val newMap = oldMap.mapValues { (type, count) ->
				if(type == editedType) count + delta else count
			}
			if(newMap.values.sum() <= Scheduler.staffSize && newMap.values.all{ it >= 0 })
				IndexedValue(i, newMap)
			else null
		}
		Scheduler.updateRequirements(newRequirements)
	}

	override fun undo() {
		Scheduler.updateRequirements(oldValues)
	}


}