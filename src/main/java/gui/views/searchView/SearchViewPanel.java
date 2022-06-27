package gui.views.searchView;

import gui.MainFrame;
import gui.MainFrameController;
import gui.components.ImageViewer;
import gui.components.LastElementAutoCompleteField;
import gui.views.imageView.ImageViewController;
import model.ImgTag;
import model.Pixeldata;
import utils.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SearchViewPanel extends JSplitPane {

    private final Component rightView;

    private final JPanel pGrid = new JPanel(new GridLayout(1, 1, 5, 5), true);
//    private final JPanel pGrid = new JPanel(new WrappingFlowLayout(WrappingFlowLayout.CENTER),  true);

    private final LastElementAutoCompleteField auto = new LastElementAutoCompleteField(50);

    public SearchViewPanel() {

        super(JSplitPane.HORIZONTAL_SPLIT, false);

        Component leftView = new JScrollPane(createTagView(new ArrayList<>(0)),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        leftView.setPreferredSize(new Dimension(200, 2000));
        rightView = createGridView();

        setLeftComponent(leftView);
        setRightComponent(rightView);
        setDividerLocation(.5);

    }

    public void showImages(List<Pixeldata> pxs) {
        pGrid.removeAll();
        Dimension d = new Dimension(256, 256);
        pxs.forEach(px -> {
            ImageViewer iv = new ImageViewer();
            iv.setImg(ImageUtils.resize(px.getImage(), 256));
            Dimension d2 = new Dimension(iv.getImg().getWidth(), iv.getImg().getHeight());
            iv.setPreferredSize(d2);
            iv.setMaximumSize(d2);
            iv.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    super.mouseClicked(e);
                    System.out.println("show image " + px.getImageId());
                    ImageViewController.get().showImage(px);
                    MainFrameController.get().showView(MainFrame.VIEW_IMAGE);
                }
            });
//            iv.setBorder(BorderFactory.createLineBorder(Color.gray));
            pGrid.add(iv);
        });
        pGrid.revalidate();
    }

    private Component createGridView() {

        JPanel p = new JPanel(new BorderLayout(), true);

        JPanel pGridContainer = new JPanel(new FlowLayout(FlowLayout.CENTER), true);
        pGridContainer.add(pGrid);

        JScrollPane jsp = new JScrollPane(pGridContainer, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.getVerticalScrollBar().setUnitIncrement(10);
        jsp.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                if (!isVisible()) {
                    return;
                }
                Dimension size = jsp.getSize();
                int       nx   = (int) ((size.getWidth() - 5) / 261);
                int       ny   = (int) Math.ceil(pGrid.getComponents().length / (double) nx);

                GridLayout layout = (GridLayout) pGrid.getLayout();
                layout.setColumns(nx);
                layout.setRows(ny);

                pGrid.revalidate();
                jsp.revalidate();
            }
        });
        p.add(jsp, BorderLayout.CENTER);

        JPanel pSearch = new JPanel(new FlowLayout(), true);

        auto.setReturnAction(tags -> {
            SearchViewController.get().search();
        });

        pSearch.add(new JLabel("Search:"));
        pSearch.add(auto);

        p.add(pSearch, BorderLayout.NORTH);

        return p;
    }

    private Component createTagView(List<ImgTag> tags) {

        JPanel p = new JPanel(true);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel lblArtist = new JLabel("Artist");
        lblArtist.setFont(lblArtist.getFont().deriveFont(Font.BOLD).deriveFont(18f));
        lblArtist.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        p.add(lblArtist);
        createTagLabels(tags, ImgTag.Category.Artist, p);


        JLabel lblCharacter = new JLabel("Character");
        lblCharacter.setFont(lblCharacter.getFont().deriveFont(Font.BOLD).deriveFont(18f));
        lblCharacter.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        p.add(lblCharacter);
        createTagLabels(tags, ImgTag.Category.Character, p);

        JLabel lblGeneral = new JLabel("General");
        lblGeneral.setFont(lblGeneral.getFont().deriveFont(Font.BOLD).deriveFont(18f));
        lblGeneral.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        p.add(lblGeneral);
        createTagLabels(tags, ImgTag.Category.General, p);

        JLabel lblMeta = new JLabel("Meta");
        lblMeta.setFont(lblMeta.getFont().deriveFont(Font.BOLD).deriveFont(18f));
        lblMeta.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        p.add(lblMeta);
        createTagLabels(tags, ImgTag.Category.Meta, p);


//        p.setPreferredSize(new Dimension(150, 1));
//        p.setMinimumSize(new Dimension(150, 1));
        p.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 5));

        return p;
    }

    private void createTagLabels(final List<ImgTag> tags, final ImgTag.Category c, final JPanel p) {
        tags.stream().filter(t -> t.getCategory() == c).forEach(t -> {
            JLabel lbl = new JLabel(t.getName());
            lbl.setForeground(c.getColor());
            lbl.setFont(lbl.getFont().deriveFont(14f).deriveFont(Font.BOLD));
            lbl.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (e.isControlDown()) {
                            auto.setText((auto.getText() + " " + t.getName()).trim());
                            auto.grabFocus();
                        } else {
                            auto.setText(t.getName());
                            SearchViewController.get().search();
                            auto.grabFocus();
                        }
                    }
                }
            });
            lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            p.add(lbl);
        });
    }

    public void showTags(final List<ImgTag> tags) {
        JScrollPane tagView = new JScrollPane(createTagView(tags),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
//        tagView.setPreferredSize(new Dimension(200, 0));
        tagView.getVerticalScrollBar().setUnitIncrement(5);
        tagView.setPreferredSize(new Dimension(200, 2000));
        tagView.setMinimumSize(new Dimension(200, 2000));
        setLeftComponent(tagView);
        revalidate();
        repaint();
    }

    public List<String> getSearchTags() {
        return Arrays.stream(auto.getText().trim().toLowerCase().split("\\s+")).toList();
    }

    public void setSearchTags(final List<String> searchTags) {
        auto.setText(searchTags.stream().map(ImgTag::getNameFromString).collect(Collectors.joining(" ")));
    }
}
