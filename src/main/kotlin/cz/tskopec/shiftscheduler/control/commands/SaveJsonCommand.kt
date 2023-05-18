package cz.tskopec.shiftscheduler.control.commands

import cz.tskopec.shiftscheduler.domain.entities.serial.JsonInstance
import cz.tskopec.shiftscheduler.domain.entities.serial.ScheduleData
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.Modality
import javafx.stage.Stage
import kotlinx.serialization.encodeToString


class SaveJsonCommand: Command {

	override fun execute() {

		val chooserWindow = Stage().apply { initModality(Modality.APPLICATION_MODAL) }
		val file = FileChooser().run {
			extensionFilters += ExtensionFilter("json", "*.json")
			showSaveDialog(chooserWindow)
		}
		val data = ScheduleData.createFromCurrentState()
		file.writeText(JsonInstance.encodeToString(data))
	}
}