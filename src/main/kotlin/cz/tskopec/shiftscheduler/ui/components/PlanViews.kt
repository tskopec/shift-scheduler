package cz.tskopec.shiftscheduler.ui.components


import cz.tskopec.shiftscheduler.ui.handlers.SelectionBoxController
import cz.tskopec.shiftscheduler.ui.viewmodel.SchedulerModel
import cz.tskopec.shiftscheduler.control.Controller.currentViewMode
import cz.tskopec.shiftscheduler.domain.Scheduler
import cz.tskopec.shiftscheduler.domain.entities.ShiftType
import cz.tskopec.shiftscheduler.ui.ViewMode
import cz.tskopec.shiftscheduler.ui.build.boundToViewMode
import cz.tskopec.shiftscheduler.ui.build.selectableCellLabel
import cz.tskopec.shiftscheduler.ui.build.withFixedConstraints
import cz.tskopec.shiftscheduler.ui.build.withSelectionHandler
import cz.tskopec.shiftscheduler.ui.viewmodel.plan.*
import javafx.beans.binding.Bindings
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.layout.GridPane
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane


fun planViews(model: SchedulerModel, selectionBoxCtl: SelectionBoxController): Region {
	return  StackPane(
		shiftsView(model, selectionBoxCtl),
		requirementsView(model, selectionBoxCtl),
		vacationsView(model, selectionBoxCtl)
	).apply{ styleClass += "central-view" }
}

private fun shiftsView(model: SchedulerModel, boxCtl: SelectionBoxController): Region = planView(model.shiftsModel)
	.withSelectionHandler(boxCtl, model.shiftsModel)
	.boundToViewMode(ViewMode.SHIFTS, currentViewMode)
	.apply{ Tooltip.install(this, UserInfo.shiftPlan)}

private fun vacationsView(model: SchedulerModel, boxCtl: SelectionBoxController): Region = planView(model.vacationsModel)
	.withSelectionHandler(boxCtl, model.vacationsModel)
	.boundToViewMode(ViewMode.VACATIONS, currentViewMode)
	.apply{ Tooltip.install(this, UserInfo.vacationPlan)}

private fun requirementsView(model: SchedulerModel, boxCtl: SelectionBoxController): Region = planView(model.requirementsModel)
	.withSelectionHandler(boxCtl, model.requirementsModel)
	.boundToViewMode(ViewMode.REQUIREMENTS, currentViewMode)
	.apply{ Tooltip.install(this, UserInfo.requirementsPlan)}


private fun <C : PlanViewColumn<*>> planView(model: PlanViewModel<C>): GridPane =
	GridPane().withFixedConstraints(Scheduler.scheduleLength, Scheduler.staffSize).apply {

		fun addColumnOfGridCells(columnIndex: Int, column: PlanViewColumn<*>, labelProducer: (PlanViewColumn.Cell<*>) -> Label) {
			column.cells.forEachIndexed { rowIndex, cell ->
				add(labelProducer(cell), columnIndex, rowIndex)
			}
		}

		fun addRequirementBars(columnIndex: Int, column: RequirementsViewColumn) {
			add(requirementBars(column), columnIndex, 0)
		}

		model.columns.forEachIndexed { i, col ->
			when (col) {
				is ShiftsViewColumn -> addColumnOfGridCells(i, col) { shiftCellLabel(it as ShiftsViewColumn.Cell) }
				is VacationsViewColumn -> addColumnOfGridCells(i, col) { vacationCellLabel(it as VacationsViewColumn.Cell) }
				is RequirementsViewColumn -> addRequirementBars(i, col)
				else -> throw IllegalStateException("Unknown cell type")
			}
		}
	}



private fun shiftCellLabel(cell: ShiftsViewColumn.Cell) = selectableCellLabel(cell).apply {
		textProperty().bind(Bindings.createStringBinding({ cell.getValue().symbol.toString() }, cell.valueProp))
		pseudoClassStateChanged(PseudoClasses[cell.getValue()], true)
		cell.valueProp.addListener { _, oldValue, newValue ->
			pseudoClassStateChanged(PseudoClasses[oldValue], false)
			pseudoClassStateChanged(PseudoClasses[newValue], true)
		}

	}

private fun vacationCellLabel(cell: VacationsViewColumn.Cell) = selectableCellLabel(cell).apply {
		pseudoClassStateChanged(PseudoClasses.vacation, cell.getValue())
		cell.valueProp.addListener { _  ->
			pseudoClassStateChanged(PseudoClasses.vacation, cell.getValue())
		}
	}

private fun requirementBar(type: ShiftType, cell: PlanViewColumn.Cell<Map<ShiftType, Int>>) = selectableCellLabel(cell).apply {
		textProperty().bind(Bindings.createStringBinding({ cell.getValue()[type].toString() }, cell.valueProp))
		pseudoClassStateChanged(PseudoClasses[type], true)
	}


private fun requirementBars(column: RequirementsViewColumn) = GridPane()
	.withFixedConstraints(1, Scheduler.staffSize)
	.also { barsPane ->

		GridPane.setRowSpan(barsPane, Scheduler.staffSize)

		val barsByShiftType = ShiftType.values().associateWith { type -> requirementBar(type, column) }

		fun updateBars(){
			val requirements = column.getValue()
			var previousSizesSum = 0
			for(type in ShiftType.values()){
				val label = barsByShiftType[type]
				val span = requirements[type] ?: 0
				if(span > 0){
					if(!barsPane.children.contains(label))
						barsPane.add(label, 0, 0)
					GridPane.setRowIndex(label, previousSizesSum)
					GridPane.setRowSpan(label, span)
				}
				else
					barsPane.children -= label
				previousSizesSum += span
			}
		}

		barsByShiftType.values.forEach { barsPane.add(it, 0, 0) }
		column.valueProp.addListener { _ -> updateBars() }
		updateBars()
	}

