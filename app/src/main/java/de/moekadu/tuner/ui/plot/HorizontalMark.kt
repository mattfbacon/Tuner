package de.moekadu.tuner.ui.plot

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import de.moekadu.tuner.ui.theme.TunerTheme
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


/** Helper function to compute height of text labels.
 * @param style Text style.
 * @param density Density of measurement environment.
 * @param paddingTop Padding above text.
 * @param paddingBottom Padding below text.
 * @param textMeasurer Text measurer which is used to measure the text.
 * @return Label height in px.
 */
@Composable
fun rememberTextLabelHeight(
    style: TextStyle = LocalTextStyle.current,
    density: Density = LocalDensity.current,
    paddingTop: Dp = 0.dp,
    paddingBottom: Dp = 0.dp,
    textMeasurer: TextMeasurer = rememberTextMeasurer()
): Float {
    return remember(textMeasurer, density, paddingTop, paddingBottom, style) {
        with(density) {
            (textMeasurer.measure("X", style = style, density = density).size.height
                    + paddingTop.toPx()
                    + paddingBottom.toPx())
        }
    }
}

class HorizontalMarks(
    private val label: (@Composable (modifier: Modifier, level: Int, index: Int, y: Float) -> Unit)?,
    private val markLevel: MarkLevel,
    private val maxLabelHeight: Density.() -> Float,
    private val anchor: Anchor = Anchor.Center,
    private val horizontalLabelPosition: Float = 0.5f,
    private val lineWidth: Dp = 1.dp,
    private val lineColor: @Composable () -> Color = {  Color.Unspecified },
    private val clipLabelToPlotWindow: Boolean = false,
    private val maxNumLabels: Int = -1,
    private val screenOffset: DpOffset = DpOffset.Zero
): PlotGroup {
    private data class MeasuredMark(
        val position: MarkLayoutData,
        val placeable: Placeable
    )

    data class MarkLayoutData(val position: Float):
        ParentDataModifier {
        override fun Density.modifyParentData(parentData: Any?) = this@MarkLayoutData
    }

    @Composable
    override fun Draw(transformation: Transformation) {
        val density = LocalDensity.current
        val lineWidthPx = with(density) { lineWidth.toPx() }
        val screenOffsetPx  = with(density) { screenOffset.y.toPx() }
        val maxLabelHeightPx = density.maxLabelHeight()
        val maxNumLabelsResolved = if (maxNumLabels <= 0)
            (transformation.viewPortScreen.height / maxLabelHeightPx / 2f).roundToInt()
        else
            maxNumLabels
        val range = remember(transformation, maxNumLabelsResolved, markLevel, maxLabelHeightPx, lineWidthPx, screenOffsetPx) {
            val labelHeightScreen = Rect(
                0f,
                0f,
                1f,
                maxLabelHeightPx + 0.5f * lineWidthPx + screenOffsetPx.absoluteValue
            )

            val labelHeightRaw = transformation.toRaw(labelHeightScreen).height
            markLevel.getMarksRange(
                transformation.viewPortRaw.bottom - labelHeightRaw,
                transformation.viewPortRaw.top + labelHeightRaw,
                maxNumLabelsResolved,
                labelHeightRaw
            )
        }
        Box(Modifier.fillMaxSize()) {
            val lineColor = lineColor().takeOrElse { MaterialTheme.colorScheme.outline }
            val clipShape = transformation.rememberClipShape()
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(clipShape)
            ) {
                for (i in range.indexBegin until range.indexEnd) {
                    val yOffset = Offset(0f, markLevel.getMarkValue(range.level, i))
                    val yTransformed = transformation.toScreen(yOffset).y
                    drawLine(
                        lineColor,
                        Offset(transformation.viewPortScreen.left.toFloat(), yTransformed),
                        Offset(transformation.viewPortScreen.right.toFloat(), yTransformed),
                        strokeWidth = lineWidth.toPx()
                    )
                }
            }

            Layout(
                content = {
                    label?.let { l ->
                        for (i in range.indexBegin until range.indexEnd) {
                            val y = markLevel.getMarkValue(range.level, i)
                            l(MarkLayoutData(y), range.level, i, y)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (clipLabelToPlotWindow) Modifier.clip(clipShape) else Modifier)
            ) { measureables, constraints ->
                val c = constraints.copy(minWidth = 0, minHeight = 0)
                val placeables = measureables.map {
                    MeasuredMark(
                        it.parentData as MarkLayoutData,
                        it.measure(c)
                    )
                }

                layout(constraints.maxWidth, constraints.maxHeight) {
                    placeables.forEach {
                        val p = it.placeable
                        val yOffset = Offset(0f, it.position.position)
                        val yTransformed = transformation.toScreen(yOffset).y

                        val vp = transformation.viewPortScreen
                        val x = vp.left + horizontalLabelPosition * vp.width + screenOffset.x.toPx()
                        val y = yTransformed + screenOffset.y.toPx()
                        val w = p.width
                        val h = p.height
                        val l2 = 0.5f * lineWidth.toPx()

                        when (anchor) {
                            Anchor.NorthWest -> p.place(x.roundToInt(), (y + l2).roundToInt())
                            Anchor.North -> p.place(
                                (x - 0.5 * w).roundToInt(),
                                (y + l2).roundToInt()
                            )

                            Anchor.NorthEast -> p.place(
                                (x - w).roundToInt(),
                                (y + l2).roundToInt()
                            )

                            Anchor.West -> p.place(x.roundToInt(), (y - 0.5f * h).roundToInt())
                            Anchor.Center -> p.place(
                                (x - 0.5f * w).roundToInt(),
                                (y - 0.5f * h).roundToInt()
                            )

                            Anchor.East -> p.place(
                                (x - w).roundToInt(),
                                (y - 0.5f * h).roundToInt()
                            )

                            Anchor.SouthWest -> p.place(
                                x.roundToInt(),
                                (y - h - l2).roundToInt()
                            )

                            Anchor.South -> p.place(
                                (x - 0.5 * w).roundToInt(),
                                (y - h - l2).roundToInt()
                            )

                            Anchor.SouthEast -> p.place(
                                (x - w).roundToInt(),
                                (y - h - l2).roundToInt()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberTransformation(
    screenWidth: Dp, screenHeight: Dp,
    viewPortRaw: Rect
): Transformation {
    val widthPx = with(LocalDensity.current) { screenWidth.roundToPx() }
    val heightPx = with(LocalDensity.current) { screenHeight.roundToPx() }

    val transformation = remember(widthPx, heightPx, viewPortRaw) {
        Transformation(IntRect(0, 0, widthPx, heightPx), viewPortRaw)
    }
    return transformation
}

@Preview(widthDp = 200, heightDp = 400, showBackground = true)
@Composable
private fun HorizontalMarksPreview() {
    TunerTheme {
        BoxWithConstraints {
            val transformation = rememberTransformation(
                screenWidth = maxWidth,
                screenHeight = maxHeight,
                viewPortRaw = Rect(-10f, 5f, 10f, -5f)
            )
            val textLabelHeight = rememberTextLabelHeight()

            val marks = HorizontalMarks(
                label = { m, l, i, y -> Text("$l, $i, $y", modifier = m.background(Color.Magenta))},
                anchor = Anchor.South,
                markLevel = MarkLevelExplicitRanges(
                    listOf(
                        floatArrayOf(-3f, -2f, 0f, 4f),
                        floatArrayOf(-3f, -2f, -1f, 0f, 2f, 4f),
                        floatArrayOf(-3f, -2.5f, -2f, -1.5f, -1f, 0f, 1f, 2f, 3f, 4f),
                    ).toImmutableList()
                ),
                maxLabelHeight = { textLabelHeight },
                screenOffset = DpOffset(0.dp, (-1).dp)
            )
            
            marks.Draw(transformation = transformation)
        }
    }
}