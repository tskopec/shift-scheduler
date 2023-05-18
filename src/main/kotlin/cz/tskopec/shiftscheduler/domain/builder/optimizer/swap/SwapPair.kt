package cz.tskopec.shiftscheduler.domain.builder.optimizer.swap

import cz.tskopec.shiftscheduler.domain.Scheduler
import cz.tskopec.shiftscheduler.domain.entities.Employee
import cz.tskopec.shiftscheduler.domain.entities.ScheduleDay
import cz.tskopec.shiftscheduler.domain.entities.ShiftType
import cz.tskopec.shiftscheduler.domain.entities.StaffMap

// Pair of employees and their assigned shifts.
// In Swapper, employees are swapped with respect to those shifts in order to create a mutation
class SwapPair(
	val first: Assignment,
	val second: Assignment
){

	val firstShiftType get() = first.containingShift.type
	val secondShiftType get() = second.containingShift.type

	companion object {

		// all pairs of employees assigned to different shifts on given day
		fun allPairs(day: ScheduleDay): Sequence<SwapPair>{

			return sequence {

				for(employeeA in Scheduler.staff)
					for(employeeB in Scheduler.staff){
						if(employeeA == employeeB)
							continue
						val typeA = day.getAssignedShiftType(employeeA)
						val typeB = day.getAssignedShiftType(employeeB)
						if(typeA == typeB)
							continue
						yield(
							SwapPair(
							Assignment(employeeA, TypedStaffMap(typeA, day[typeA])),
							Assignment(employeeB, TypedStaffMap(typeB, day[typeB]))
							)
						)
					}
			}
		}
	}

	class Assignment(
		val employee: Employee,
		val containingShift: TypedStaffMap
	)
}


data class TypedStaffMap(val type: ShiftType, val staff: StaffMap) {

	fun swapEmployees(toAdd: Employee, toRemove: Employee) =
		TypedStaffMap(type, staff.swapEmployees(toAdd, toRemove))
}