package gui.views.tagView;

import database.DatabaseController;
import model.ImgTag;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Set;

public class TagViewPanel extends JPanel {

    private final TagViewTable tbl;

    public TagViewPanel() {
        super(new BorderLayout(), true);

        JLabel header = new JLabel("All Tags", JLabel.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD).deriveFont(21f));
        add(header, BorderLayout.NORTH);

        tbl = new TagViewTable();
        JScrollPane jsp = new JScrollPane(tbl, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(jsp, BorderLayout.CENTER);

        JPanel pCtrl = new JPanel(new BorderLayout(), true);

        JButton btnUpdate = new JButton("Update");
        btnUpdate.addActionListener(updateAction());

        JButton btnDelete = new JButton("Delete");
        btnDelete.addActionListener(deleteAction());

        pCtrl.add(btnDelete, BorderLayout.WEST);
        pCtrl.add(btnUpdate, BorderLayout.EAST);
        pCtrl.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        add(pCtrl, BorderLayout.SOUTH);
    }

    private ActionListener deleteAction() {
        return e -> {
            int c = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete all selected tags?", "Delete tags...", JOptionPane.YES_NO_OPTION);
            if (c != JOptionPane.YES_OPTION) return;

            var model = (TagViewTable.TagTableModel) tbl.getModel();

            for (int i = model.getRowCount() - 1; i >= 0; i--) {
                if ((Boolean) model.getValueAt(i, TagViewTable.TagTableModel.COL_DELETE)) {
                    var     row = model.getRow(i);
                    ImgTag  tag = row.getTag();
                    boolean b   = DatabaseController.getInstance().deleteTag(tag);
                    if (b)
                        model.removeRow(i);
                }
            }

        };
    }

    private ActionListener updateAction() {
        return e -> {
            var model = (TagViewTable.TagTableModel) tbl.getModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                var item = model.getRow(i);
                if (item.isChanged()) {
                    DatabaseController.getInstance().updateTag(item.getTag());
                }
            }
        };
    }

    public void setTags(final Set<ImgTag> allTags) {
        var model = (TagViewTable.TagTableModel) tbl.getModel();
        model.setRows(allTags.stream()
                             .sorted((e1, e2) -> Integer.compare(e2.getCount(), e1.getCount()))
                             .map(t -> new TagViewModel.TagViewItem(t, false, false))
                             .toList());
    }
}
