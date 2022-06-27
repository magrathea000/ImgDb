package model;

import lombok.*;
import org.dizitart.no2.NitriteId;

import java.util.Set;

@Getter
@AllArgsConstructor
@Builder
@Setter
@ToString
public class Metadata {

    private NitriteId id;

    private NitriteId imageId;

    private int witdh;

    private int height;

    private int rating;

    private long lastModified;

    private Set<String> tags;

    public Metadata(int witdh, int height, int rating, Set<String> tags) {
        this.witdh = witdh;
        this.height = height;
        this.rating = rating;
        this.tags = tags;
    }
}
