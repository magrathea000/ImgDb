package gui.views.tagView;

import model.ImgTag;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class TagViewTable extends JTable {

    private final TagTableModel model;

    private final boolean[] sortOrder;

    public TagViewTable() {
        model = new TagTableModel();

        getTableHeader().setReorderingAllowed(false);

        setModel(model);
        sortOrder = new boolean[model.getColumnCount()];

        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TagViewTable tt = this;
        getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                int     i        = columnAtPoint(e.getPoint());
                boolean reversed = false;
                for (int j = 0; j < sortOrder.length; j++) {
                    if (j == i) {
                        if (sortOrder[j]) {
                            sortOrder[j] = false;
                            reversed = true;
                        } else sortOrder[j] = true;
                    } else {
                        sortOrder[j] = false;
                    }
                }
                var sorted = model.getItems().stream().sorted(model.getColumnComparator(i, reversed)).toList();
                model.setRows(sorted);
            }
        });

        JComboBox<String> cat = new JComboBox<>(new String[]{"Artist", "Character", "Meta", "General"});
        getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(cat));

    }

    static class TagTableModel extends AbstractTableModel {

        public static final int COL_NAME     = 0;
        public static final int COL_CATEGORY = 1;
        public static final int COL_COUNT    = 2;
        public static final int COL_DELETE   = 3;

        private static final String[]                       headers = {"Name", "Category", "Count", "Delete"};
        private final        List<TagViewModel.TagViewItem> items   = new ArrayList<>();

        public void removeRow(int idx) {
            items.remove(idx);
            fireTableRowsDeleted(idx, idx);
        }

        @Override
        public int getRowCount() {
            return items.size();
        }

        @Override
        public int getColumnCount() {
            return headers.length;
        }

        @Override
        public String getColumnName(final int column) {
            return headers[column];
        }

        @Override
        public Class<?> getColumnClass(final int columnIndex) {
            return switch (columnIndex) {
                case COL_NAME, COL_CATEGORY -> String.class;
                case COL_COUNT -> Integer.class;
                case COL_DELETE -> Boolean.class;
                default -> throw new IllegalStateException("Unexpected value: " + columnIndex);
            };
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            TagViewModel.TagViewItem item = items.get(rowIndex);
            return switch (columnIndex) {
                case COL_NAME -> item.getTag().getName();
                case COL_CATEGORY -> item.getTag().getCategory().name();
                case COL_COUNT -> item.getTag().getCount();
                case COL_DELETE -> item.isDelete();
                default -> null;
            };
        }

        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex) {
            return columnIndex != 2;
        }

        public TagViewModel.TagViewItem getRow(final int row) {
            return items.get(row);
        }

        public void setRows(Collection<TagViewModel.TagViewItem> items) {
            this.items.clear();
            this.items.addAll(items);
            fireTableRowsInserted(0, items.size());
        }

        @Override
        public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
            var item = items.get(rowIndex);
            switch (columnIndex) {
                case COL_NAME -> item.getTag().setName((String) aValue);
                case COL_CATEGORY -> item.getTag().setCategory(ImgTag.Category.valueOf((String) aValue));
                case COL_DELETE -> item.setDelete((Boolean) aValue);
            }
            item.setChanged(true);
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        public List<TagViewModel.TagViewItem> getItems() {
            return items;
        }

        public Comparator<? super TagViewModel.TagViewItem> getColumnComparator(final int column, final boolean reversed) {
            return switch (column) {
                case COL_NAME -> (e1, e2) -> reversed ?
                        e2.getTag().getName().compareTo(e1.getTag().getName()) :
                        e1.getTag().getName().compareTo(e2.getTag().getName());
                case COL_CATEGORY -> (e1, e2) -> reversed ?
                        e2.getTag().getCategory().compareTo(e1.getTag().getCategory()) :
                        e1.getTag().getCategory().compareTo(e2.getTag().getCategory());
                case COL_DELETE -> (e1, e2) -> reversed ?
                        Boolean.compare(e2.isDelete(), e1.isDelete()) :
                        Boolean.compare(e1.isDelete(), e2.isDelete());
                case COL_COUNT -> (e1, e2) -> reversed ?
                        Integer.compare(e2.getTag().getCount(), e1.getTag().getCount()) :
                        Integer.compare(e1.getTag().getCount(), e2.getTag().getCount());
                default -> throw new IllegalStateException("Unexpected value: " + column);
            };
        }
    }

}
