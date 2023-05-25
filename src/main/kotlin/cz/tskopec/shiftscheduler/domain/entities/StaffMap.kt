package cz.tskopec.shiftscheduler.domain.entities

import cz.tskopec.shiftscheduler.domain.Scheduler
import cz.tskopec.shiftscheduler.domain.entities.Schedule.Companion.MAX_STAFF_SIZE
/*
Describes set of employees present on a given shift. Integer value works as a bitmap, where all 1-bits signify
presence of an employee with employee index corresponding to the position of the given bit.
 */
@JvmInline
value class StaffMap(
	val bits: Int
) {

	val employeeCount get() = bits.countOneBits()

	constructor(staff: Collection<Employee>): this(staff.fold(0) { map, emp -> map or emp.maskBit })

	constructor(indices: Set<Int>): this(
		indices
			.filter { it in 0 until MAX_STAFF_SIZE }
			.fold(0) { bits, i -> bits or (1 shl i) }
	)

	operator fun plus(other: StaffMap) = StaffMap(this.bits or other.bits)

	operator fun plus(employee: Employee) = StaffMap(this.bits or employee.maskBit)

	operator fun plus(maps: Collection<StaffMap>) = plus(maps.reduce{ m1, m2 -> m1 + m2 })

	operator fun minus(other: StaffMap) = StaffMap((this.bits and other.bits) xor this.bits)

	operator fun minus(employee: Employee) = StaffMap((this.bits and employee.maskBit) xor this.bits)

	operator fun minus(maps: Collection<StaffMap>) = minus(maps.reduce(StaffMap::plus))

	fun swapEmployees(toAdd: Employee, toRemove: Employee): StaffMap {
		val afterAdd = this.bits or toAdd.maskBit
		return StaffMap((afterAdd and toRemove.maskBit) xor afterAdd)
	}

	operator fun contains(employee: Employee): Boolean = this.bits and employee.maskBit == employee.maskBit

	operator fun contains(other: StaffMap): Boolean = this.bits or other.bits == this.bits

	fun conflictsWith(other: StaffMap): Boolean = this.bits and other.bits != 0

	fun commonStaff(other: StaffMap): StaffMap = StaffMap(this.bits and other.bits)

	fun commonBits(other: Int): Int = this.bits and other

	override fun toString(): String = Integer.toBinaryString(bits).padStart(Scheduler.staffSize, '0')


	fun toIndices(): Set<Int> {

		val indices = mutableSetOf<Int>()
		var remainingBits = this.bits
		while(remainingBits > 0){
			indices += remainingBits.countTrailingZeroBits()
			remainingBits -= remainingBits.takeLowestOneBit()
		}
		return indices
	}
}

object StaffMaps {

	val EMPTY = StaffMap(0)
	fun fullStaff(staffSize: Int = Scheduler.staffSize) = StaffMap((1 shl staffSize) - 1)
}
