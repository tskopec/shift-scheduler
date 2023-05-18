package cz.tskopec.shiftscheduler.control.commands

sealed class EditScheduleCommand<T>(
	protected val editedList: MutableList<T>,
	selectedIndices: List<Int>
): UndoableCommand  {

	protected val editedIndices: List<Int> = selectedIndices.distinct().sorted()
	protected val oldValues: List<IndexedValue<T>> = editedIndices.map { IndexedValue(it, editedList[it]) }


	override fun hadEffect(): Boolean {

		return oldValues.any{ (index, origValue) ->
			editedList[index] != origValue
		}
	}
}
