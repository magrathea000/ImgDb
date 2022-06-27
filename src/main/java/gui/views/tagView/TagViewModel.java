package gui.views.tagView;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import model.ImgTag;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class TagViewModel {

    private List<ImgTag> tags;

    public TagViewModel() {
        tags = new ArrayList<>();
    }

    public void setTags(List<ImgTag> tags) {
        this.tags = tags;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    static
    class TagViewItem {
        private ImgTag  tag;
        private boolean changed;
        private boolean delete;
    }

}
