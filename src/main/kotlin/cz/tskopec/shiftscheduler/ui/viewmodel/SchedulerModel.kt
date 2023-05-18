package cz.tskopec.shiftscheduler.ui.viewmodel
import cz.tskopec.shiftscheduler.domain.entities.Schedule
import cz.tskopec.shiftscheduler.ui.viewmodel.plan.DateCellsModel
import cz.tskopec.shiftscheduler.ui.viewmodel.plan.RequirementsPlanModel
import cz.tskopec.shiftscheduler.ui.viewmodel.plan.ShiftsPlanModel
import cz.tskopec.shiftscheduler.ui.viewmodel.plan.VacationsPlanModel
import cz.tskopec.shiftscheduler.ui.viewmodel.stats.RequirementsStatsModel
import cz.tskopec.shiftscheduler.ui.viewmodel.stats.ShiftStatsModel
import cz.tskopec.shiftscheduler.ui.viewmodel.stats.VacationStatsModel


class SchedulerModel(schedule: Schedule) {

	val yearAndMonth: String = "${schedule.month.year} / ${schedule.month.month}"
	val dateCellsModel = DateCellsModel(schedule)
	val validatorModel = ValidatorViewModel()

	val staffNames: List<String> = schedule.staff.map { it.name }

	val shiftsModel = ShiftsPlanModel(schedule)
	val vacationsModel = VacationsPlanModel(schedule)
	val requirementsModel = RequirementsPlanModel(schedule)

	val shiftStatsModel = ShiftStatsModel(schedule)
	val vacationStatsModel = VacationStatsModel(schedule)
	val requirementsStatsModel = RequirementsStatsModel(schedule)


}