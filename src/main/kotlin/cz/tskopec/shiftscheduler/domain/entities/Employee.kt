package cz.tskopec.shiftscheduler.domain.entities


class Employee(val index: Int, val name: String = "Employee (${index + 1})") {

	// bit position within StaffMap, which corresponds to this employee
	val maskBit = 1 shl index
	val map = StaffMap(maskBit)

	override fun toString(): String = name
}