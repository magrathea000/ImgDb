package database;

import model.ImgTag;
import model.Metadata;
import model.Pixeldata;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;

import java.util.Set;
import java.util.stream.Collectors;

public class Mapper {

    public static final String KEY_ID           = "_id";
    public static final String KEY_IMAGE_ID     = "imageId";
    public static final String KEY_META_WIDTH   = "width";
    public static final String KEY_META_HEIGHT  = "height";
    public static final String KEY_META_RATING  = "rating";
    public static final String KEY_META_TAGS    = "tags";
    public static final String KEY_PX_DATA      = "data";
    public static final String KEY_TAG_NAME     = "name";
    public static final String KEY_TAG_COUNT    = "count";
    public static final String KEY_TAG_CATEGORY = "category";

    public static Document from(Metadata m) {
        Set<String> cleanedTags = m.getTags().stream().map(ImgTag::getNameFromString).collect(Collectors.toSet());
        return new Document().put(KEY_META_HEIGHT, m.getHeight())
                             .put(KEY_META_WIDTH, m.getWitdh())
                             .put(KEY_META_RATING, m.getRating())
                             .put(KEY_META_TAGS, cleanedTags)
                             .put(KEY_IMAGE_ID, m.getImageId().getIdValue())
                             .put(KEY_ID, m.getId().getIdValue());
    }

    public static Document from(Pixeldata p) {
        return new Document().put(KEY_PX_DATA, p.getData())
                             .put(KEY_IMAGE_ID, p.getImageId().getIdValue())
                             .put(KEY_ID, p.getId().getIdValue());
    }

    public static Document from(ImgTag t) {
        return new Document().put(KEY_TAG_NAME, t.getName())
                             .put(KEY_TAG_COUNT, t.getCount())
                             .put(KEY_TAG_CATEGORY, t.getCategory())
                             .put(KEY_ID, t.getId().getIdValue());
    }

    public static Metadata docToMeta(Document d) {
        return d == null || d.isEmpty() ? null :
                Metadata.builder()
                        .id(d.getId())
                        .imageId(NitriteId.createId(d.get(KEY_IMAGE_ID, Long.class)))
                        .height(d.get(KEY_META_HEIGHT, Integer.class))
                        .witdh(d.get(KEY_META_WIDTH, Integer.class))
                        .rating(d.get(KEY_META_RATING, Integer.class))
                        .lastModified(d.getLastModifiedTime())
                        .tags((Set<String>) d.get(KEY_META_TAGS))
                        .build();
    }

    public static Pixeldata docToPx(Document d) {
        return d == null || d.isEmpty() ? null :
                Pixeldata.builder()
                         .id(d.getId())
                         .imageId(NitriteId.createId(d.get(KEY_IMAGE_ID, Long.class)))
                         .data(d.get(KEY_PX_DATA, byte[].class))
                         .build();
    }

    public static ImgTag docToTag(Document d) {
        return d == null || d.isEmpty() ? null :
                ImgTag.builder()
                      .id(d.getId())
                      .name(d.get(KEY_TAG_NAME, String.class))
                      .count(d.get(KEY_TAG_COUNT, Integer.class))
                      .category(d.get(KEY_TAG_CATEGORY, ImgTag.Category.class))
                      .build();
    }

}
