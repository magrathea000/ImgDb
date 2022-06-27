package gui.views.importView;

import database.DatabaseController;
import gui.components.*;
import gui.views.importView.ImportViewModel.ImportItem;
import model.ImgTag;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ImportViewPanel extends JSplitPane {

    private final DefaultListModel<ImportItem> fvListModel = new DefaultListModel<>();
    private final JList<ImportItem>            fvList      = new JList<>(fvListModel);

    private final ImageViewer ivImage = new ImageViewer();

    private final WholeStringAutoCompleteField mvAuto                 = new WholeStringAutoCompleteField(30);
    private final JTextArea                    mvTags                 = new JTextArea();
    private final JRating2                     mvRating               = new JRating2("Rating: ", 0, 0, 5);
    private final JPanel                       mvRecommendedContainer = new JPanel(new WrappingFlowLayout(WrappingFlowLayout.LEFT), true);

    private final Component leftView;
    private final Component rightView;

    public ImportViewPanel() {

        super(JSplitPane.HORIZONTAL_SPLIT, false);

        leftView = createFileView();
        rightView = createImportItemEditView();

        setLeftComponent(leftView);
        setRightComponent(rightView);

        leftView.setPreferredSize(new Dimension(250, 1));
//        setDividerLocation(.5);
    }

    public void updateMetadataUi(ImportViewModel m) {
        ImportItem ii = m.getCurrentImportItem();
        mvRating.setValue(ii.getRating());
        ivImage.setImg(ii.getImg());
        mvTags.setText(ii.getTags().stream().map(ImgTag::getUiString).collect(Collectors.joining(" ")));
        updateRecommendedTags(ii.getRecommendations());
        rightView.revalidate();
        rightView.repaint();
    }

    public void updateImportItem(ImportItem ii) {
        if (ii == null) return;
        DatabaseController dbc = DatabaseController.getInstance();
        ii.setRating(mvRating.getValue());
        Set<ImgTag> tags = Arrays.stream(mvTags.getText().trim().toLowerCase().split("\\s+"))
                                 .map(s -> dbc.getTagByName(s)
                                              .orElse(new ImgTag(
                                                      null,
                                                      ImgTag.getNameFromString(s),
                                                      0,
                                                      ImgTag.getCategoryFromString(s))
                                              ))
                                 .collect(Collectors.toSet());
        ii.setTags(tags);
    }

    public void updateRecommendedTags(Set<ImgTag> tags) {
        mvRecommendedContainer.removeAll();
        tags.forEach(t -> {
            JLabel lbl = new JLabel(t.getName());
            lbl.setForeground(switch (t.getCategory()) {
                case General -> Color.getHSBColor(0.66f, 1, .66f);
                case Artist -> Color.getHSBColor(0, 1, .66f);
                case Character -> Color.getHSBColor(0.33f, 1, .25f);
                case Meta -> Color.getHSBColor(0.09f, 1, .66f);
            });
            lbl.setBackground(switch (t.getCategory()) {
                case General -> Color.getHSBColor(0.66f, .25f, .9f);
                case Artist -> Color.getHSBColor(0, .25f, .9f);
                case Character -> Color.getHSBColor(0.33f, .25f, .9f);
                case Meta -> Color.getHSBColor(0.09f, .25f, .9f);
            });
            lbl.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    mvTags.setText(mvTags.getText() + " " + ImgTag.getUiString(t));
                    ImportViewController.get().addTagForCurrentImportItem(mvTags.getText());
                }
            });
            lbl.setOpaque(true);
            lbl.setBorder(new TextBubbleBorder(lbl.getForeground(), 1, 15));
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD).deriveFont(14f));
            mvRecommendedContainer.add(lbl);
        });
    }

    private Component createImportItemEditView() {

        JPanel p = new JPanel(new BorderLayout(), true);

        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        ivImage.setPreferredSize(new Dimension(512, 512));

        p.add(ivImage, BorderLayout.CENTER);

        JPanel pTags = new JPanel(new BorderLayout(), true);

        mvTags.setLineWrap(true);
        mvTags.setWrapStyleWord(true);

        JScrollPane jsp = new JScrollPane(mvTags, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        mvAuto.setReturnAction(tag -> {
            if (tag == null || tag.isBlank()) return;
            String cleaned = tag.trim().toLowerCase().replaceAll("\\s+", "_");
            String s       = mvTags.getText().trim().toLowerCase().replaceAll("\\s+", " ") + " " + cleaned;
            mvTags.setText(s);
            ImportViewController.get().addTagForCurrentImportItem(s);
            mvAuto.setText("");
        });

        pTags.add(mvAuto, BorderLayout.NORTH);
        pTags.add(jsp, BorderLayout.CENTER);

        JPanel pRecommended = new JPanel(new BorderLayout(), true);

        mvRecommendedContainer.setBorder(BorderFactory.createTitledBorder("Recommended"));

        pRecommended.add(mvRating, BorderLayout.NORTH);
        pRecommended.add(mvRecommendedContainer, BorderLayout.CENTER);

        JPanel pMetaContainer = new JPanel(new GridLayout(1, 2), true);

        pMetaContainer.setPreferredSize(new Dimension(0, 125));

        pMetaContainer.add(pTags);
        pMetaContainer.add(pRecommended);

        JPanel pCtrl = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnImport = new JButton("Import");
        btnImport.addActionListener(e -> ImportViewController.get().importCurrentImportItem());

        pCtrl.add(btnImport);

        p.add(pMetaContainer, BorderLayout.SOUTH);

        JPanel pContainer = new JPanel(new BorderLayout(), true);

        pContainer.add(p, BorderLayout.CENTER);
        pContainer.add(pCtrl, BorderLayout.SOUTH);

        return pContainer;
    }

    private Component createFileView() {
        fvList.setCellRenderer(new ImportItemListRenderer());
        fvList.addListSelectionListener(fvSelectionChanged());

        return new JScrollPane(
                fvList,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
    }

    private ListSelectionListener fvSelectionChanged() {
        return e -> {
            int idx = fvList.getSelectedIndex();
            if (idx == -1) return;
            ImportItem elementAt = fvListModel.getElementAt(idx);
            ImportViewController.get().updateUi(idx, elementAt);
        };
    }

    public void updateFiles(List<ImportItem> items) {
        fvListModel.setSize(0);
        fvListModel.addAll(items);
    }

    public void selectIndex(int i) {
        if (fvList.getSelectedIndex() == i) return;
        fvList.setSelectedIndex(i);
    }

    public ImportItem getSelectedImportItem() {
        return fvList.getSelectedValue();
    }

    public void updateAutocompletion(Set<ImgTag> allTags) {
        mvAuto.clearElements();
        mvAuto.setElements(allTags);
    }

    public void removeCurrentImportItem() {
        int idx = fvList.getSelectedIndex();
        fvListModel.remove(idx);
        fvList.setSelectedIndex(idx);
    }

    static class ImportItemListRenderer extends JPanel implements ListCellRenderer<ImportItem> {

        private final JLabel lblName = new JLabel();
        private final JLabel lblPath = new JLabel();

        public ImportItemListRenderer() {
            super(new FlowLayout(FlowLayout.LEFT), true);
            add(lblName);
            add(lblPath);
            lblPath.setFont(lblPath.getFont().deriveFont(Font.ITALIC));
            lblPath.setForeground(Color.gray);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ImportItem> list, ImportItem value, int index, boolean isSelected, boolean cellHasFocus) {
            lblName.setText(value.getFile().getName());
            lblPath.setText(value.getFile().getParent());
            lblName.setForeground(isSelected ? Color.white : Color.black);
            lblPath.setForeground(isSelected ? Color.lightGray : Color.gray);
            setBackground(isSelected ? new Color(0, 156, 200) : Color.WHITE);
            return this;
        }
    }
}
