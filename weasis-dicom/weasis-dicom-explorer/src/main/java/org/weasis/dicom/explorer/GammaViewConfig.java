/** */
package org.weasis.dicom.explorer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.util.ResourceUtil;

import com.formdev.flatlaf.extras.FlatSVGIcon;

/** @author t68 */
public class GammaViewConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(GammaViewConfig.class);

  private JButton button = null;

  public GammaViewConfig() {
    File gvc = new File("gammaViewConfig.properties");
    if (gvc.canRead()) {
      Properties p = new Properties();
      try (FileInputStream fis = new FileInputStream(gvc)) {
        p.load(fis);
      } catch (IOException ex) {
        LOGGER.warn("", ex);
      }
      String exec = p.getProperty("executable");
      if (exec != null) {
        button =
            new JButton(
                new FlatSVGIcon(new File(p.getProperty("svgPath")))
                    .derive(ResourceUtil.TOOLBAR_ICON_SIZE, ResourceUtil.TOOLBAR_ICON_SIZE));
        button.setToolTipText(p.getProperty("tooltip", ""));
        button.addActionListener(
            e -> {
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
}
