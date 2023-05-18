package cz.tskopec.shiftscheduler.ui.handlers

import cz.tskopec.shiftscheduler.ui.viewmodel.plan.PlanViewModel
import javafx.event.EventHandler
import javafx.geometry.Bounds
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.layout.GridPane

// selects nodes within cellGrid by selection box bounds and updates selected status of corresponding cells in the viewModel
class CellSelectionHandler(
	private val cellGrid: GridPane,
	private val boxController: SelectionBoxController,
	private val viewModel: PlanViewModel<*>
): EventHandler<MouseEvent>{

	private var alreadySelected: List<Node> = emptyList()

	override fun handle(e: MouseEvent?) {

		e?.let {
			when (e.eventType) {
				MouseEvent.MOUSE_PRESSED -> {
					if (!e.isControlDown)
						cellGrid.children.forEach { it.setSelected(false) }
					alreadySelected = cellGrid.children.filter{ it.isSelected() }
				}
				MouseEvent.MOUSE_DRAGGED -> {
					if (!e.isStillSincePress) {
						boxController.updateBox(e.sceneX, e.sceneY, cellGrid.localToScene(cellGrid.boundsInLocal))
						val selectionBounds = boxController.getSelectionBounds()
						cellGrid.children
							.filter { !alreadySelected.contains(it) }
							.forEach { it.updateSelectedStatus(selectionBounds) }
					}
				}
				MouseEvent.MOUSE_RELEASED -> {
					if (!e.isStillSincePress)
						boxController.discardBox()
				}
				MouseEvent.MOUSE_CLICKED -> {
					if (e.isStillSincePress) {
						cellGrid.children
							.filter { e in it }
							.forEach { it.toggleSelection() }
					}
				}
			}
		}
	}
	private fun Node.isSelected(): Boolean
			= viewModel.isSelected(GridPane.getColumnIndex(this), GridPane.getRowIndex(this))

	private fun Node.setSelected(value: Boolean)
			= viewModel.setSelected(GridPane.getColumnIndex(this), GridPane.getRowIndex(this), value)

	private fun Node.toggleSelection()
			= setSelected(!isSelected())

	private fun Node.updateSelectedStatus(selectionBounds: Bounds)
			= setSelected(localToScene(boundsInLocal).intersects(selectionBounds))

	private operator fun Node.contains(e: MouseEvent): Boolean
			= boundsInParent.contains(e.x, e.y)
}
