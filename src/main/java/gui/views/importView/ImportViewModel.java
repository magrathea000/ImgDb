package gui.views.importView;

import lombok.*;
import model.ImgTag;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
public class ImportViewModel {

    private final Map<File, ImportItem> items;

    private ImportItem currentImportItem;

    public ImportViewModel() {
        items = new HashMap<>();
    }

    public ImportItem newImportItem(File f) {
        ImportItem importItem = new ImportItem(f);
        items.put(f, importItem);
        return importItem;
    }

    public ImportItem getImportItem(File f) {
        return items.get(f);
    }

    public boolean hasImportItem(File f) {
        return items.containsKey(f);
    }

    public ImportItem getCurrentImportItem() {
        return currentImportItem;
    }

    public void setSelectedImportItem(ImportItem selected) {
        currentImportItem = selected;
    }

    public void clearImportItems() {
        items.clear();
    }

    public void removeCurrentImportItem() {
        items.remove(currentImportItem.file);
        currentImportItem = null;
    }

    @Getter
    @Setter
    @RequiredArgsConstructor()
    @AllArgsConstructor
    static class ImportItem {
        @NonNull
        private File          file;
        private BufferedImage img;
        private int           rating;
        private Set<ImgTag>   tags            = new HashSet<>(0);
        private Set<ImgTag>   recommendations = new HashSet<>(0);
    }

}
