package database;

import model.Pixeldata;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteCollection;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.WriteResult;
import org.dizitart.no2.filters.Filters;
import org.dizitart.no2.util.Iterables;

import java.util.*;

public class PixeldataRepository {

    private final NitriteCollection cMetadata;
    private final NitriteCollection cPixeldata;
    private final NitriteCollection cTags;

    PixeldataRepository(Database db) {
        cMetadata = db.cMetadata;
        cPixeldata = db.cPixeldata;
        cTags = db.cTags;
    }

    boolean store(Pixeldata p) {
        WriteResult update = cPixeldata.update(Mapper.from(p), true);
        NitriteId   id     = Iterables.firstOrDefault(update);
        return id != null;
    }

    public List<Pixeldata> getByImageIds(Set<NitriteId> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>(0);
        return ids.stream()
                  .map(NitriteId::getIdValue)
                  .map(l -> cPixeldata.find(Filters.eq("imageId", l)).firstOrDefault())
                  .filter(Objects::nonNull)
                  .map(Mapper::docToPx)
                  .toList();

//        return cPixeldata.find(Filters.in("imageId", lids))
//                         .toList()
//                         .stream()
//                         .map(Mapper::docToPx)
//                         .toList();
    }

    public Optional<Pixeldata> getByImageId(NitriteId id) {
        if (id == null) return Optional.empty();
        Document d = cPixeldata.find(Filters.eq("imageId", id.getIdValue())).firstOrDefault();
        return Optional.ofNullable(Mapper.docToPx(d));
    }


    public Optional<Pixeldata> getById(NitriteId id) {
        if (id == null) return Optional.empty();
        Document d = cPixeldata.getById(id);
        return Optional.ofNullable(Mapper.docToPx(d));
    }

}
