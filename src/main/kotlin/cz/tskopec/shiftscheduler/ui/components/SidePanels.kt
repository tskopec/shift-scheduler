package cz.tskopec.shiftscheduler.ui.components

import cz.tskopec.shiftscheduler.domain.Scheduler
import cz.tskopec.shiftscheduler.ui.build.*
import cz.tskopec.shiftscheduler.ui.handlers.SelectionBoxController
import cz.tskopec.shiftscheduler.ui.viewmodel.SchedulerModel
import cz.tskopec.shiftscheduler.ui.viewmodel.ValidatorViewModel
import cz.tskopec.shiftscheduler.ui.viewmodel.plan.DateCellsModel
import javafx.beans.InvalidationListener
import javafx.beans.binding.Bindings
import javafx.beans.property.BooleanProperty
import javafx.scene.Node
import javafx.scene.control.Tooltip
import javafx.scene.layout.GridPane
import javafx.scene.layout.Region


fun dateYearLabel(model: SchedulerModel): Region = baseLabel(model.yearAndMonth, "panel-label")

fun employeePanel(staffNames: List<String>): Region = GridPane()
	.withFixedConstraints(1, Scheduler.staffSize)
	.apply {
		staffNames.forEachIndexed { i, name ->
			add(employeeLabel(name), 0, i) }
	}

fun datesPanel(
	model: DateCellsModel,
	boxCtl: SelectionBoxController
): Region = GridPane()
	.withFixedConstraints(Scheduler.scheduleLength, 1)
	.withSelectionHandler(boxCtl, model)
	.apply {
		model.columns.forEach {
			add(dateLabel(it.getValue(), it.selectedProp), it.getValue() - 1, 0)
		}
	}

fun dowPanel(model: ValidatorViewModel) = GridPane()
	.withFixedConstraints(Scheduler.scheduleLength, 1)
	.apply {
		model.days.forEach {
			add(dowLabel(it), it.date - 1, 0)
		}
}

private fun employeeLabel(name: String): Region = baseLabel(name, "panel-label")


private fun dateLabel(date: Int, selectedProp: BooleanProperty) = baseLabel(date.toString(), "panel-label")
	.showingSelectedStatus(selectedProp)


private fun dowLabel(day: ValidatorViewModel.ValidatorDay)
	= baseLabel(Scheduler.findDayOfWeek(day.date).name[0].uppercase(), "panel-label")
	.showingErrorStatus(Bindings.isNotEmpty(day.errors))
	.showErrorInfo(day)

private fun Node.showErrorInfo(day: ValidatorViewModel.ValidatorDay) = apply {

	val tooltip = Tooltip(day.allErrorsToString())
	day.errors.addListener(InvalidationListener {
		tooltip.text = day.allErrorsToString()
	})
	Tooltip.install(this, tooltip)
}
