package cz.tskopec.shiftscheduler.domain.builder.generator

import cz.tskopec.shiftscheduler.domain.entities.ScheduleDay

sealed class GeneratorResult {

	class Success(lastDay: GeneratorDay): GeneratorResult(){

		val plan = mutableListOf<ScheduleDay>().apply {
			var currentDay = lastDay
			while(currentDay.previousDay != null){
				this += currentDay.toScheduleDay()
				currentDay = currentDay.previousDay ?: break
			}
			reverse()
		}
	}

	object DeadEnd: GeneratorResult()

}
