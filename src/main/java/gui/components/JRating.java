package gui.components;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

@Getter
@Setter
public class JRating extends JPanel {

    private final JLabel               lblRating = new JLabel();
    private       int                  value;
    private       int                  min;
    private       int                  max;
    private       boolean              editable  = true;
    private       ValueChangedCallback callback;

    public JRating(String lbl, int value, int min, int max) {
        this(lbl, value, min, max, null);
    }

    public JRating(String lbl, int value, int min, int max, ValueChangedCallback callback) {

        super(new FlowLayout(FlowLayout.LEFT), true);

        this.min = min;
        this.max = max;

        Font font = Arrays.stream(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts())
                          .filter(f -> f.getName().equals("Asap"))
                          .findAny()
                          .orElse(getFont())
                          .deriveFont(72f)
                          .deriveFont(Font.BOLD);


        lblRating.setFont(font);
        lblRating.setForeground(Color.orange);

        if (!lbl.isBlank())
            add(new JLabel(lbl));
        add(lblRating);

        setValue(value);

        FontRenderContext frc    = new FontRenderContext(font.getTransform(), true, true);
        TextLayout        tl     = new TextLayout(lblRating.getText(), font, frc);
        Rectangle2D       bounds = tl.getBounds();

        Dimension d = new Dimension((int) bounds.getWidth() + 6, (int) bounds.getHeight() + 6);
        lblRating.setPreferredSize(d);
        lblRating.setMinimumSize(d);
        lblRating.setMaximumSize(d);

        lblRating.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isEnabled()) return;
                super.mouseClicked(e);
                if (!editable) return;

                Point     point = e.getPoint();
                Dimension size  = lblRating.getSize();

                float p = (float) Math.min(Math.max(point.getX() / size.getWidth(), 0), size.getWidth());

                int range = Math.abs(max - min);

                if (p < 1. / range / 2) {
                    setValue(min);
                } else {
                    setValue(min + (int) Math.ceil(p * range));
                }
            }
        });

        addMouseWheelListener(e -> {
            if (!isEnabled()) return;
            setValue(Math.min(Math.max(this.value - e.getWheelRotation(), min), max));
        });

        this.callback = callback;
    }

    public void setValue(int value) {
        this.value = value;
        lblRating.setText("★".repeat(value - min) + "☆".repeat(max - value));
        if (callback != null) {
            callback.valueChanged(value);
        }
    }

    public void setEditable(final boolean b) {
        editable = b;
    }

    public void setValueChangedCallback(ValueChangedCallback callback) {
        this.callback = callback;
    }

    public interface ValueChangedCallback {
        void valueChanged(int newValue);
    }
}
