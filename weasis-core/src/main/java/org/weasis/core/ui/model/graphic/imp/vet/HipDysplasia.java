package org.weasis.core.ui.model.graphic.imp.vet;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.swing.Icon;

import org.weasis.core.api.gui.util.DecFormatter;
import org.weasis.core.api.gui.util.GeomUtil;
import org.weasis.core.api.image.measure.MeasurementsAdapter;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.model.graphic.AbstractDragGraphic;
import org.weasis.core.ui.model.utils.bean.AdvancedShape;
import org.weasis.core.ui.model.utils.bean.MeasureItem;
import org.weasis.core.ui.model.utils.bean.Measurement;
import org.weasis.core.ui.model.utils.exceptions.InvalidShapeException;
import org.weasis.core.ui.util.MouseEventDouble;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "hipDysplasia")
@XmlRootElement(name = "hipDysplasia")
public class HipDysplasia extends AbstractDragGraphic {

	public static final Integer POINTS_NUMBER = 8;

	public static final Icon ICON = ResourceUtil.getIcon("svg/action/hd_measurement.svg").derive(22, 22);

	protected static final List<Measurement> MEASUREMENT_LIST = new ArrayList<>(
			List.of(new Measurement("Left Angle", 1, true, true, true), new Measurement("Right Angle", 1, true, true, true), new Measurement("Femoral Head Distance", 1, true, true, true)));

	protected Point2D[] centerPt = new Point2D[2]; // Let O be the center of the three point interpolated circle
	protected Double[] radiusPt = new Double[2]; // circle radius
	protected Point2D[] hipPt = new Point2D[2];
	protected Double angleDeg[] = new Double[2];
	protected Double femoralHeadDistance = null;

	public HipDysplasia() {
		super(POINTS_NUMBER);
	}

	public HipDysplasia(HipDysplasia graphic) {
		super(graphic);
	}

	@Override
	public HipDysplasia copy() {
		return new HipDysplasia(this);
	}

	@Override
	public Icon getIcon() {
		return ICON;
	}

	@Override
	public String getUIName() {
		return "Hip dysplasia";
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
		updateTool();
		AdvancedShape aShape = new AdvancedShape(this, 5);

		for (int i = 0; i < 2; i++) {
			if (Objects.nonNull(centerPt[i]) && !Objects.equals(radiusPt[i], 0d)) {
				aShape.addShape(new Ellipse2D.Double(centerPt[i].getX() - radiusPt[i], centerPt[i].getY() - radiusPt[i], 2 * radiusPt[i], 2 * radiusPt[i]));
			}
			if (Objects.nonNull(hipPt[i]))
				aShape.addShape(new Line2D.Double(centerPt[i].getX(), centerPt[i].getY(), hipPt[i].getX(), hipPt[i].getY()));
		}
		if (Objects.nonNull(centerPt[0]) && Objects.nonNull(centerPt[1])) {
			aShape.addShape(new Line2D.Double(centerPt[0].getX(), centerPt[0].getY(), centerPt[1].getX(), centerPt[1].getY()));
		}

		setShape(aShape, mouseEvent);
		updateLabel(mouseEvent, getDefaultView2d(mouseEvent));
	}

	@Override
	public List<MeasureItem> computeMeasurements(MeasurableLayer layer, boolean releaseEvent, Unit displayUnit) {
		if ((layer != null && layer.hasContent() && isShapeValid()) && layer.getMeasurementAdapter(displayUnit) != null) {
			MeasurementsAdapter adapter = layer.getMeasurementAdapter(displayUnit);
			ArrayList<MeasureItem> measVal = new ArrayList<>();
			for (int i = 0; i < 2; i++)
				measVal.add(new MeasureItem(MEASUREMENT_LIST.get(i), angleDeg[i], "°"));
			if (Objects.nonNull(centerPt[0]) && Objects.nonNull(centerPt[1])) {
				femoralHeadDistance = centerPt[0].distance(centerPt[1]) * adapter.getCalibRatio();
				measVal.add(new MeasureItem(MEASUREMENT_LIST.get(2), femoralHeadDistance, adapter.getUnit()));
			}
			return measVal;
		}
		return Collections.emptyList();
	}

	@Override
	public List<Measurement> getMeasurementList() {
		return MEASUREMENT_LIST;
	}

	@Override
	public boolean isShapeValid() {
		updateTool();
		return super.isShapeValid() && pts.size() == POINTS_NUMBER;
	}

	protected void updateTool() {
		Point2D[] ptA = new Point2D[2];
		int[] circlePointNumbers = { 3, 7 };
		for (int i = 0; i < 2; i++) {
			ptA[i] = getHandlePoint(4 * i);
			if (pts.size() >= circlePointNumbers[i]) {
				centerPt[i] = GeomUtil.getCircleCenter(List.of(pts.get(4 * i), pts.get(4 * i + 1), pts.get(4 * i + 2)));
				radiusPt[i] = (centerPt[i] != null && ptA[i] != null) ? centerPt[i].distance(ptA[i]) : 0;
			}
		}
		hipPt[0] = getHandlePoint(3);
		hipPt[1] = getHandlePoint(7);
		for (int i = 0; i < 2; i++) {
			if (Objects.nonNull(centerPt[0]) && Objects.nonNull(centerPt[1]) && Objects.nonNull(hipPt[i])) {
				angleDeg[i] = Math.abs(GeomUtil.getSmallestRotationAngleDeg(GeomUtil.getAngleDeg(hipPt[i], centerPt[i], centerPt[1 - i])));
			}
		}
	}

	@Override
	public void paintLabel(Graphics2D g2, AffineTransform transform) {
		super.paintLabel(g2, transform);
		if (isLabelDisplayable()) {
			for (int i = 0; i < 2; i++) {
				if (angleDeg[i] != null) {
					String str = DecFormatter.allNumber(angleDeg[i]) + "°";
					double px = centerPt[i].getX() - 30 + (i * 30);
					double py = centerPt[i].getY() + 30;
					Point2D pt = new Point2D.Double(px, py);
					transform.transform(pt, pt);
					float x = (float) pt.getX();
					float y = (float) pt.getY();
					g2.setPaint(Color.BLACK);
					g2.drawString(str, x - 1f, y - 1f);
					g2.drawString(str, x - 1f, y);
					g2.drawString(str, x - 1f, y + 1f);
					g2.drawString(str, x, y - 1f);
					g2.drawString(str, x, y + 1f);
					g2.drawString(str, x + 1f, y - 1f);
					g2.drawString(str, x + 1f, y);
					g2.drawString(str, x + 1f, y + 1f);
					g2.setPaint(Color.WHITE);
					g2.drawString(str, x, y);
				}
			}
		}
	}

}
