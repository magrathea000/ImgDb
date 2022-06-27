package database;

import lombok.extern.slf4j.Slf4j;
import model.ImgTag;
import model.Metadata;
import org.dizitart.no2.*;
import org.dizitart.no2.filters.Filters;
import org.dizitart.no2.util.Iterables;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.dizitart.no2.filters.Filters.elemMatch;
import static org.dizitart.no2.filters.Filters.eq;

@Slf4j
public class MetadataRepository {

    private final NitriteCollection cMetadata;
    private final NitriteCollection cPixeldata;
    private final NitriteCollection cTags;

    MetadataRepository(Database db) {
        cMetadata = db.cMetadata;
        cPixeldata = db.cPixeldata;
        cTags = db.cTags;
    }

    boolean store(Metadata m) {
        WriteResult update = cMetadata.update(Mapper.from(m), true);
        NitriteId   id     = Iterables.firstOrDefault(update);
        return id != null;
    }

    public List<Metadata> getByTags(Set<String> tags) {
        if (tags == null || tags.isEmpty() || (String.join("", tags).isBlank())) return getRecent();
        Filter filter = Filters.and(tags.stream()
                                        .map(t -> elemMatch("tags", eq("$", ImgTag.getNameFromString(t))))
                                        .toArray(Filter[]::new));
        List<Metadata> metadata = cMetadata.find(filter, FindOptions.sort("_modified", SortOrder.Descending))
                                           .toList()
                                           .stream()
                                           .map(Mapper::docToMeta)
                                           .toList();
        log.info("Search for {}. Result -> {} entries. {}", tags, metadata.size(), metadata.stream().map(Metadata::getImageId).toList());
        return metadata;
    }

    public List<Metadata> getRecent() {
        List<Metadata> metadata = cMetadata.find(FindOptions.sort("_modified", SortOrder.Descending)).toList()
                                           .stream()
                                           .map(Mapper::docToMeta)
                                           .toList();
        log.info("Search for recent. Result -> {} entries. {}", metadata.size(), metadata.stream().map(Metadata::getImageId).toList());
        return metadata;
    }

    public Optional<Metadata> getByImageId(NitriteId id) {
        if (id == null) return Optional.empty();
        Document d = cMetadata.find(Filters.eq("imageId", id.getIdValue())).firstOrDefault();
        return Optional.ofNullable(Mapper.docToMeta(d));
    }

    public List<Metadata> getByImageIds(Set<NitriteId> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>(0);
        Long[] lids = ids.stream().map(NitriteId::getIdValue).toArray(Long[]::new);
        return cMetadata.find(Filters.in("imageId", lids))
                        .toList()
                        .stream()
                        .map(Mapper::docToMeta)
                        .toList();
    }

    public List<Metadata> getWithTags(Set<String> tags) {
        if (tags == null || tags.isEmpty() || (String.join("", tags).isBlank())) return getRecent();
        Filter filter = Filters.or(tags.stream()
                                       .map(t -> elemMatch("tags", eq("$", ImgTag.getNameFromString(t))))
                                       .toArray(Filter[]::new));
        List<Metadata> metadata = cMetadata.find(filter)
                                           .toList()
                                           .stream()
                                           .map(Mapper::docToMeta)
                                           .toList();
        log.info("Search for {}. Result -> {} entries. {}", tags, metadata.size(), metadata.stream().map(Metadata::getImageId).toList());
        return metadata;
    }

}
