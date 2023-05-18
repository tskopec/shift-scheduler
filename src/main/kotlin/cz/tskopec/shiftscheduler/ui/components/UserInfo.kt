package cz.tskopec.shiftscheduler.ui.components

import javafx.scene.control.Tooltip

object UserInfo {

	val build = Tooltip("""
		Auto-generate schedule based on current constraints.
		By default fills in only blank days, specific dates
		can be selected in the top panel.""".trimIndent()
	)
	val clear = Tooltip("""
		Return plan to its default state. Specific dates
		can be selected in the top panel.""".trimIndent()
	)
	val undo = Tooltip("Undo the last action.")
	val save = Tooltip("Save current state to JSON.")
	val quit = Tooltip("Exit without saving.")
	val shiftsView = Tooltip("Show shifts plan.")
	val requirementsView = Tooltip("Show staff size requirements.")
	val vacationsView = Tooltip("Show vacations plan.")

	val shiftPlan = Tooltip("""
		To assign shift, press
		<d> for Day,
		<n> for Night,
		<m> for Morning,
		<-> for Day off.
	""".trimIndent())

	val vacationPlan = Tooltip("""
		To change vacation plan, press
		<+> to assign vacation day,
		<-> to cancel vacation day.
	""".trimIndent())

	val requirementsPlan = Tooltip("""
		To change staff size requirements, press
		<d> and <D> to increase and decrease day shift size,
		<n> and <N> to increase and decrease night shift size,
		<m> and <M> to increase and decrease morning shift size,
	""".trimIndent())
}