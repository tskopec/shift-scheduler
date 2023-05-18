package cz.tskopec.shiftscheduler.control

import cz.tskopec.shiftscheduler.control.commands.*
import cz.tskopec.shiftscheduler.domain.Scheduler
import cz.tskopec.shiftscheduler.domain.builder.PlanBuilder
import cz.tskopec.shiftscheduler.domain.constraints.ScheduleErrorType
import cz.tskopec.shiftscheduler.domain.constraints.Validator
import cz.tskopec.shiftscheduler.domain.entities.ShiftType
import cz.tskopec.shiftscheduler.ui.ViewMode
import cz.tskopec.shiftscheduler.ui.build.ViewBuilder
import cz.tskopec.shiftscheduler.ui.viewmodel.SchedulerModel
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType

object Controller {

	val currentViewMode = SimpleObjectProperty(ViewMode.SHIFTS)

	private val schedulerModel: SchedulerModel = Scheduler.createViewModel()
	private val commandStacks: Map<ViewMode, MutableList<UndoableCommand>> = ViewMode.values().associateWith { mutableListOf() }
	private val currentStack: MutableList<UndoableCommand> get() = commandStacks.getValue(currentViewMode.value)


	init {
		Validator.validateUpdate(Scheduler.plan.indices.toList(), ScheduleErrorType.all)
	}

	fun getView() = ViewBuilder(schedulerModel).build()

	fun undo(){
		currentStack.removeLastOrNull()?.undo()
	}

	fun buildPlan(){
		execute(BuildPlanCommand(schedulerModel.dateCellsModel))
	}

	fun cancelBuild(){
		PlanBuilder.cancelBuildJob()
	}

	fun editShifts(type: ShiftType){
		execute(EditShiftsCommand(schedulerModel, type))
	}

	fun editRequirements(type: ShiftType, delta: Int){
		execute(EditRequirementsCommand(schedulerModel, type, delta))
	}

	fun editVacations(value: Boolean){
		execute(EditVacationsCommand(schedulerModel, value))
	}

	fun clear(){
		when(currentViewMode.get()){
			ViewMode.SHIFTS -> execute(ClearShiftsCommand(schedulerModel))
			ViewMode.REQUIREMENTS -> execute(ClearRequirementsCommand(schedulerModel))
			ViewMode.VACATIONS -> execute(ClearVacationsCommand(schedulerModel))
			else -> {}
		}
	}

	fun quit(){
		Alert(
			Alert.AlertType.CONFIRMATION,
			"Unsaved progress will be lost. Quit?",
			ButtonType.OK, ButtonType.CANCEL
		).showAndWait()
			.ifPresent { if(it.buttonData == ButtonBar.ButtonData.OK_DONE) Platform.exit() }
	}

	fun save(){
		execute(SaveJsonCommand())
	}

	fun showErrors(result: Validator.Result) {
		schedulerModel.validatorModel.update(result)
	}

	private fun execute(cmd: Command){

		cmd.execute()
		if(cmd is UndoableCommand && cmd.hadEffect())
			currentStack.add(cmd)
	}
}
