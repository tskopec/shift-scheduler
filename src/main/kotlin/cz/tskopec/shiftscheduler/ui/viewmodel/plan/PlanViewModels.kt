package cz.tskopec.shiftscheduler.ui.viewmodel.plan

import cz.tskopec.shiftscheduler.domain.Scheduler
import cz.tskopec.shiftscheduler.domain.entities.Schedule
import cz.tskopec.shiftscheduler.domain.entities.ShiftType
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList

// Represents view divided into columns, one for each day of the schedule.
// Columns are themselves divided into 1 or more selectable cells
abstract class PlanViewModel<T: PlanViewColumn<*>> {

	abstract val columns: List<T>

	fun setSelected(col: Int, row: Int, value: Boolean) = columns[col].select(row, value)
	fun isSelected(col: Int, row: Int): Boolean = columns[col].isSelected(row)
	fun selectedColumns(): List<T> = columns.filter { it.containsSelectedCells() }
	fun selectedColumnIndices() = selectedColumns().map{ it.index }

}

abstract class ObservingViewModel<T: PlanViewColumn<*>>(
	observedList: ObservableList<*>
): PlanViewModel<T>() {


	private val updateHandler = ListChangeListener { change ->
		while (change.next())
			for (index in change.from until change.to)
				updateColumn(index)
	}

	abstract fun updateColumn(index: Int)

	init {
		observedList.addListener(updateHandler)
	}
}

// view-model for the selectable dates panel
class DateCellsModel(
	schedule: Schedule
): PlanViewModel<DateColumn>(){

	override val columns: List<DateColumn>
		= List(schedule.plan.size){ DateColumn(it) }

}

class ShiftsPlanModel(
	private val schedule: Schedule
): ObservingViewModel<ShiftsViewColumn>(schedule.plan){

	override val columns = List(schedule.plan.size){ ShiftsViewColumn(it) }

	init { columns.indices.forEach { updateColumn(it) }}

	override fun updateColumn(index: Int){
		val day = schedule.plan[index]
		columns[index].update(schedule.staff.map { day.getAssignedShiftType(it) })
	}
}


class VacationsPlanModel(
	private val schedule: Schedule
): ObservingViewModel<VacationsViewColumn>(schedule.vacations){

	override val columns = List(schedule.vacations.size){ VacationsViewColumn(it) }

	init { columns.indices.forEach { updateColumn(it) }}

	override fun updateColumn(index: Int){
		val staffOnVacation = schedule.vacations[index]
		columns[index].update(schedule.staff.map { it in staffOnVacation })
	}
}


class RequirementsPlanModel(
	private val schedule: Schedule
): ObservingViewModel<RequirementsViewColumn>(schedule.sizeRequirements){

	override val columns = List(schedule.sizeRequirements.size){ RequirementsViewColumn(it) }

	init { columns.indices.forEach { updateColumn(it) }}

	override fun updateColumn(index: Int) {
		val requirementsCopy = schedule.sizeRequirements[index].toMutableMap()
		requirementsCopy[ShiftType.DAY_OFF] = Scheduler.staffSize - requirementsCopy.values.sum()
		columns[index].update(requirementsCopy, 0)
	}
}

