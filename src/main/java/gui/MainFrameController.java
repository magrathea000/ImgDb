package gui;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Objects;

@Slf4j
public class MainFrameController {

    private static MainFrameController instance;

    private final MainFrame mf;

    private MainFrameController() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        GraphicsEnvironment ge = null;
        try {
            ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String      path = "assets/fonts/RobotoMono.ttf";
            InputStream is   = Objects.requireNonNull(MainFrameController.class.getClassLoader().getResourceAsStream(path));
            Font        font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12f);
            ge.registerFont(font);
            log.info("{} registerd", font);
            Enumeration<Object> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key   = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof FontUIResource) {
                    UIManager.put(key, new FontUIResource(font));
                }
            }
            path = "assets/fonts/Asap.ttf";
            is = Objects.requireNonNull(MainFrameController.class.getClassLoader().getResourceAsStream(path));
            font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12f);
            ge.registerFont(font);
            log.info("{} registerd", font);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }

        mf = new MainFrame();
    }

    public static MainFrameController get() {
        if (instance == null) {
            instance = new MainFrameController();
        }
        return instance;
    }

    public void showView(int v) {
        mf.setView(v);
    }

    public void show() {
        mf.setVisible(true);
    }

    public MainFrame getFrame() {
        return mf;
    }
}
