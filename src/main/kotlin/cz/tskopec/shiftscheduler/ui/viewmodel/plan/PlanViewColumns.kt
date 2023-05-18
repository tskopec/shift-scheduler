package cz.tskopec.shiftscheduler.ui.viewmodel.plan

import cz.tskopec.shiftscheduler.domain.Scheduler
import cz.tskopec.shiftscheduler.domain.entities.ShiftType
import javafx.beans.property.BooleanProperty
import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty


abstract class PlanViewColumn<T> {

	interface Cell<T> {
		val selectedProp: BooleanProperty
		val valueProp: Property<T>
		fun isSelected(): Boolean = selectedProp.value
		fun getValue(): T = valueProp.value
	}

	abstract val index: Int
	abstract val cells: List<Cell<T>>

	open fun getCell(row: Int): Cell<T> = cells[row]
	fun containsSelectedCells(): Boolean = cells.any { it.isSelected() }

	fun select(row: Int, value: Boolean) {
		getCell(row).selectedProp.value = value
	}
	fun isSelected(row: Int): Boolean = getCell(row).isSelected()
	fun selectedRows(): Set<Int> = cells.mapIndexedNotNull { index, cell -> if (cell.isSelected()) index else null }.toSet()
	fun update(newValue: T, row: Int) {
		getCell(row).valueProp.value = newValue
	}
	fun update(newValues: List<T>) = newValues.forEachIndexed { index, newVal -> update(newVal, index) }
}

class ShiftsViewColumn(override val index: Int) : PlanViewColumn<ShiftType>() {
	class Cell : PlanViewColumn.Cell<ShiftType> {
		override val selectedProp = SimpleBooleanProperty(false)
		override val valueProp = SimpleObjectProperty(ShiftType.DAY_OFF)
	}

	override val cells = List(Scheduler.staffSize) { Cell() }
}

class VacationsViewColumn(override val index: Int) : PlanViewColumn<Boolean>() {
	class Cell : PlanViewColumn.Cell<Boolean> {
		override val selectedProp = SimpleBooleanProperty(false)
		override val valueProp = SimpleBooleanProperty(false)
	}

	override val cells = List(Scheduler.staffSize) { Cell() }
}

class RequirementsViewColumn(
	override val index: Int
) : PlanViewColumn<Map<ShiftType, Int>>(),
	PlanViewColumn.Cell<Map<ShiftType, Int>> {

	override val selectedProp = SimpleBooleanProperty(false)
	override val valueProp = SimpleObjectProperty(ShiftType.values().associateWith { 0 })

	override val cells: List<Cell<Map<ShiftType, Int>>> = listOf(this)
	override fun getCell(row: Int) = this
}

class DateColumn(override val index: Int) : PlanViewColumn<Int>(), PlanViewColumn.Cell<Int> {

	override val selectedProp = SimpleBooleanProperty(false)
	override val valueProp = SimpleObjectProperty(index + 1)

	override val cells: List<Cell<Int>> = listOf(this)
	override fun getCell(row: Int) = this
}

