package database;

import lombok.extern.slf4j.Slf4j;
import model.ImgTag;
import model.Metadata;
import model.Pixeldata;
import org.dizitart.no2.NitriteId;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class DatabaseController {

    private static DatabaseController instance;

    private final Database db;

    private final Map<NitriteId, Metadata>      cacheMetadata;
    private final Map<String, ImgTag>           cacheTag;
    private final Map<NitriteId, Pixeldata>     cachePixeldata;
    private final Map<NitriteId, BufferedImage> cacheImages;

    private DatabaseController() {
        db = new Database();
        cacheMetadata = new HashMap<>();
        cacheTag = new HashMap<>();
        cachePixeldata = new HashMap<>();
        cacheImages = new HashMap<>();
    }

    public static DatabaseController getInstance() {
        if (instance == null) {
            instance = new DatabaseController();
        }
        return instance;
    }

    public List<Metadata> getMetadataByTags(Set<String> tags) {
        return db.metadata.getByTags(tags);
    }

    public List<Metadata> getMetadataWithTags(Set<String> tags) {
        return db.metadata.getWithTags(tags);
    }

    private List<Metadata> getRecentMetadata() {
        return db.metadata.getRecent();
    }

    public Optional<Metadata> getMetadataByImageId(NitriteId id) {
        return db.metadata.getByImageId(id);
    }

    public List<Metadata> getMetadataByImageIds(Set<NitriteId> ids) {
        return db.metadata.getByImageIds(ids);
    }

    public List<Pixeldata> getPixeldataByImageIds(Set<NitriteId> ids) {

        Set<NitriteId>  idsNotInCache = ids.stream().filter(id -> !cachePixeldata.containsKey(id)).collect(Collectors.toSet());
        List<Pixeldata> pxNotInCache  = db.pixeldata.getByImageIds(idsNotInCache);

        pxNotInCache.forEach(px -> cachePixeldata.put(px.getImageId(), px));

        return cachePixeldata.entrySet().stream()
                             .filter(p -> ids.contains(p.getKey()))
                             .map(Map.Entry::getValue)
                             .toList();
    }

    public Optional<Pixeldata> getPixeldataByImageId(NitriteId id) {
        if (cachePixeldata.containsKey(id))
            return cachePixeldata.entrySet()
                                 .stream()
                                 .filter(p -> p.getKey().equals(id))
                                 .map(Map.Entry::getValue)
                                 .findFirst();
        return db.pixeldata.getByImageId(id);
    }

    public Optional<Pixeldata> getPixeldataById(NitriteId id) {
        Optional<Pixeldata> byId = db.pixeldata.getById(id);
        byId.ifPresent(p -> cachePixeldata.put(p.getImageId(), p));
        return byId;
    }

    public List<ImgTag> getTagsByNames(Set<String> tags) {
        tags.stream()
            .filter(s -> !cacheTag.containsKey(s))
            .map(db.tags::getByName)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(this::cacheTag);
        return tags.stream().map(cacheTag::get).filter(Objects::nonNull).toList();
    }

    public Optional<ImgTag> getTagByName(String tag) {
        var t = cacheTag.get(ImgTag.getNameFromString(tag));
        if (t != null) return Optional.of(t);
        Optional<ImgTag> byName = db.tags.getByName(tag);
        byName.ifPresent(this::cacheTag);
        return byName;
    }

    public Set<ImgTag> getAllTags() {
        if (cacheTag.isEmpty()) {
            db.tags.getAll().forEach(t -> cacheTag.put(t.getName(), t));
        }
        return new HashSet<>(cacheTag.values());
    }

    public boolean store(Metadata m, Pixeldata p) {

        Objects.requireNonNull(m, "image must not be null");
        Objects.requireNonNull(p, "image must not be null");

        if (m.getId() == null) {
            m.setId(NitriteId.newId());
        }

        if (p.getId() == null) {
            p.setId(NitriteId.newId());
        }

        NitriteId imageId = NitriteId.newId();

        p.setImageId(imageId);
        m.setImageId(imageId);

        if (!db.metadata.store(m)) {
            return false;
        }

        if (!db.pixeldata.store(p)) {
            return false;
        }

        db.tags.store(m.getTags());

        return true;
    }

    public Optional<BufferedImage> getImageByImageId(NitriteId id) {
        return Optional.ofNullable(cacheImages.get(id));
    }

    public void cacheImage(NitriteId imageId, BufferedImage img) {
        if (img == null || imageId == null) return;
        cacheImages.put(imageId, img);
    }

    public void cacheTag(ImgTag t) {
        cacheTag.put(t.getName(), t);
    }

    public List<ImgTag> getRecommendedTags(Set<ImgTag> tags) {
        List<Metadata> metadataWithTags = getMetadataWithTags(tags.stream().map(ImgTag::getName).collect(Collectors.toSet()));
        return metadataWithTags.stream()
                               .flatMap(m -> m.getTags().stream())
                               .distinct().map(this::getTagByName)
                               .filter(Optional::isPresent)
                               .map(Optional::get)
                               .filter(t -> !tags.contains(t))
                               .toList();
    }

    public boolean deleteTag(final ImgTag tag) {
        try {
            db.tags.deleteTag(tag);
            cacheTag.remove(tag.getName());
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Delete tag error...", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean updateTag(final ImgTag tag) {
        try {
            db.tags.updateTag(tag);
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Delete tag error...", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    public boolean addTagForImage(final NitriteId imageId, final ImgTag tag) {
        Optional<Metadata> byImageId = db.metadata.getByImageId(imageId);
        if (byImageId.isEmpty()) return false;

        Metadata metadata = byImageId.get();
        metadata.setTags(new HashSet<>(metadata.getTags()) {{
            add(tag.getName());
        }});
        boolean store = db.metadata.store(metadata);
        if (!store) return false;

        Optional<ImgTag> tagByName = getTagByName(tag.getName());
        tagByName.ifPresentOrElse(this::store, () -> store(tag));
        return true;
    }

    private void store(final ImgTag tag) {
        tag.setCount(tag.getCount() + 1);
        db.tags.store(tag);
        cacheTag.put(tag.getName(), tag);
    }

    public void removeTagFromImage(final NitriteId imageId, final ImgTag tag) {
        Optional<Metadata> byImageId = db.metadata.getByImageId(imageId);
        byImageId.ifPresent(meta -> {
            Set<String> tags   = meta.getTags();
            boolean     remove = tags.remove(tag.getName());
            if (!remove) return;
            boolean store = db.metadata.store(meta);
            if (!store) return;
            cacheMetadata.put(imageId, meta);

            Optional<ImgTag> tagByName = getTagByName(tag.getName());
            tagByName.ifPresent(t -> {
                int count = t.getCount() - 1;
                if (count <= 0) {
                    try {
                        db.tags.deleteTag(t);
                        cacheTag.remove(t.getName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
                t.setCount(count);
                db.tags.store(t);
            });
        });
    }

    public void invalidateMetadataCache() {
        cacheMetadata.clear();
    }
}
