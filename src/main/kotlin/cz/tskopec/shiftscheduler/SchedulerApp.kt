package cz.tskopec.shiftscheduler

import cz.tskopec.shiftscheduler.ui.startup.MainMenu
import javafx.application.Application
import javafx.stage.Stage


class SchedulerApp : Application() {

	override fun start(stage: Stage) {

		MainMenu(stage).show()
	}

}

fun main(args: Array<String>) {
	Application.launch(SchedulerApp::class.java)
}
