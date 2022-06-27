package gui;

import gui.views.imageView.ImageViewController;
import gui.views.importView.ImportViewController;
import gui.views.searchView.SearchViewController;
import gui.views.tagView.TagViewController;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class MainFrame extends JFrame {

    public static final int VIEW_SEARCH = 0;
    public static final int VIEW_IMAGE  = 1;
    public static final int VIEW_IMPORT = 2;
    public static final int VIEW_TAGS   = 3;

    private final JTabbedPane pContent;

    public MainFrame() throws HeadlessException {

        super("Image Database");

        pContent = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        pContent.setPreferredSize(new Dimension(1280, 720));

        pContent.add("Search", SearchViewController.get().getComponent());
        pContent.add("Image", ImageViewController.get().getComponent());
        pContent.add("Import", ImportViewController.get().getComponent());
        pContent.add("Tags", TagViewController.get().getComponent());

//        pContent = new JPanel(new GridLayout(1, 1), true);

        pContent.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                switch (pContent.getSelectedIndex()) {
                    case 0 -> SearchViewController.get().activate();
                    case 1 -> ImageViewController.get().activate();
                    case 2 -> ImportViewController.get().activate();
                    case 3 -> TagViewController.get().activate();
                }
            }
        });
        SearchViewController.get().activate();

        setContentPane(pContent);

        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        Toolkit.getDefaultToolkit().setDynamicLayout(false);

        makeDropTarget();

//        setVisible(true);

    }

    private void makeDropTarget() {
        setDropTarget(new DropTarget() {

            @Override
            public synchronized void drop(DropTargetDropEvent e) {
                try {
                    e.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable transferable = e.getTransferable();

                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        var droppedFiles = ((List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor))
                                .stream()
                                .flatMap(f -> {
                                    if (f.isFile()) return Stream.of(f);
                                    try (Stream<Path> walkStream = Files.walk(f.toPath())) {
                                        return walkStream.filter(p -> p.toFile().isFile())
                                                         .filter(p -> switch (FilenameUtils.getExtension(p.getFileName().toString())) {
                                                             case "jpg", "jpeg", "jfif", "png" -> true;
                                                             default -> false;
                                                         })
                                                         .map(Path::toFile);
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
                                    return Stream.of();
                                })
                                .sorted()
                                .toList();

                        if (droppedFiles.isEmpty()) return;

                        ImportViewController.get().setFiles(droppedFiles);
                        setView(MainFrame.VIEW_IMPORT);
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void setView(int idx) {
        pContent.setSelectedIndex(idx);
        revalidate();
        repaint();
    }
}
