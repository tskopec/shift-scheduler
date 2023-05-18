package cz.tskopec.shiftscheduler.ui.components

import cz.tskopec.shiftscheduler.domain.entities.ShiftType
import javafx.css.PseudoClass

object PseudoClasses {

	private val shiftTypePseudoClasses: Map<ShiftType, PseudoClass>
		= ShiftType.values().associateWith { PseudoClass.getPseudoClass(it.name.lowercase()) }

	val vacation: PseudoClass = PseudoClass.getPseudoClass("vacation")

	operator fun get(type: ShiftType):PseudoClass = shiftTypePseudoClasses.getValue(type)
}