package cz.tskopec.shiftscheduler.ui.components

import cz.tskopec.shiftscheduler.control.Controller
import cz.tskopec.shiftscheduler.control.Controller.currentViewMode
import cz.tskopec.shiftscheduler.domain.builder.PlanBuilder
import cz.tskopec.shiftscheduler.ui.ViewMode
import cz.tskopec.shiftscheduler.ui.build.stretchable
import javafx.beans.binding.Bindings
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority

fun actionButtons() = HBox(
	buildButton().apply{ Tooltip.install(this, UserInfo.build) },
	actionButton("Clear") { Controller.clear() }.apply { Tooltip.install(this, UserInfo.clear) },
	actionButton("Undo") { Controller.undo() }.apply { Tooltip.install(this, UserInfo.undo) },
	actionButton("Save") { Controller.save() }.apply { Tooltip.install(this, UserInfo.save) },
	actionButton("Quit") { Controller.quit() }.apply { Tooltip.install(this, UserInfo.quit) },
)

fun viewButtons() = HBox(
	baseButton("Plan") { currentViewMode.set(ViewMode.SHIFTS) }
		.apply{ Tooltip.install(this, UserInfo.shiftsView)},
	baseButton("Requirements") { currentViewMode.set(ViewMode.REQUIREMENTS) }
		.apply{ Tooltip.install(this, UserInfo.requirementsView)},
	baseButton("Vacations") { currentViewMode.set(ViewMode.VACATIONS) }
		.apply{ Tooltip.install(this, UserInfo.vacationsView)}
)

private fun baseButton(text: String = "", handler: EventHandler<ActionEvent>? = null) = Button(text).stretchable().apply {
	handler?.let { onAction = it }
	HBox.setHgrow(this, Priority.ALWAYS)
}

private fun actionButton(text: String, handler: EventHandler<ActionEvent>) = baseButton(text, handler).apply {
	disableProperty().bind(PlanBuilder.planBuildInProgress)
}

private fun buildButton() = baseButton().apply {

	disableProperty().bind(currentViewMode.isNotEqualTo(ViewMode.SHIFTS))

	PlanBuilder.planBuildInProgress.let { progressProp ->
		textProperty().bind(Bindings.createStringBinding({
			if(progressProp.value) "Cancel build"
			else "Build plan"
		}, progressProp))
		onActionProperty().bind(Bindings.createObjectBinding({
			if(progressProp.value) EventHandler { Controller.cancelBuild() }
			else EventHandler { Controller.buildPlan() }
		}, progressProp))
	}

}

