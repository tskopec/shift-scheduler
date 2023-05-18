package cz.tskopec.shiftscheduler.ui.viewmodel.stats

import cz.tskopec.shiftscheduler.domain.Scheduler
import cz.tskopec.shiftscheduler.domain.entities.Schedule
import cz.tskopec.shiftscheduler.domain.entities.ShiftType
import javafx.beans.InvalidationListener
import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.ObservableList

typealias ShiftCounts = Map<ShiftType, IntegerProperty>
typealias VacationCounts = Map<Boolean, IntegerProperty>

// ViewModel for statistics view
abstract class StatsModel(
	observedList: ObservableList<*>
){
	abstract fun update()

	init {
		observedList.addListener(InvalidationListener { _ -> update() })
	}

	fun zeroShiftCounts() = ShiftType.values().associateWith { SimpleIntegerProperty(0) }
	fun zeroVacationCounts() = listOf(true, false).associateWith { SimpleIntegerProperty(0) }
}

class ShiftStatsModel(private val schedule: Schedule): StatsModel(schedule.plan) {


	val staffStats: List<ShiftCounts> = List(Scheduler.staffSize){ zeroShiftCounts() }

	init { update() }

	override fun update(){
		Scheduler.staff.forEach { emp ->
			val emptyCounts = ShiftType.values().associateWith { 0 }.toMutableMap()
			schedule.plan
				.groupBy { it.getAssignedShiftType(emp) }
				.mapValuesTo (emptyCounts){ (type, days) -> days.size }
				.forEach { (type, count) -> staffStats[emp.index][type]?.set(count) }
		}
	}
}

class VacationStatsModel(private val schedule: Schedule): StatsModel(schedule.vacations) {

	val staffStats: List<VacationCounts> = List(Scheduler.staffSize){ zeroVacationCounts() }

	init { update()	}

	override fun update(){
		Scheduler.staff.forEach { emp ->
			val count = schedule.vacations.count { emp in it }
			staffStats[emp.index].getValue(true).set(count)
			staffStats[emp.index].getValue(false).set(Scheduler.scheduleLength - count)
		}
	}
}

class RequirementsStatsModel(private val schedule: Schedule): StatsModel(schedule.sizeRequirements) {

	val requirementsStats: Map<ShiftType, IntegerProperty> = zeroShiftCounts()

	init { update()	}

	override fun update(){
		val sumCounts = schedule.sizeRequirements.fold(ShiftType.values().associateWith { 0 }){ sum, m ->
			sum.mapValues { (k,v) -> v + (m[k] ?: 0) }
		}
		for((type, count) in sumCounts)
			requirementsStats[type]?.set(count)
		requirementsStats[ShiftType.DAY_OFF]?.set(Scheduler.staffSize * Scheduler.scheduleLength - sumCounts.values.sum())
	}
}
