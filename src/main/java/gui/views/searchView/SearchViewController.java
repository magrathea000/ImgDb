package gui.views.searchView;

import database.DatabaseController;
import gui.views.ViewController;
import lombok.extern.slf4j.Slf4j;
import model.ImgTag;
import model.Metadata;
import model.Pixeldata;
import org.dizitart.no2.NitriteId;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@Slf4j
public class SearchViewController implements ViewController {

    private static SearchViewController instance;

    final SearchViewPanel v;
    final SearchViewModel m;

    private SearchViewController() {
        v = new SearchViewPanel();
        m = new SearchViewModel();
    }

    public static SearchViewController get() {
        if (instance == null) {
            instance = new SearchViewController();
        }
        return instance;
    }

    @Override
    public Component getComponent() {
        return v;
    }

    @Override
    public void activate() {
        if (m.getFoundMetadata().isEmpty()) {
            search();
        }
    }

    public void search(List<String> searchTags) {
        v.setSearchTags(searchTags);
        search();
    }

    public void search() {
        ForkJoinPool.commonPool().execute(() -> {
            List<String> searchTags = v.getSearchTags();
            log.info("Searching for tags: {}", searchTags);
            DatabaseController dbc            = DatabaseController.getInstance();
            List<Metadata>     metadataByTags = dbc.getMetadataByTags(new HashSet<>(searchTags));

            Set<NitriteId> imgIds = metadataByTags.stream()
                                                  .sorted((e1, e2) -> Long.compare(e2.getLastModified(), e1.getLastModified()))
                                                  .limit(20)
                                                  .map(Metadata::getImageId)
                                                  .collect(Collectors.toSet());

            List<Pixeldata> pixeldata = dbc.getPixeldataByImageIds(imgIds);

            Set<String>  imgTags   = metadataByTags.stream().flatMap(m -> m.getTags().stream()).collect(Collectors.toSet());
            List<ImgTag> foundTags = dbc.getTagsByNames(imgTags);

            m.setSearchTerms(searchTags);
            m.setFoundMetadata(metadataByTags);
            m.setFoundPixeldata(pixeldata);
            m.setFoundTags(foundTags);

            showImages(pixeldata, foundTags);
        });

    }

    public void showImages(List<Pixeldata> pixeldata, List<ImgTag> tags) {
        v.showImages(pixeldata);
        v.showTags(tags);
    }
}
