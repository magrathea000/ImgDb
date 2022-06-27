package gui.views.imageView;

import gui.MainFrame;
import gui.MainFrameController;
import gui.components.ImageViewer;
import gui.components.JRating2;
import gui.components.VerticalFlowLayout;
import gui.views.searchView.SearchViewController;
import lombok.extern.slf4j.Slf4j;
import model.ImgTag;
import model.Metadata;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import utils.ImageUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ImageViewPanel extends JSplitPane {

    private static BufferedImage icoCopy;
    private static BufferedImage icoDel;
    private static BufferedImage icoAdd;

    static {

        ClassLoader classLoader = ImageViewPanel.class.getClassLoader();
        int         res         = 16;
        try {
            InputStream   isAdd  = classLoader.getResourceAsStream("assets/images/ico_add_sml.png");
            BufferedImage imgAdd = ImageUtils.loadImage(isAdd);
            icoAdd = ImageUtils.resize(Objects.requireNonNull(imgAdd), res);
        } catch (NullPointerException e) {
            BufferedImage image = new BufferedImage(res, res, BufferedImage.TYPE_INT_RGB);
            Graphics2D    g     = image.createGraphics();
            g.setColor(Color.GREEN);
            g.fillRect(0, 0, res, res);
            g.dispose();
            icoAdd = image;
        }

        try {
            InputStream   isDel  = classLoader.getResourceAsStream("assets/images/ico_del_sml.png");
            BufferedImage imgDel = ImageUtils.loadImage(isDel);
            icoDel = ImageUtils.resize(Objects.requireNonNull(imgDel), res);
        } catch (NullPointerException e) {
            BufferedImage image = new BufferedImage(res, res, BufferedImage.TYPE_INT_RGB);
            Graphics2D    g     = image.createGraphics();
            g.setColor(Color.GREEN);
            g.fillRect(0, 0, res, res);
            g.dispose();
            icoDel = image;
        }

        try {
            InputStream   isCopy  = classLoader.getResourceAsStream("assets/images/ico_cpy_sml.png");
            BufferedImage imgCopy = ImageUtils.loadImage(isCopy);
            icoCopy = ImageUtils.resize(Objects.requireNonNull(imgCopy), res);
        } catch (NullPointerException e) {
            BufferedImage image = new BufferedImage(res, res, BufferedImage.TYPE_INT_RGB);
            Graphics2D    g     = image.createGraphics();
            g.setColor(Color.GREEN);
            g.fillRect(0, 0, res, res);
            g.dispose();
            icoCopy = image;
        }

    }

    private final ImageViewer iv;
    private final JPanel      sidePanel;
    private final JTextField  txtId  = new JTextField(15);
    private final JLabel      lblRes = new JLabel();
    private final JRating2    rating = new JRating2("", 0, 0, 5);
    private       Component   tagView;

    public ImageViewPanel() {
        super(JSplitPane.HORIZONTAL_SPLIT, false);

        txtId.setEditable(false);
        txtId.setBorder(null);
        txtId.setFont(lblRes.getFont());

        JScrollPane jsp = new JScrollPane(
                createTagView(new ArrayList<>(0)),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        jsp.getVerticalScrollBar().setUnitIncrement(5);

        tagView = jsp;

        iv = new ImageViewer();
        sidePanel = new JPanel(new BorderLayout(), true);

        sidePanel.add(createMetaView(), BorderLayout.NORTH);
        sidePanel.add(tagView, BorderLayout.CENTER);

        JPanel pImg = new JPanel(new BorderLayout(), true);

        pImg.add(iv, BorderLayout.CENTER);

        JPanel pctrl = new JPanel(new FlowLayout(FlowLayout.RIGHT), true);

        JButton btnExport = new JButton("Export");
        btnExport.addActionListener(exportAction());
        pctrl.add(btnExport);

        pImg.add(pctrl, BorderLayout.SOUTH);

        setLeftComponent(sidePanel);
        setRightComponent(pImg);
        setDividerLocation(.5);

    }

    private ActionListener exportAction() {
        JFileChooser   jfc    = new JFileChooser();
        ImageViewPanel parent = this;
        return e -> {
            if (jfc.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;
            File   selectedFile = jfc.getSelectedFile();
            String ext          = FilenameUtils.getExtension(selectedFile.getName());
            if (ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg") || ext.equals("qoi")) {
                try {
                    ImageIO.write(iv.getImg(), ext, selectedFile);
                } catch (IOException ex) {
                    log.warn("unable to safe image to file {}. {}", selectedFile.getAbsolutePath(), ex.getMessage());
                    JOptionPane.showMessageDialog(parent,
                            "Unable to safe image to file " + selectedFile.getAbsolutePath(),
                            "Export error...",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else if (ext == null || ext.isBlank()) {
                try {
                    ImageIO.write(iv.getImg(), "png", new File(selectedFile.getAbsolutePath() + ".png"));
                } catch (IOException ex) {
                    log.warn("unable to safe image to file {}. {}", selectedFile.getAbsolutePath(), ex.getMessage());
                    JOptionPane.showMessageDialog(parent,
                            "Unable to safe image to file " + selectedFile.getAbsolutePath(),
                            "Export error...",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                log.warn("unable to safe image to file {}. {}", selectedFile.getAbsolutePath(), "Unsupported file type");
                JOptionPane.showMessageDialog(parent,
                        "Unsupported file extension '" + ext + "'",
                        "Export error...",
                        JOptionPane.ERROR_MESSAGE);
            }
        };
    }

    public void updateMetaView(Metadata m) {
        txtId.setText(m.getImageId().getIdValue().toString());
        lblRes.setText(m.getWitdh() + "x" + m.getHeight());
        rating.setValue(m.getRating());
    }

    private Component createMetaView() {
        JPanel p = new JPanel(new GridLayout(3, 2), true);

        p.add(new JLabel("Image ID"));

        JPanel pid = new JPanel(new BorderLayout(), true);
        pid.setPreferredSize(new Dimension(125, 24));
        pid.setMinimumSize(new Dimension(125, 24));
        pid.add(txtId, BorderLayout.CENTER);
        JLabel lblCpy = new JLabel(new ImageIcon(icoCopy));
        lblCpy.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (!isEnabled()) return;
                StringSelection selection = new StringSelection(txtId.getText());
                Clipboard       clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
            }
        });
        pid.add(lblCpy, BorderLayout.EAST);
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseEntered(final MouseEvent e) {
                lblCpy.setVisible(true);
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                lblCpy.setVisible(false);
            }
        };

        pid.addMouseListener(ma);
        txtId.addMouseListener(ma);
        lblCpy.addMouseListener(ma);

        lblCpy.setVisible(false);

        p.add(pid);

        p.add(new JLabel("Resolution"));
        p.add(lblRes);

        p.add(new JLabel("Rating"));
        p.add(rating);

        p.setBorder(BorderFactory.createTitledBorder("Image Data"));

        return p;
    }

    private Component createTagView(List<ImgTag> tags) {

        VerticalFlowLayout l = new VerticalFlowLayout(VerticalFlowLayout.TOP, 5, 5);
        l.setMaximizeOtherDimension(true);
        JPanel p = new JPanel(l, true);

        JPanel artist = createTagSection(tags, "Artist", ImgTag.Category.Artist);
        p.add(artist);

        JPanel character = createTagSection(tags, "Character", ImgTag.Category.Character);
        p.add(character);

        JPanel general = createTagSection(tags, "General", ImgTag.Category.General);
        p.add(general);

        JPanel meta = createTagSection(tags, "Meta", ImgTag.Category.Meta);
        p.add(meta);

        return p;
    }

    private JPanel createTagSection(final List<ImgTag> tags, final String header, final ImgTag.Category category) {

        JPanel p = new JPanel(new BorderLayout(), true);

        JPanel phead     = new JPanel(new FlowLayout(FlowLayout.LEFT), true);
        JLabel lblArtist = new JLabel(header, JLabel.LEFT);
        lblArtist.setFont(lblArtist.getFont().deriveFont(Font.BOLD).deriveFont(18f));
        phead.add(lblArtist);

        JPanel pTags = createTagLabels(tags, category);
        p.setOpaque(true);
        JLabel lblAdd = new JLabel(new ImageIcon(icoAdd));
        lblAdd.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (!isEnabled()) return;
                MainFrame    frame     = MainFrameController.get().getFrame();
                List<ImgTag> imageTags = ImageViewController.get().getImageTags();
                String r = switch (category) {
                    case Artist -> NewTagDialog.showArtistDialog(frame, imageTags);
                    case Character -> NewTagDialog.showCharacterDialog(frame, imageTags);
                    case General -> NewTagDialog.showGeneralDialog(frame, imageTags);
                    case Meta -> NewTagDialog.showMetaDialog(frame, imageTags);
                };
                if (r != null)
                    ImageViewController.get().addTag(r);
            }
        });
        phead.add(lblAdd);

        p.add(phead, BorderLayout.NORTH);
        p.add(pTags, BorderLayout.CENTER);

        return p;
    }

    private JPanel createTagLabels(final List<ImgTag> tags, final ImgTag.Category c) {

        ImageIcon     delicon = new ImageIcon(icoDel);
        GridLayout    l       = new GridLayout();
        JPanel        p       = new JPanel(l, true);
        AtomicInteger n       = new AtomicInteger(0);
        tags.stream().filter(t -> t.getCategory() == c).sorted().forEach(t -> {
            JLabel lbl = createTagLabel(t);

            JLabel lblDel = new JLabel(delicon);
            lblDel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    ImageViewController.get().removeTag(t);
                }
            });

            JPanel pl = new JPanel(new FlowLayout(FlowLayout.LEFT));
            pl.add(lbl);
            pl.add(lblDel);

            p.add(pl);
            n.incrementAndGet();
        });

        l.setRows(Math.max(n.get(), 1));
        l.setColumns(1);

        return p;
    }

    @NotNull
    private JLabel createTagLabel(final ImgTag t) {
        JLabel lbl = new JLabel(t.getName(), JLabel.LEFT);
        lbl.setForeground(t.getCategory().getColor());
        lbl.setFont(lbl.getFont().deriveFont(14f).deriveFont(Font.BOLD));
        lbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getButton() == MouseEvent.BUTTON1) {
                    MainFrameController.get().showView(MainFrame.VIEW_SEARCH);
                    SearchViewController.get().search(List.of(t.getName()));
                }
            }
        });
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return lbl;
    }

    public void setTags(final List<ImgTag> tags) {
        sidePanel.remove(tagView);
        JScrollPane jsp = new JScrollPane(
                createTagView(tags),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        jsp.getVerticalScrollBar().setUnitIncrement(5);

        tagView = jsp;
        sidePanel.add(tagView, BorderLayout.CENTER);
        sidePanel.revalidate();
        sidePanel.repaint();
    }

    public void setImage(BufferedImage img) {
        iv.setImg(img);
    }

    public void setMetadata(Metadata m) {
        updateMetaView(m);
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        rating.setEnabled(enabled);
    }
}
