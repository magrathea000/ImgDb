package gui.components;

import lombok.Getter;
import lombok.Setter;
import utils.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Objects;

@Getter
@Setter
public class JRating2 extends JPanel {

    private static ImageIcon icoStarFilled;
    private static ImageIcon icoStarEmpty;

    static {
        InputStream   is     = JRating2.class.getClassLoader().getResourceAsStream("assets/images/star_filled.png");
        BufferedImage resize = ImageUtils.resize(Objects.requireNonNull(ImageUtils.loadImage(is)), 16);
        icoStarFilled = new ImageIcon(resize);
        is = JRating2.class.getClassLoader().getResourceAsStream("assets/images/star_empty.png");
        resize = ImageUtils.resize(Objects.requireNonNull(ImageUtils.loadImage(is)), 16);
        icoStarEmpty = new ImageIcon(resize);
    }

    private int                  value;
    private int                  min;
    private int                  max;
    private boolean              editable = true;
    private ValueChangedCallback callback;
    private JLabel[]             lblStars;

    public JRating2(String lbl, int value, int min, int max) {
        this(lbl, value, min, max, null);
    }

    public JRating2(String lbl, int value, int min, int max, ValueChangedCallback callback) {

//        setLayout(new GridLayout(1, Math.max(max - min, 0)));
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setMinimumSize(new Dimension(icoStarEmpty.getIconWidth() * Math.max(max - min, 0), icoStarEmpty.getIconHeight()));

        this.min = min;
        this.max = max;
        this.callback = callback;

        lblStars = new JLabel[Math.max(max - min, 0)];

        MouseWheelListener mwl = e -> {
            if (!isEnabled()) return;
            setValue(Math.min(Math.max(this.value - e.getWheelRotation(), min), max));
        };

        addMouseWheelListener(mwl);

        if (lbl != null && !lbl.isBlank()) {
            JLabel hint = new JLabel(lbl);
            hint.addMouseWheelListener(mwl);
            add(hint);
        }


        for (int i = 0; i < lblStars.length; i++) {
            JLabel    l = new JLabel(icoStarEmpty);
            Dimension d = new Dimension(icoStarEmpty.getIconWidth(), icoStarEmpty.getIconHeight());
            l.setMinimumSize(d);
            l.setMaximumSize(d);
            l.setPreferredSize(d);
            final int finalI = i;
            l.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    if (!isEnabled()) return;
                    if (finalI == 0) {
                        int x = e.getX();
                        if (x < d.getWidth() / 3) {
                            setValue(min);
                        } else {
                            setValue(finalI + 1);
                        }
                    } else {
                        setValue(finalI + 1);
                    }
                }
            });
            l.addMouseWheelListener(mwl);
            add(l);
            lblStars[i] = l;
        }

        setValue(value);
    }

    public void setValue(int value) {
        this.value = value;

        for (int i = 0; i < lblStars.length; i++) {
            if (value == min) {
                lblStars[i].setIcon(icoStarEmpty);
            } else if (i + min < value) {
                lblStars[i].setIcon(icoStarFilled);
            } else {
                lblStars[i].setIcon(icoStarEmpty);
            }
        }

        repaint();

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
