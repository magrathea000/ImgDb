package database;

import model.ImgTag;
import model.Metadata;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteCollection;
import org.dizitart.no2.NitriteId;

import java.util.*;
import java.util.stream.Collectors;

import static org.dizitart.no2.filters.Filters.eq;

public class TagRepository {

    private final NitriteCollection cMetadata;
    private final NitriteCollection cPixeldata;
    private final NitriteCollection cTags;

    TagRepository(Database db) {
        cMetadata = db.cMetadata;
        cPixeldata = db.cPixeldata;
        cTags = db.cTags;
    }

    void store(Set<String> tags) {
        tags.stream()
            .map(t -> {
                String[] split = t.split(":");

                String          name = ImgTag.getNameFromString(t);
                ImgTag.Category cat  = ImgTag.getCategoryFromString(t);

                Document d = cTags.find(eq("name", name)).firstOrDefault();
                if (d != null) {
                    d.put("count", d.get("count", Integer.class) + 1);
                    return d;
                }

                return Mapper.from(new ImgTag(NitriteId.newId(), name, 1, cat));
            })
            .filter(Objects::nonNull)
            .forEach(d -> {
                if (cTags.update(d, true).getAffectedCount() != 0)
                    DatabaseController.getInstance().cacheTag(Mapper.docToTag(d));
            });
    }

    public List<ImgTag> getByNames(Set<String> tags) {
        if (tags == null || tags.isEmpty()) return new ArrayList<>(0);
        return tags.stream()
                   .distinct()
                   .map(t -> cTags.find(eq("name", t)).firstOrDefault())
                   .filter(Objects::nonNull)
                   .map(Mapper::docToTag)
                   .toList();
    }

    public Set<ImgTag> getAll() {
        return cTags.find().toList().stream().map(Mapper::docToTag).collect(Collectors.toSet());
    }

    public Optional<ImgTag> getByName(String tag) {
        return Optional.ofNullable(Mapper.docToTag(cTags.find(eq("name", ImgTag.getNameFromString(tag))).firstOrDefault()));
    }

    public void deleteTag(final ImgTag tag) throws Exception {
        List<Metadata>     metadataWithTags = DatabaseController.getInstance().getMetadataWithTags(Set.of(tag.getName()));
        Optional<Metadata> any              = metadataWithTags.stream().filter(m -> m.getTags().size() < 2).findAny();
        if (any.isPresent()) {
            throw new Exception("Can't delete tag. Image " + any.get().getImageId() + "would have no tags!");
        }

        metadataWithTags.forEach(m -> {
            m.getTags().remove(tag.getName());
            cMetadata.update(eq("_id", m.getId().getIdValue()), Mapper.from(m));
        });

        cTags.remove(eq("_id", tag.getId().getIdValue()));
    }

    public void updateTag(final ImgTag tag) throws Exception {

        Document dt     = cTags.getById(tag.getId());
        ImgTag   imgTag = Mapper.docToTag(dt);

        if (imgTag == null) {
            throw new Exception("Can't find tag with id " + tag.getId() + "!");
        }

        List<Metadata> metadataWithTags = DatabaseController.getInstance().getMetadataWithTags(Set.of(imgTag.getName()));

        metadataWithTags.forEach(m -> {
            m.getTags().remove(imgTag.getName());
            m.getTags().add(tag.getName());
            cMetadata.update(eq("_id", m.getId().getIdValue()), Mapper.from(m));
        });

        DatabaseController.getInstance().invalidateMetadataCache();

        cTags.update(eq("_id", tag.getId().getIdValue()), Mapper.from(tag));
    }

    public void store(final ImgTag tag) {
        cTags.update(Mapper.from(tag), true);
    }
}
