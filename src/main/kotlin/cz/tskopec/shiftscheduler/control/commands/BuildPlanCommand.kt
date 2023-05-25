package cz.tskopec.shiftscheduler.control.commands

import cz.tskopec.shiftscheduler.domain.Scheduler
import cz.tskopec.shiftscheduler.domain.builder.PlanBuilder
import cz.tskopec.shiftscheduler.domain.entities.ScheduleDay
import cz.tskopec.shiftscheduler.ui.viewmodel.plan.DateCellsModel
import javafx.concurrent.Task
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType

// replaces selected parts of the shifts plan by auto-generated and optimized days
class BuildPlanCommand(
	model: DateCellsModel
) : EditScheduleCommand<ScheduleDay>(
	editedList = Scheduler.plan,
	selectedIndices = model.selectedColumnIndices().ifEmpty { Scheduler.blankDayIndices }
) {

	override fun execute() {

		PlanBuilder.planBuildInProgress.set(true)

		val buildTask = object : Task<PlanBuilder.Result>() {
			override fun call(): PlanBuilder.Result {
				return PlanBuilder.build(
					Scheduler.planCopy(),
					Scheduler.constraintsCopy(),
					editedIndices
				)
			}
		}.apply {
			setOnSucceeded { handleResult((value)) }
			setOnFailed {  PlanBuilder.planBuildInProgress.set(false) }
		}
		Thread(buildTask).start()
	}

	override fun hadEffect(): Boolean = editedIndices.isNotEmpty()

	override fun undo() {
		Scheduler.updatePlan(oldValues.map{ it.value })
	}

	private fun handleResult(result: PlanBuilder.Result) {
		when (result) {
			is PlanBuilder.Success -> {
				Scheduler.updatePlan(result.plan)
			}
			is PlanBuilder.Failure -> {
				Alert(Alert.AlertType.ERROR, result.reason, ButtonType.OK).show()
			}
		}
		PlanBuilder.planBuildInProgress.set(false)
	}
}
