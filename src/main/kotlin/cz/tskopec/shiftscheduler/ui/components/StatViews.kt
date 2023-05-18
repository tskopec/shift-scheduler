package cz.tskopec.shiftscheduler.ui.components

import cz.tskopec.shiftscheduler.control.Controller.currentViewMode
import cz.tskopec.shiftscheduler.domain.Scheduler
import cz.tskopec.shiftscheduler.domain.entities.ShiftType
import cz.tskopec.shiftscheduler.ui.ViewMode
import cz.tskopec.shiftscheduler.ui.build.*
import cz.tskopec.shiftscheduler.ui.viewmodel.SchedulerModel
import javafx.beans.property.IntegerProperty
import javafx.scene.control.Tooltip
import javafx.scene.layout.GridPane
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane


fun statsViews(model: SchedulerModel): Region {
	return StackPane(
		shiftStatsView(model),
		requirementsStatsView(model),
		vacationsStatsView(model)
	)
}


private fun statLabel(countProp: IntegerProperty?, type: ShiftType? = null) = basicCellLabel().apply {
	textProperty().bind(countProp?.asString())
	Tooltip.install(this, Tooltip("Shift type: $type"))
}

private fun shiftStatsView(model: SchedulerModel): Region = GridPane()
	.withFixedConstraints(1, Scheduler.staffSize)
	.boundToViewMode(ViewMode.SHIFTS, currentViewMode)
	.apply {
		model.shiftStatsModel.staffStats.forEachIndexed { index, stats ->
			val rowPane = GridPane().apply { rowConstraints += fixedConstraints(1) }
			ShiftType.values().forEach { type ->
				rowPane.add(shiftStatLabel(stats, type), type.ordinal, 0)
				rowPane.columnConstraints.add(boundConstraints(stats.values, stats.getValue(type)))
			}
			add(rowPane, 0, index)
		}
	}

private fun shiftStatLabel(shiftCounts: Map<ShiftType, IntegerProperty>, type: ShiftType)
	= statLabel(shiftCounts[type], type).apply { pseudoClassStateChanged(PseudoClasses[type], true) }

private fun vacationsStatsView(model: SchedulerModel): Region = GridPane()
	.withFixedConstraints(1, Scheduler.staffSize)
	.boundToViewMode(ViewMode.VACATIONS, currentViewMode)
	.apply {
		for (emp in Scheduler.staff) {
			val stats = model.vacationStatsModel.staffStats[emp.index]
			val rowPane = GridPane().apply { rowConstraints += fixedConstraints(1) }
			stats.entries.forEachIndexed { index, (vacationValue, count) ->
				rowPane.add(vacationStatLabel(stats, vacationValue), index, 0)
				rowPane.columnConstraints.add(boundConstraints(stats.values, count))
			}
			add(rowPane, 0, emp.index)
		}
	}

private fun vacationStatLabel(vacationCounts: Map<Boolean, IntegerProperty>, vacationValue: Boolean)
	= statLabel(vacationCounts[vacationValue]).apply {
		pseudoClassStateChanged(PseudoClasses.vacation, vacationValue )
	}


private fun requirementsStatsView(model: SchedulerModel): Region = GridPane()
	.apply { columnConstraints += fixedConstraints(1) }
	.boundToViewMode(ViewMode.REQUIREMENTS, currentViewMode)
	.apply {
		val stats = model.requirementsStatsModel.requirementsStats
		ShiftType.values().forEach { type ->
			rowConstraints.add(boundConstraints(stats.values, stats.getValue(type)))
			add(requirementStatLabel(stats[type], type), 0, type.ordinal)
		}
	}

private fun requirementStatLabel(countProp: IntegerProperty?, type: ShiftType)
	= statLabel(countProp, type).apply {
		pseudoClassStateChanged(PseudoClasses[type], true)
		minHeight = 0.0
	}





