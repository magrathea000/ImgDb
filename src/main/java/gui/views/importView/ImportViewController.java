package gui.views.importView;

import database.DatabaseController;
import gui.views.ViewController;
import model.ImgTag;
import model.Metadata;
import model.Pixeldata;
import utils.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class ImportViewController implements ViewController {

    private static ImportViewController instance;

    final ImportViewPanel v;
    final ImportViewModel m;

    private ImportViewController() {
        v = new ImportViewPanel();
        m = new ImportViewModel();
    }

    public static ImportViewController get() {
        if (instance == null) {
            instance = new ImportViewController();
        }
        return instance;
    }

    public void setFiles(Collection<File> files) {
        m.clearImportItems();
        List<ImportViewModel.ImportItem> importItems = files.stream().distinct().map(m::newImportItem).toList();
        v.updateFiles(importItems);
        selectIndex(0);
    }

    public void selectIndex(int i) {
        v.updateImportItem(m.getCurrentImportItem());
        v.selectIndex(i);
        ImportViewModel.ImportItem importItem = v.getSelectedImportItem();
        if (importItem.getImg() == null) {
            importItem.setImg(ImageUtils.loadImage(importItem.getFile()));
        }
        m.setSelectedImportItem(importItem);
        v.updateMetadataUi(m);
        v.updateAutocompletion(DatabaseController.getInstance().getAllTags());
    }

    @Override
    public Component getComponent() {
        return v;
    }

    @Override
    public void activate() {

    }

    public void addTagForCurrentImportItem(String currentTags) {
        DatabaseController dbc = DatabaseController.getInstance();
        var imgTags = Arrays.stream(currentTags.trim().toLowerCase().split("\\s+"))
                            .map(s -> dbc.getTagByName(s).orElse(new ImgTag(s))).collect(Collectors.toSet());

        m.getCurrentImportItem().setTags(imgTags);
        Set<ImgTag> recommendedTags = new HashSet<>(DatabaseController.getInstance().getRecommendedTags(imgTags));
        m.getCurrentImportItem().setRecommendations(recommendedTags);
        v.updateMetadataUi(m);
        v.updateRecommendedTags(recommendedTags);
    }

    public void importCurrentImportItem() {

        ImportViewModel.ImportItem ii = m.getCurrentImportItem();
        v.updateImportItem(ii);

        if (ii.getRating() == 0) {
            JOptionPane.showMessageDialog(v, "Rating must not be 0!", "Import Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Set<ImgTag> tags = ii.getTags();
        if (tags == null || tags.isEmpty()) {
            JOptionPane.showMessageDialog(v, "Rating must not be empty!", "Import Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DatabaseController dbc = DatabaseController.getInstance();

        BufferedImage img     = ii.getImg();
        Set<String>   strTags = tags.stream().map(ImgTag::getUiString).collect(Collectors.toSet());

        Metadata metadata = new Metadata(img.getWidth(), img.getHeight(), ii.getRating(), strTags);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageUtils.writeImage(img, "qoi", baos);

        Pixeldata pixeldata = new Pixeldata(baos.toByteArray());

        boolean store = dbc.store(metadata, pixeldata);
        if (store) {
            m.removeCurrentImportItem();
            v.removeCurrentImportItem();
        } else {
            JOptionPane.showMessageDialog(v, "Import error!", "Import Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    public void updateUi(int idx, ImportViewModel.ImportItem importItem) {
        v.updateImportItem(m.getCurrentImportItem());
        m.setSelectedImportItem(importItem);
        if (importItem.getImg() == null) {
            importItem.setImg(ImageUtils.loadImage(importItem.getFile()));
        }
        v.updateMetadataUi(m);
        v.updateAutocompletion(DatabaseController.getInstance().getAllTags());
    }

}
