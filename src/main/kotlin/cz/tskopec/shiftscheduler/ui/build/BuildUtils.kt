package cz.tskopec.shiftscheduler.ui.build

import cz.tskopec.shiftscheduler.ui.ViewMode
import cz.tskopec.shiftscheduler.ui.handlers.CellSelectionHandler
import cz.tskopec.shiftscheduler.ui.handlers.SelectionBoxController
import cz.tskopec.shiftscheduler.ui.viewmodel.plan.PlanViewColumn
import cz.tskopec.shiftscheduler.ui.viewmodel.plan.PlanViewModel
import javafx.beans.binding.Bindings
import javafx.beans.property.IntegerProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.value.ObservableBooleanValue
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.effect.Effect
import javafx.scene.effect.Glow
import javafx.scene.effect.InnerShadow
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color


inline fun <reified T : ConstraintsBase> growingConstraints(vararg priorities: Priority): List<T> {

	return priorities.map {
		when (T::class) {
			ColumnConstraints::class -> ColumnConstraints().apply { hgrow = it } as T
			RowConstraints::class -> RowConstraints().apply { vgrow = it } as T
			else -> throw IllegalArgumentException("Unknown constraints type")
		}
	}
}

inline fun <reified T : ConstraintsBase> fixedConstraints(count: Int): List<T> {

	return (0 until count).map {
		when (T::class) {
			ColumnConstraints::class -> ColumnConstraints().apply { percentWidth = 100.0 / count } as T
			RowConstraints::class -> RowConstraints().apply { percentHeight = 100.0 / count } as T
			else -> throw IllegalArgumentException("Unknown constraints type")
		}
	}
}


inline fun <reified T : ConstraintsBase> boundConstraints(
	allSizes: Collection<IntegerProperty>,
	boundSize: IntegerProperty
): T {

	val binding = Bindings.createDoubleBinding(
		{ 100.0 / (allSizes.sumOf { it.value } / boundSize.value.toDouble()) },
		*allSizes.toTypedArray()
	)
	return when (T::class) {
		ColumnConstraints::class -> ColumnConstraints().apply { percentWidthProperty().bind(binding) } as T
		RowConstraints::class -> RowConstraints().apply { percentHeightProperty().bind(binding) } as T
		else -> throw IllegalArgumentException("Unknown constraints type")
	}
}


fun GridPane.withFixedConstraints(column: Int, row: Int) = apply {
	columnConstraints += fixedConstraints(column)
	rowConstraints += fixedConstraints(row)
}


fun <T : Region> T.stretchable() = apply {
	maxHeight = Double.MAX_VALUE
	maxWidth = Double.MAX_VALUE
}

fun baseLabel(text: String = "", style: String?) = Label(text)
	.stretchable()
	.apply { style?.let { styleClass += it } }



fun basicCellLabel() = baseLabel(style = "plan-cell")


fun selectableCellLabel(cell: PlanViewColumn.Cell<*>) = basicCellLabel().showingSelectedStatus(cell.selectedProp)




fun <T : GridPane> T.withSelectionHandler(boxCtl: SelectionBoxController, viewModel: PlanViewModel<*>) = apply {
	addEventHandler(MouseEvent.ANY, CellSelectionHandler(this, boxCtl, viewModel))
}

fun <T : Node> T.boundToViewMode(mode: ViewMode, observedProperty: ObjectProperty<ViewMode>) = apply {
	visibleProperty().bind(Bindings.createBooleanBinding({ observedProperty.value == mode }, observedProperty))
}

fun <T : Node> T.showEffectsOnProp(observedVal: ObservableBooleanValue, onTrue: Effect? = null, onFalse: Effect? = null) = apply {
	effectProperty().bind(Bindings.createObjectBinding({
		if (observedVal.value) onTrue else onFalse
	}, observedVal))
}

fun <T : Node> T.showingSelectedStatus(observedVal: ObservableBooleanValue) = showEffectsOnProp(
	observedVal,
	onTrue = Effects.selected
)

fun <T : Node> T.showingErrorStatus(observedVal: ObservableBooleanValue) = showEffectsOnProp(
	observedVal,
	onTrue = Effects.error,
	onFalse = Effects.valid
)


object Effects {
	val selected = Glow(1.0)
	val error = InnerShadow(20.0, Color.RED)
	val valid = InnerShadow(20.0, Color.GREEN)
}
