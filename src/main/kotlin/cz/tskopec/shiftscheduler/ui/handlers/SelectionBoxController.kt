package cz.tskopec.shiftscheduler.ui.handlers

import javafx.geometry.BoundingBox
import javafx.geometry.Bounds
import javafx.geometry.Point2D
import javafx.scene.layout.AnchorPane
import javafx.scene.shape.Rectangle
import kotlin.math.abs

class SelectionBoxController(private val glassPane: AnchorPane) {

	private var selectionOrigin: Point2D? = null
	private var selectionBox: Rectangle? = null


	fun updateBox(currentMouseX: Double, currentMouseY: Double, allocatedArea: Bounds) {

		val currentPosition = Point2D(
			minOf(allocatedArea.maxX, maxOf(allocatedArea.minX, currentMouseX)),
			minOf(allocatedArea.maxY, maxOf(allocatedArea.minY, currentMouseY))
		)

		if (selectionOrigin == null)
			selectionOrigin = currentPosition
		else selectionOrigin?.let { origin ->
			glassPane.children.clear()
			selectionBox = createRectangle(origin, currentPosition)
			glassPane.children += selectionBox
		}

	}

	fun discardBox() {

		selectionOrigin = null
		selectionBox = null
		glassPane.children.clear()
	}

	fun getSelectionBounds(): Bounds = selectionBox?.boundsInLocal ?: BoundingBox(0.0, 0.0, 0.0, 0.0)

	private fun createRectangle(p: Point2D, q: Point2D): Rectangle =
		Rectangle(minOf(p.x, q.x), minOf(p.y, q.y), abs(p.x - q.x), abs(p.y - q.y))
			.apply { styleClass += "selection-box" }
}