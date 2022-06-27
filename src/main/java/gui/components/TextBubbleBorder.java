package gui.components;

import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

public class TextBubbleBorder extends AbstractBorder {

    RenderingHints hints;
    private Color       color;
    private int         thickness  = 4;
    private int         radii      = 8;
    private Insets      insets     = null;
    private BasicStroke stroke     = null;
    private int         strokePad;
    private int         pointerPad = 4;
    private boolean     left       = true;

    TextBubbleBorder(
            Color color) {
        this(color, 4, 8);
    }


    public TextBubbleBorder(
            int thickness, int radii) {
        this(Color.black, thickness, radii);
    }

    public TextBubbleBorder(
            Color color, int thickness, int radii) {
        this.thickness = thickness;
        this.radii = radii;
        this.color = color;

        stroke = new BasicStroke(thickness);
        strokePad = thickness / 2;

        hints = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int pad = (radii + strokePad) >> 1;
        insets = new Insets(pad >> 1, pad, pad >> 1, pad);
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return insets;
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        return getBorderInsets(c);
    }

    @Override
    public void paintBorder(
            Component c,
            Graphics g,
            int x, int y,
            int width, int height) {

        Graphics2D g2 = (Graphics2D) g;

        int bottomLineY = height - thickness;

        RoundRectangle2D.Double bubble = new RoundRectangle2D.Double(
                0 + strokePad,
                0 + strokePad,
                width - thickness,
                bottomLineY,
                radii,
                radii);

        Area area = new Area(bubble);

        g2.setRenderingHints(hints);

        // Paint the BG color of the parent, everywhere outside the clip
        // of the text bubble.
        Component parent = c.getParent();
        if (parent != null) {
            Color     bg           = parent.getBackground();
            Rectangle rect         = new Rectangle(0, 0, width, height);
            Area      borderRegion = new Area(rect);
            borderRegion.subtract(area);
            g2.setClip(borderRegion);
            g2.setColor(bg);
            g2.fillRect(0, 0, width, height);
            g2.setClip(null);
        }

        g2.setColor(color);
        g2.setStroke(stroke);
        g2.draw(area);
    }
}