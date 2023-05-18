package cz.tskopec.shiftscheduler.control.commands


interface Command {

	fun execute()
}

interface UndoableCommand: Command {

	fun undo()
	fun hadEffect(): Boolean = true
}
