package model;

import lombok.*;
import org.dizitart.no2.NitriteId;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class ImgTag implements Comparable<ImgTag> {

    private NitriteId id;
    private String    name;
    private int       count;
    private Category  category;

    public ImgTag(String uiString) {
        this(NitriteId.newId(), getNameFromString(uiString), 0, getCategoryFromString(uiString));
    }

    public static String getNameFromString(String s) {
        int i = s.indexOf(':');
        if (i == -1) return s;
        return s.substring(i + 1);
    }

    public static Category getCategoryFromString(String s) {
        int i = s.indexOf(':');
        if (i == -1) return Category.General;
        return Category.fromShort(s.substring(0, i));
    }

    public static String getUiString(ImgTag t) {
        return (t.getCategory() == Category.General ? "" : t.getCategory().shortName + ":") + t.getName();
    }

    @Override
    public int compareTo(@NotNull ImgTag o) {
        return name.compareTo(o.name);
    }

    @Getter
    public enum Category {
        Artist("a", new Color(169, 0, 1)),
        Character("c", new Color(0, 169, 1)),
        General("g", new Color(0, 0, 153)),
        Meta("m", new Color(253, 135, 1));

        private final String shortName;
        private final Color  color;

        Category(String shortName, Color color) {
            this.shortName = shortName;
            this.color = color;
        }

        public static Category fromShort(String s) {
            for (Category c : Category.values()) {
                if (c.shortName.equals(s)) return c;
            }
            return General;
        }

    }
}
