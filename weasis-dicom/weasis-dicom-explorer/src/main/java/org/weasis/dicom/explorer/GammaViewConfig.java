/** */
package org.weasis.dicom.explorer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;

import javax.swing.JButton;

import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.SeriesComparator;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.SortSeriesStack;

import com.formdev.flatlaf.extras.FlatSVGIcon;

/** @author t68 */
public class GammaViewConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(GammaViewConfig.class);

	private static GammaViewConfig instance = new GammaViewConfig();

	public static GammaViewConfig getInstance() {
		return instance;
	}

	private JButton button = null;
	Properties p = new Properties();

	public GammaViewConfig() {
		File gvc = new File("gammaViewConfig.properties");
		if (gvc.canRead()) {
			try (FileInputStream fis = new FileInputStream(gvc)) {
				p.load(fis);
			} catch (IOException ex) {
				LOGGER.warn("", ex);
			}
			String exec = p.getProperty("executable");
			if (exec != null) {
				button = new JButton(new FlatSVGIcon(new File(p.getProperty("svgPath"))).derive(ResourceUtil.TOOLBAR_ICON_SIZE, ResourceUtil.TOOLBAR_ICON_SIZE));
				button.setToolTipText(p.getProperty("tooltip", ""));
				button.addActionListener(e -> {
					try {
						Runtime.getRuntime().exec(exec, null, new File(exec).getParentFile());
					} catch (IOException ex) {
						LOGGER.warn("", ex);
					}
				});
			}
		}
	}

	public JButton getConfiguredToolButton() {
		return button;
	}

	public SeriesComparator<DicomImageElement> getDefaultSeriesComparator(MediaSeries<DicomImageElement> series) {
		try {
			Object o = p.get("SeriesComparator");
			if (o != null) {
				String value = o.toString();
				String[] splitted = value.split(";");
				for (String s : splitted) {
					int indexOf = s.indexOf(">");
					if (indexOf > 0) {
						SeriesComparator<DicomImageElement> comp = getMatchingComparator(series, s, indexOf);
						if (comp != null)
							return comp;
					} else {
						SeriesComparator<DicomImageElement> comp = getComparatorByName(s);
						if (comp != null)
							return comp;
					}
				}
			}
		} catch (Exception ex) {
			LOGGER.warn("", ex);
		}
		return SortSeriesStack.instanceNumber;
	}

	private SeriesComparator<DicomImageElement> getComparatorByName(String name) {

		try {
			Field declaredField = SortSeriesStack.class.getDeclaredField(name);
			Object object = declaredField.get(null);
			LOGGER.info("declaredField: " + object);
			if (object instanceof SeriesComparator comp) {
				return comp;
			}
		} catch (Exception ex) {
			LOGGER.warn("", ex);
		}
		return null;
	}

	private SeriesComparator<DicomImageElement> getMatchingComparator(MediaSeries<DicomImageElement> series, String s, int indexOf) {
		String name = s.substring(indexOf + 1);
		SeriesComparator<DicomImageElement> comp = getComparatorByName(name);
		if (comp != null && series != null) {
			String tagConfig = s.substring(0, indexOf);
			String[] tags = tagConfig.split(",");
			boolean match = true;
			for (String tagValue : tags) {
				String[] splitted = tagValue.split("=");
				int key = TagUtils.intFromHexString(splitted[0]);
				String value = splitted[1];
				TagW tag = series.getTagElement(key);
				Object tv = series.getTagValue(tag);
				match = match && value.equalsIgnoreCase(tv.toString());
			}
			if (match)
				return comp;
		}
		return null;
	}
}
