package gui.views.searchView;

import lombok.Getter;
import lombok.Setter;
import model.ImgTag;
import model.Metadata;
import model.Pixeldata;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class SearchViewModel {

    private List<String>    searchTerms;
    private List<ImgTag>    foundTags;
    private List<Metadata>  foundMetadata;
    private List<Pixeldata> foundPixeldata;

    public SearchViewModel() {
        searchTerms = new ArrayList<>();
        foundTags = new ArrayList<>();
        foundMetadata = new ArrayList<>();
        foundPixeldata = new ArrayList<>();
    }


}
