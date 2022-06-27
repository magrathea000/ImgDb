package gui.views.imageView;

import lombok.Getter;
import lombok.Setter;
import model.ImgTag;
import model.Metadata;
import model.Pixeldata;

import java.util.Set;

@Getter
@Setter
public class ImageViewModel {

    private Metadata    metadata;
    private Pixeldata   pixeldata;
    private Set<ImgTag> tags;

}
