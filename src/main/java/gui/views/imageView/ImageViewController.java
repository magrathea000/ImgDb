package gui.views.imageView;

import database.DatabaseController;
import gui.views.ViewController;
import model.ImgTag;
import model.Metadata;
import model.Pixeldata;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ImageViewController implements ViewController {

    private static ImageViewController instance;

    final ImageViewPanel v;
    final ImageViewModel m;

    private ImageViewController() {
        v = new ImageViewPanel();
        m = new ImageViewModel();
    }

    public static ImageViewController get() {
        if (instance == null) {
            instance = new ImageViewController();
        }
        return instance;
    }

    @Override
    public Component getComponent() {
        return v;
    }

    @Override
    public void activate() {
        v.setEnabled(m.getMetadata() != null);
    }

    public void showImage(final Pixeldata px) {

        v.setImage(px.getImage());
        DatabaseController dbc = DatabaseController.getInstance();

        Optional<Metadata> metadataByImageId = dbc.getMetadataByImageId(px.getImageId());
        metadataByImageId.ifPresent(meta -> {
            m.setPixeldata(px);
            m.setMetadata(meta);
            v.updateMetaView(meta);
            List<ImgTag> tagsByNames = dbc.getTagsByNames(meta.getTags());
            m.setTags(new HashSet<>(tagsByNames));
            v.setTags(tagsByNames);
        });
    }

    public Optional<ImgTag> addTag(final String tag) {
        ImgTag             imgTag = new ImgTag(tag);
        DatabaseController dbc    = DatabaseController.getInstance();
        boolean            b      = dbc.addTagForImage(m.getMetadata().getImageId(), imgTag);
        if (b) {
            Optional<Metadata> metadataByImageId = dbc.getMetadataByImageId(m.getMetadata().getImageId());
            metadataByImageId.ifPresent(m::setMetadata);
            List<ImgTag> tagsByNames = dbc.getTagsByNames(m.getMetadata().getTags());
            m.setTags(new HashSet<>(tagsByNames));
            v.setTags(tagsByNames);
            return Optional.of(imgTag);
        }
        return Optional.empty();
    }

    public List<ImgTag> getImageTags() {
        Set<String>  tags        = m.getMetadata().getTags();
        List<ImgTag> tagsByNames = DatabaseController.getInstance().getTagsByNames(tags);
        return tagsByNames;
    }

    public void removeTag(final ImgTag t) {
        DatabaseController dbc = DatabaseController.getInstance();
        dbc.removeTagFromImage(m.getMetadata().getImageId(), t);
        Optional<Metadata> metadataByImageId = dbc.getMetadataByImageId(m.getMetadata().getImageId());
        metadataByImageId.ifPresent(m::setMetadata);
        List<ImgTag> tagsByNames = dbc.getTagsByNames(m.getMetadata().getTags());
        m.setTags(new HashSet<>(tagsByNames));
        v.setTags(tagsByNames);
    }
}
