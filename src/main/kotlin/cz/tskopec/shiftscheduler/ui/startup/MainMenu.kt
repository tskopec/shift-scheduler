package cz.tskopec.shiftscheduler.ui.startup

import cz.tskopec.shiftscheduler.control.Controller
import cz.tskopec.shiftscheduler.domain.entities.serial.JsonInstance
import cz.tskopec.shiftscheduler.domain.entities.serial.ScheduleData
import javafx.application.Platform
import javafx.beans.property.StringProperty
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.Modality
import javafx.stage.Stage
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import java.io.File

class MainMenu(private val stage: Stage) : GridPane() {

	companion object Settings {
		private val settingsPathField = TextField("src/main/resources/settings")
		private val rosterPathField = TextField("src/main/resources/rosters/default")

		val settingsPath: String get() = settingsPathField.text
		val rosterPath: String get() = rosterPathField.text
	}


	init {
		padding = Insets(20.0)
		hgap = 10.0
		vgap = 10.0

		addColumn(
			0,
			Button("New schedule").apply { setOnAction { runScheduler() } },
			Button("Load schedule").apply { setOnAction { showFileChooser() } },
			Button("Exit").apply { setOnAction { Platform.exit() } }
		)
		addColumn(
			1,
			Label("Settings file: "),
			Label("Default roster: "),
		)
		addColumn(
			2,
			settingsPathField,
			rosterPathField
		)
		addColumn(
			3,
			findPathButton(settingsPathField.textProperty()),
			findPathButton(rosterPathField.textProperty())
		)
	}

	fun show(){

		val menuScene = Scene(this)
		stage.title = "Scheduler menu"
		stage.scene = menuScene
		stage.show()
	}


	private fun runScheduler() {

		val schedulerScene = Scene(Controller.getView())
		schedulerScene.stylesheets += "style.css"
		stage.title = "Scheduler"
		stage.scene = schedulerScene
	}

	private fun showFileChooser() {

		val file = chooseFile(ExtensionFilter("json", "*.json")) ?: return
		try {
			val data = JsonInstance.decodeFromString<ScheduleData>(file.readText())
			if(!ScheduleInitializer.tryLoadingSchedule(data))
				Alert(Alert.AlertType.ERROR, "File contains error(s), generating blank schedule").show()
			runScheduler()
		} catch (e: SerializationException) {
			Alert(Alert.AlertType.ERROR, "Could not read file", ButtonType.OK).show()
		}
	}

	private fun chooseFile(vararg filters: ExtensionFilter): File? = FileChooser().run {
		val chooserWindow = Stage().apply { initModality(Modality.APPLICATION_MODAL) }
		title = "Choose file"
		extensionFilters += filters
		showOpenDialog(chooserWindow)
	}

	private fun findPathButton(target: StringProperty) = Button("Find").apply {
		setOnAction {
			val file = chooseFile(ExtensionFilter("all", "*.*"))
			file?.let { target.set(it.absolutePath) }
		}
	}


}