package gui.components;

import model.ImgTag;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class AutoCompleteField extends JTextField implements KeyListener, DocumentListener, ComponentListener {

    protected final List<ImgTag>             elements;
    private final   JFrame                   fGuesses;
    private final   JList<ImgTag>            jGuesses;
    private final   DefaultListModel<ImgTag> model;
    private final   int                      numberOfGuesses  = 10;
    protected       boolean                  guessListVisible = false;
    private         ReturnAction             action           = (s) -> {
    };
    private         Shape                    shape;
    private         boolean                  decorated        = true;

    public AutoCompleteField(int columns) {
        this(columns, true);
    }

    public AutoCompleteField(int columns, boolean decorated) {
        super(columns);

        elements = new ArrayList<>();
        model = new DefaultListModel<>();

        jGuesses = new JList<>(model);
        jGuesses.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        jGuesses.setCellRenderer(new TagListCellRenderer());

        addKeyListener(this);
        getDocument().addDocumentListener(this);
        addComponentListener(this);

        fGuesses = new JFrame();
        fGuesses.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        fGuesses.add(jGuesses);
        fGuesses.setUndecorated(true);
        fGuesses.setAlwaysOnTop(true);

        this.decorated = decorated;
//        setFocusTraversalKeysEnabled(false);
    }

    public void addElement(ImgTag s) {
        if (elements.contains(s)) return;
        elements.add(s);
        Collections.sort(elements);
    }

    public void addElements(Collection<ImgTag> s) {
        for (ImgTag t : s) {
            if (elements.contains(t)) continue;
            elements.add(t);
        }
        Collections.sort(elements);
    }

    public void setElements(Collection<ImgTag> s) {
        elements.clear();
        elements.addAll(s.stream().toList());
        Collections.sort(elements);
    }

    public void clearElements() {
        elements.clear();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.isControlDown() && !fGuesses.isVisible() && e.getKeyCode() == KeyEvent.VK_SPACE) {
            updateGuesses(getNewGuesses(getText(), getCaretPosition()));
            fGuesses.setVisible(true);
            e.consume();
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (fGuesses.isVisible()) {
                replaceWithGuess();
                fGuesses.setVisible(false);
                e.consume();
                return;
            } else {
                action.action(getText());
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            fGuesses.setVisible(false);
            e.consume();
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            jGuesses.setSelectedIndex((jGuesses.getSelectedIndex() - 1) % model.size());
            e.consume();
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            jGuesses.setSelectedIndex((jGuesses.getSelectedIndex() + 1) % model.size());
            e.consume();
        }
    }

    private void showGuessList() {

        Dimension d = new Dimension(getWidth(), 24 * numberOfGuesses);
        jGuesses.setMinimumSize(d);
        jGuesses.setPreferredSize(d);
        jGuesses.setFocusable(false);

        Point location = getLocationOnScreen();
        fGuesses.setLocation(location.x, location.y + getHeight());
        fGuesses.pack();
        fGuesses.setVisible(true);
        fGuesses.revalidate();
        fGuesses.repaint();
        fGuesses.setFocusable(false);
        fGuesses.setFocusableWindowState(false);

    }

    private void updateGuesses(List<ImgTag> tags) {
        model.clear();
        tags.stream()
            .limit(numberOfGuesses)
            .forEach(model::addElement);
        jGuesses.setSelectedIndex(0);
    }

    protected abstract List<ImgTag> getNewGuesses(String text, int pos);

    private void replaceWithGuess() {
        String text = getText();
        int    i    = text.lastIndexOf(' ');
        setText(text.substring(0, i + 1) + model.getElementAt(jGuesses.getSelectedIndex()).getName());
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        if (e.getLength() == 1)
            changed(getCaretPosition() + e.getLength());
        else
            fGuesses.setVisible(false);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        if (e.getLength() == 1)
            changed(getCaretPosition() - e.getLength());
        else
            fGuesses.setVisible(false);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        if (e.getLength() == 1)
            changed(getCaretPosition() + e.getLength());
        else {
            fGuesses.setVisible(false);
        }
    }

    private void changed(int pos) {
        String text = getText();
        if (pos >= text.length() && !text.isBlank()) {
            updateGuesses(getNewGuesses(text, pos));
            showGuessList();
            if (model.getSize() < 1) {
                fGuesses.setVisible(false);
            }
        } else {
            fGuesses.setVisible(false);
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {

    }

    @Override
    public void componentMoved(ComponentEvent e) {
        if (!isVisible() || !isShowing()) return;
        Point location = getLocationOnScreen();
        fGuesses.setLocation(location.x, location.y + getHeight());
        fGuesses.revalidate();
        fGuesses.repaint();
    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }

    @Override
    public void setText(String t) {
        super.setText(t);
        setCaretPosition(t.length());
    }

    public void setReturnAction(ReturnAction action) {
        this.action = action;
    }

    protected void paintComponent(Graphics g) {
        if (!isOpaque() && getBorder() instanceof RoundedCornerBorder) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(getBackground());
            g2.fill(((RoundedCornerBorder) getBorder()).getBorderShape(
                    0, 0, getWidth() - 1, getHeight() - 1));
            g2.dispose();
        } else if (decorated) {
            updateUI();
            paintComponent(g);
        }
        super.paintComponent(g);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (!decorated) return;
        setOpaque(false);
        setBorder(new RoundedCornerBorder());
    }

    public interface ReturnAction {
        void action(String s);
    }

    class TagListCellRenderer extends JLabel implements ListCellRenderer<ImgTag> {

        private static final Color cSelectionBg = new Color(0, 156, 200);

        public TagListCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ImgTag> list, ImgTag value, int index, boolean isSelected, boolean cellHasFocus) {
            this.setText(value.getName());
            this.setBackground(isSelected ? cSelectionBg : Color.white);
            this.setForeground(isSelected ? Color.white : Color.black);
            return this;
        }
    }
}
