package cz.tskopec.shiftscheduler.ui.build

import cz.tskopec.shiftscheduler.ui.handlers.KeyHandler
import cz.tskopec.shiftscheduler.ui.handlers.SelectionBoxController
import cz.tskopec.shiftscheduler.ui.viewmodel.SchedulerModel
import cz.tskopec.shiftscheduler.ui.components.*
import javafx.scene.input.KeyEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority.ALWAYS
import javafx.scene.layout.Priority.NEVER
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.util.Builder



class ViewBuilder(
	private val schedulerModel: SchedulerModel
) : Builder<Region> {

	private val glassPane = AnchorPane().apply { mouseTransparentProperty().set(true) }
	private val selectionBoxCtl = SelectionBoxController(glassPane)

	override fun build(): Region {


		val appPane = GridPane().apply {
			columnConstraints += growingConstraints(NEVER, ALWAYS, ALWAYS)
			rowConstraints += growingConstraints(NEVER, ALWAYS, NEVER)

			addColumn(0,
				dateYearLabel(schedulerModel),
				employeePanel(schedulerModel.staffNames)
			)
			addColumn(1,
				datesPanel(schedulerModel.dateCellsModel, selectionBoxCtl),
				planViews(schedulerModel, selectionBoxCtl),
				dowPanel(schedulerModel.validatorModel)
			)
			addColumn(2,
				actionButtons(),
				statsViews(schedulerModel),
				viewButtons()
			)
		}.apply{ styleClass += "background"}

		val result = StackPane(appPane, glassPane)
		result.addEventHandler(KeyEvent.KEY_TYPED, KeyHandler())

		return result
	}

}