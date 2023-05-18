package cz.tskopec.shiftscheduler.ui.handlers

import cz.tskopec.shiftscheduler.control.Controller
import cz.tskopec.shiftscheduler.control.Controller.currentViewMode
import cz.tskopec.shiftscheduler.domain.builder.PlanBuilder
import cz.tskopec.shiftscheduler.domain.entities.ShiftType
import cz.tskopec.shiftscheduler.ui.ViewMode
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.EventHandler
import javafx.scene.input.KeyEvent


class KeyHandler: EventHandler<KeyEvent> {

	private val keysDisabledProp = SimpleBooleanProperty(false).apply{
		bind(PlanBuilder.planBuildInProgress)
	}

	private val generalKeyMap: Map<String, () -> Unit> = mapOf(
		"c" to Controller::clear,
		"u" to Controller::undo,
		"q" to Controller::quit,
		"s" to Controller::save
	)

	private val keyMaps:Map<ViewMode, Map<String,() -> Unit>> = mapOf(
		ViewMode.SHIFTS to mapOf(
			"d" to { Controller.editShifts(ShiftType.DAY) },
			"n" to { Controller.editShifts(ShiftType.NIGHT) },
			"m" to { Controller.editShifts(ShiftType.MORNING) },
			"-" to { Controller.editShifts(ShiftType.DAY_OFF) },
			"b" to { Controller.buildPlan() }
		),
		ViewMode.REQUIREMENTS to mapOf(
			"d" to { Controller.editRequirements(ShiftType.DAY, 1) },
			"n" to { Controller.editRequirements(ShiftType.NIGHT, 1) },
			"m" to { Controller.editRequirements(ShiftType.MORNING, 1) },
			"D" to { Controller.editRequirements(ShiftType.DAY, -1) },
			"N" to { Controller.editRequirements(ShiftType.NIGHT, -1) },
			"M" to { Controller.editRequirements(ShiftType.MORNING, -1) }
		),
		ViewMode.VACATIONS to mapOf(
			"+" to { Controller.editVacations(true) },
			"-" to { Controller.editVacations(false) }
		)
	)

	override fun handle(event: KeyEvent?) {

		if(keysDisabledProp.value)
			return

		if(event?.eventType == KeyEvent.KEY_TYPED){

			val key = event?.character ?: return
			val activeKeyMap = keyMaps[currentViewMode.value] ?: return
			val action = activeKeyMap[key] ?: generalKeyMap[key] ?: return
			action.invoke()
		}
	}

}