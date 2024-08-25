/**
 * 
 */
package org.weasis.core.ui.model.graphic.imp.vet;

import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.swing.Icon;

import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.GeomUtil;
import org.weasis.core.api.image.measure.MeasurementsAdapter;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.model.graphic.AbstractDragGraphic;
import org.weasis.core.ui.model.utils.bean.AdvancedShape;
import org.weasis.core.ui.model.utils.bean.MeasureItem;
import org.weasis.core.ui.model.utils.bean.Measurement;
import org.weasis.core.ui.model.utils.exceptions.InvalidShapeException;
import org.weasis.core.ui.util.MouseEventDouble;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * https://de.wikipedia.org/wiki/TPLO Punkt 1-2
 * 
 * @author t68
 */
@XmlType(name = "vertebralHeartScore")
@XmlRootElement(name = "vertebralHeartScore")
public class TibialPlateauLevelingOsteotomy extends AbstractDragGraphic {

	public static final Integer POINTS_NUMBER = 9;
	public static final Icon ICON = ResourceUtil.getIcon("svg/action/tplo.svg").derive(22, 22);

	private static final int MaxSawRadius = 50;
	private static final int MinSawRadius = 5;

	protected static final List<Measurement> MEASUREMENT_LIST = List.of(new Measurement("Radial Saw Radius", 1, true, true, true), new Measurement("Tibial Plateau Angle", 1, true, true, true));

	protected Point2D[] plateauPts = new Point2D[2]; // plateau points
	protected Point2D[] centerPt = new Point2D[3];
	protected Double[] radiusPt = new Double[3];
	protected Double tpa = null;
	protected Integer radius = null;

	public TibialPlateauLevelingOsteotomy() {
		super(POINTS_NUMBER);
	}

	public TibialPlateauLevelingOsteotomy(TibialPlateauLevelingOsteotomy graphic) {
		super(graphic);
	}

	@Override
	public TibialPlateauLevelingOsteotomy copy() {
		return new TibialPlateauLevelingOsteotomy(this);
	}

	@Override
	public Icon getIcon() {
		return ICON;
	}

	@Override
	public String getUIName() {
		return "Tibial Plateau Leveling Osteotomy";
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
		AdvancedShape aShape = new AdvancedShape(this, 5);
		if (Objects.nonNull(plateauPts[0]) && Objects.nonNull(plateauPts[1]))
			aShape.addShape(new Line2D.Double(plateauPts[0], plateauPts[1]));
		for (int i = 0; i < 2; i++) {
			if (Objects.nonNull(centerPt[i]) && Objects.nonNull(radiusPt[i]) && !Objects.equals(radiusPt[i], 0d)) {
				aShape.addShape(new Ellipse2D.Double(centerPt[i].getX() - radiusPt[i], centerPt[i].getY() - radiusPt[i], 2 * radiusPt[i], 2 * radiusPt[i]), getDashStroke(1.0f), true);
			}
		}
		if (Objects.nonNull(centerPt[0]) && Objects.nonNull(centerPt[1])) {
			aShape.addShape(new Line2D.Double(centerPt[0].getX(), centerPt[0].getY(), centerPt[1].getX(), centerPt[1].getY()));
		}
		if (Objects.nonNull(centerPt[2]) && Objects.nonNull(radiusPt[2]) && !Objects.equals(radiusPt[2], 0d)) {
//			aShape.addShape(new Ellipse2D.Double(centerPt[2].getX() - radiusPt[2], centerPt[2].getY() - radiusPt[2], 2 * radiusPt[2], 2 * radiusPt[2]));
			Rectangle2D arcAngleBounds = new Rectangle2D.Double(centerPt[2].getX() - radiusPt[2], centerPt[2].getY() - radiusPt[2], 2 * radiusPt[2], 2 * radiusPt[2]);
			System.out.println(tpa);
			Shape arcAngle = new Arc2D.Double(arcAngleBounds, 180d - tpa, 120d, Arc2D.OPEN);
			aShape.addShape(arcAngle, getDashStroke(2.0f), true);
			arcAngle = new Arc2D.Double(arcAngleBounds, 300 - tpa, 240d, Arc2D.OPEN);
			aShape.addShape(arcAngle);
		}
		setShape(aShape, mouseEvent);
		updateLabel(mouseEvent, getDefaultView2d(mouseEvent));
	}

	protected void updateTool(MouseEventDouble mouseEvent) {
		for (int i = 0; i < 2; i++)
			plateauPts[i] = getHandlePoint(i);
		if (Objects.nonNull(plateauPts[0]) && Objects.nonNull(plateauPts[1])) {
			tpa = Math.abs(GeomUtil.getAngleDeg(plateauPts[0], plateauPts[1]));
			if (tpa > 90)
				tpa = 180 - tpa;
		}

		Point2D[] ptA = new Point2D[2];
		int[] circlePointNumbers = { 5, 8 };
		for (int i = 0; i < 2; i++) {
			ptA[i] = getHandlePoint(2 + i * 3);
			if (pts.size() >= circlePointNumbers[i]) {
				centerPt[i] = GeomUtil.getCircleCenter(List.of(pts.get(2 + i * 3), pts.get(3 + i * 3), pts.get(4 + i * 3)));
				radiusPt[i] = (centerPt[i] != null && ptA[i] != null) ? centerPt[i].distance(ptA[i]) : 0;
			}
		}
		if (Objects.nonNull(centerPt[0]) && Objects.nonNull(centerPt[1])) {
			centerPt[2] = GeomUtil.getIntersectPoint(plateauPts[0], plateauPts[1], centerPt[0], centerPt[1]);
		}
		if (pts.size() == POINTS_NUMBER) {
			ViewCanvas<?> view2d = getDefaultView2d(mouseEvent);
			Unit displayUnit = view2d == null ? null : (Unit) view2d.getActionValue(ActionW.SPATIAL_UNIT.cmd());
			MeasurementsAdapter ma = view2d.getMeasurableLayer().getMeasurementAdapter(displayUnit);
			Point2D p = getHandlePoint(8);
			double temp = centerPt[2].distance(p) * ma.getCalibRatio();
			if (temp < MaxSawRadius && temp > MinSawRadius) {
				radius = (int) temp;
				radiusPt[2] = radius / ma.getCalibRatio();
			}
		}
	}

	@Override
	public List<MeasureItem> computeMeasurements(MeasurableLayer layer, boolean releaseEvent, Unit displayUnit) {
		if ((layer != null && layer.hasContent() && isShapeValid()) && layer.getMeasurementAdapter(displayUnit) != null) {
			MeasurementsAdapter ma = layer.getMeasurementAdapter(displayUnit);
			ArrayList<MeasureItem> measVal = new ArrayList<>();
			if (radius != null)
				measVal.add(new MeasureItem(MEASUREMENT_LIST.get(0), radius, ma.getUnit()));
			if (tpa != null)
				measVal.add(new MeasureItem(MEASUREMENT_LIST.get(1), tpa, "°"));
			return measVal;
		}
		return Collections.emptyList();
	}

	@Override
	public List<Measurement> getMeasurementList() {
		return MEASUREMENT_LIST;
	}

}
