package cz.tskopec.shiftscheduler.domain

import cz.tskopec.shiftscheduler.domain.constraints.ScheduleErrorType
import cz.tskopec.shiftscheduler.domain.constraints.StaffConstraints
import cz.tskopec.shiftscheduler.domain.constraints.Validator
import cz.tskopec.shiftscheduler.domain.entities.Employee
import cz.tskopec.shiftscheduler.domain.entities.ScheduleDay
import cz.tskopec.shiftscheduler.domain.entities.ShiftType
import cz.tskopec.shiftscheduler.domain.entities.StaffMap
import cz.tskopec.shiftscheduler.ui.startup.ScheduleInitializer
import cz.tskopec.shiftscheduler.ui.viewmodel.SchedulerModel
import javafx.collections.ObservableList
import java.time.DayOfWeek
import java.time.LocalDate
// Main domain object which provides different parts of the schedule as observable lists
object Scheduler {

	val schedule = ScheduleInitializer.getSchedule()

	val staff: List<Employee> = schedule.staff
	val staffSize: Int = staff.size

	val plan: ObservableList<ScheduleDay> = schedule.plan
	val sizeRequirements: ObservableList<Map<ShiftType, Int>> = schedule.sizeRequirements
	val vacations: ObservableList<StaffMap> = schedule.vacations

	val scheduleLength: Int = schedule.plan.size
	val allPlanIndices: List<Int> = plan.indices.toList()
	val blankDayIndices: List<Int> get() = schedule.plan.filter(ScheduleDay::isBlank).map(ScheduleDay::index)


	fun createViewModel(): SchedulerModel = SchedulerModel(schedule)
	fun planCopy() = schedule.plan.toList()
	fun constraintsCopy() = StaffConstraints(schedule.sizeRequirements.toList(), schedule.vacations.toList())


	fun updatePlan(newDays: List<ScheduleDay>){

		newDays.forEach { plan[it.index] = it }
		Validator.validateUpdate(newDays.map { it.index }, ScheduleErrorType.planUpdateRelevant)
	}

	fun updateRequirements(newRequirements: List<IndexedValue<Map<ShiftType, Int>>>){

		newRequirements.forEach { (i, value) -> sizeRequirements[i] = value }
		Validator.validateUpdate(newRequirements.map { it.index }, ScheduleErrorType.requirementsUpdateRelevant)
	}

	fun updateVacations(newVacations: List<IndexedValue<StaffMap>>){

		newVacations.forEach { (i, staff) -> vacations[i] = staff }
		Validator.validateUpdate(newVacations.map { it.index }, ScheduleErrorType.vacationUpdateRelevant)
	}

	fun findDayOfWeek(date: Int): DayOfWeek
			= LocalDate.of(schedule.month.year, schedule.month.month, date).dayOfWeek

}
