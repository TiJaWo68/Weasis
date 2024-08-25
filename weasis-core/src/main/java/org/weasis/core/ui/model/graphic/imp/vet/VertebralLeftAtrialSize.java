/**
 * 
 */
package org.weasis.core.ui.model.graphic.imp.vet;

import java.awt.Color;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;

import org.weasis.core.api.gui.util.GeomUtil;
import org.weasis.core.api.image.measure.MeasurementsAdapter;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.model.graphic.AbstractDragGraphic;
import org.weasis.core.ui.model.utils.bean.AdvancedShape;
import org.weasis.core.ui.model.utils.bean.AdvancedShape.BasicShape;
import org.weasis.core.ui.model.utils.bean.MeasureItem;
import org.weasis.core.ui.model.utils.bean.Measurement;
import org.weasis.core.ui.model.utils.exceptions.InvalidShapeException;
import org.weasis.core.ui.util.MouseEventDouble;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * erste Strecke: 5 Wirbel ausmessen, beginnend beim 4. (Th4 - Th8) <br>
 * zweite Strecke: Linie von der Bifurkation der Trachea bis zu der Stelle an
 * der der kaudale Rand des linken Vorhofes die dorsale Grenze der Vena cava
 * caudalis kreuzt <br>
 * dritte Strecke: parallel zur ersten Strecke (nicht editierbar), gleiche Länge
 * wie Strecke 2<br>
 * Messung 1: Average Vertebra - Größe eines Wirbels in mm<br>
 * Messung 2: VLAS: Länge der zweiten Strecke in Anzahl von Wirbeln ausgedrückt
 * (1 Kommastelle)
 * 
 * @author t68
 */
@XmlType(name = "vertebralLeftAtrialSize")
@XmlRootElement(name = "vertebralLeftAtrialSize")
public class VertebralLeftAtrialSize extends AbstractDragGraphic {

	private static final String VLAS = "Vertebral Left Atrial Size";
	private static final int Vertebra_Offset = 20;
	public static final Integer POINTS_NUMBER = 4;
	public static final Icon ICON = ResourceUtil.getIcon("svg/action/vlas.svg").derive(22, 22);

	protected static final List<Measurement> MEASUREMENT_LIST = List.of(new Measurement(VLAS, 1, true, true, true),
			new Measurement("Average Vertebra", 1, true, true, true));

	protected Point2D[] points = new Point2D[POINTS_NUMBER];

	// estimate if line segments are valid or not
	protected boolean[] linesValid = new boolean[2];
	private static final Color[] LINE_COLORs = { null, Color.CYAN, Color.PINK };

	public VertebralLeftAtrialSize() {
		super(POINTS_NUMBER);
	}

	public VertebralLeftAtrialSize(VertebralLeftAtrialSize graphic) {
		super(graphic);
	}

	@Override
	public VertebralLeftAtrialSize copy() {
		return new VertebralLeftAtrialSize(this);
	}

	@Override
	public Icon getIcon() {
		return ICON;
	}

	@Override
	public String getUIName() {
		return VLAS;
	}

	@Override
	protected void prepareShape() throws InvalidShapeException {
		if (!isShapeValid()) {
			throw new InvalidShapeException("This shape cannot be drawn");
		}
		buildShape(null);
	}

	@Override
	public void buildShape(MouseEventDouble mouseEvent) {
		updateTool(mouseEvent);

		Shape newShape = null;
		Path2D path = new Path2D.Double(Path2D.WIND_NON_ZERO, 5);

		if (points[0] != null && points[1] != null) {
			newShape = new AdvancedShape(this, 3);
			AdvancedShape aShape = (AdvancedShape) newShape;
			aShape.addShape(path);

			for (int i = 0; i < linesValid.length; i++) {
				if (linesValid[i]) {
					Line2D.Double line = new Line2D.Double(points[i * 2], points[i * 2 + 1]);
					path.append(line, false);
					BasicShape bs = aShape.addShape(line);
					if (LINE_COLORs[i] != null)
						bs.setColorPaint(LINE_COLORs[i]);
				}
			}

			double x1 = points[0].getX();
			double y1 = points[0].getY();
			double x2 = points[1].getX();
			double y2 = points[1].getY();
			double a = x1 == x2 ? 0 : (y1 - y2) / (x1 - x2);
			double b = y1 - a * x1;
			double distance1 = new Point2D.Double(x1, y1).distance(new Point2D.Double(0, b));
			double sina = Math.abs((b - y1) / distance1);

			int i = 0;
			if (linesValid[i + 1]) {
				double heartDistance = points[2 + i * 2].distance(points[3 + i * 2]);
				double offsetY = Vertebra_Offset * (i + 1) * (1 - sina);
				double offsetX = Vertebra_Offset * (i + 1) * sina;
				Point2D p3 = GeomUtil.getColinearPointWithLength(points[0], points[1], heartDistance);
				Point2D p1 = new Point2D.Double(points[0].getX() + offsetX, points[0].getY() + offsetY);
				Point2D p2 = new Point2D.Double(p3.getX() + offsetX, p3.getY() + offsetY);
				BasicShape bs = aShape.addShape(new Line2D.Double(p1, p2), getDashStroke(2.0f), true);
				if (LINE_COLORs[i + 1] != null)
					bs.setColorPaint(LINE_COLORs[i + 1]);
			}
		} else if (path.getCurrentPoint() != null) {
			newShape = path;
		}

		setShape(newShape, mouseEvent);
		updateLabel(mouseEvent, getDefaultView2d(mouseEvent));
	}

	protected void updateTool(MouseEventDouble me) {
		boolean releasedEvent = me.getID() == MouseEvent.MOUSE_RELEASED;
		for (int i = 0; i < POINTS_NUMBER; i++) {
			points[i] = getHandlePoint(i);
		}
		for (int i = 0; i < linesValid.length; i++) {
			linesValid[i] = points[2 * i] != null && points[2 * i + 1] != null
					&& !points[2 * i].equals(points[2 * i + 1]);
		}
		if (linesValid[1]) {
			int indexMoved = points[2].getX() == me.getImageX() && points[2].getY() == me.getImageY() ? 2
					: points[3].getX() == me.getImageX() && points[3].getY() == me.getImageY() ? 3 : -1;
			if (indexMoved != -1) {
				int otherIndex = indexMoved == 2 ? 3 : 2;
				if (releasedEvent) {
					pts.remove(otherIndex);
					pts.add(otherIndex, points[otherIndex]);
				}
			}
		}
	}

	@Override
	public List<Measurement> getMeasurementList() {
		return MEASUREMENT_LIST;
	}

	@Override
	public List<MeasureItem> computeMeasurements(MeasurableLayer layer, boolean releaseEvent, Unit displayUnit) {
		if (layer != null && layer.hasContent() && isShapeValid()) {
			MeasurementsAdapter adapter = layer.getMeasurementAdapter(displayUnit);

			double ratio = adapter.getCalibRatio();
			String unitStr = adapter.getUnit();

			if (adapter != null) {
				double averageVertebra = points[0].distance(points[1]) / 5;
				double tracheaToVenaCavaCaudalisLine = points[2].distance(points[3]);
				double vlas = tracheaToVenaCavaCaudalisLine / averageVertebra;

				List<MeasureItem> measVal = new Vector<>();
				measVal.add(new MeasureItem(MEASUREMENT_LIST.get(0), vlas, null));
				measVal.add(new MeasureItem(MEASUREMENT_LIST.get(1), averageVertebra * ratio, unitStr));
				return measVal;
			}
		}
		return Collections.emptyList();
	}
}
