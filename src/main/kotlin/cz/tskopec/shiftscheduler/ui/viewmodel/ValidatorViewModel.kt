package cz.tskopec.shiftscheduler.ui.viewmodel

import cz.tskopec.shiftscheduler.domain.Scheduler
import cz.tskopec.shiftscheduler.domain.constraints.ScheduleErrorType
import cz.tskopec.shiftscheduler.domain.constraints.Validator
import javafx.collections.FXCollections
import javafx.collections.ObservableSet

class ValidatorViewModel {

	class ValidatorDay(val date: Int) {

		val errors: ObservableSet<ScheduleErrorType> = FXCollections.observableSet()
		fun allErrorsToString()
			= "${Scheduler.findDayOfWeek(date).name} $date.\n" +
			errors.joinToString(separator = "\n") { it.msg }.ifBlank { "No errors" }
	}

	val days = List(Scheduler.scheduleLength) { ValidatorDay(it + 1) }


	fun update(result: Validator.Result){

		val dayToUpdate = days[result.dayIndex]
		for((error, isPresent) in result.presentErrors){
			if(isPresent)
				dayToUpdate.errors += error
			else dayToUpdate.errors -= error
		}
	}
}