package cz.tskopec.shiftscheduler.ui.startup

import cz.tskopec.shiftscheduler.Settings
import cz.tskopec.shiftscheduler.domain.entities.*
import cz.tskopec.shiftscheduler.domain.entities.serial.ScheduleData
import javafx.collections.FXCollections
import java.io.File
import java.time.YearMonth
import java.time.format.DateTimeParseException

object ScheduleInitializer {

	private var loadedSchedule: Schedule? = null


	fun tryLoadingSchedule(data: ScheduleData): Boolean {

		loadedSchedule = fromSerializedForm(data) ?: return false
		return true
	}


	fun getSchedule(): Schedule {
		return loadedSchedule ?: generateSchedule()
	}

	private fun fromSerializedForm(data: ScheduleData): Schedule? {

		val yearMonth = readYearMonth(data.yearMonthStr) ?: return null

		if(!dataCheck(data, yearMonth.lengthOfMonth()))
			return null

		val staff = data.staffData.mapIndexed { i, name -> Employee(i, name) }

		val plan = FXCollections.observableArrayList((1..yearMonth.lengthOfMonth()).map { date ->
			ScheduleDay(date, data.planData[date - 1].mapValues { (_, employeeIndices) -> StaffMap(employeeIndices) })
		})
		val sizeRequirements =
			FXCollections.observableArrayList(*Array(yearMonth.lengthOfMonth()) { data.requirementsData[it] })
		val vacations =
			FXCollections.observableArrayList(*Array(yearMonth.lengthOfMonth()) { StaffMap(data.vacationsData[it]) })

		return Schedule(yearMonth, staff, plan, sizeRequirements, vacations)
	}

	private fun dataCheck(data:ScheduleData, monthLength: Int): Boolean = with(data) {
		return staffData.size in 1..Schedule.MAX_STAFF_SIZE
			&& listOf(planData, requirementsData, vacationsData).all { it.size == monthLength }
	}



	private fun generateSchedule(): Schedule {

		val yearMonth = readYearMonth(Settings.month) ?: currentNextMonth()
		val staff = readRosterFile(MainMenu.rosterPath) ?: generateRoster()
		val plan = FXCollections.observableArrayList((1..yearMonth.lengthOfMonth()).map {
			ScheduleDay.blank(it, staff.size)
		})
		val sizeRequirements =
			FXCollections.observableArrayList(*Array(yearMonth.lengthOfMonth()) { Settings.defaultRequirements })
		val vacations = FXCollections.observableArrayList(*Array(yearMonth.lengthOfMonth()) { StaffMaps.EMPTY })

		return Schedule(yearMonth, staff, plan, sizeRequirements, vacations)

	}

	private fun readYearMonth(ymString: String): YearMonth? {

		return try {
			YearMonth.parse(ymString)
		} catch (e: DateTimeParseException) {
			null
		}
	}

	private fun currentNextMonth(): YearMonth {

		return YearMonth.now().plusMonths(1)
	}


	private fun readRosterFile(path: String): List<Employee>? {

		val rosterFile = File(path)

		return if (rosterFile.isFile && rosterFile.canRead()) {
			rosterFile.readLines()
				.filter { it.isNotBlank() && !it.startsWith("#") }
				.take(Schedule.MAX_STAFF_SIZE)
				.mapIndexed { i, name -> Employee(i, name.trim()) }
		} else null
	}

	private fun generateRoster(): List<Employee> {

		return (0 until minOf(Settings.defaultStaffSize, Schedule.MAX_STAFF_SIZE)).map { Employee(it) }
	}

}