package model;

import database.DatabaseController;
import lombok.*;
import org.dizitart.no2.NitriteId;
import utils.ImageUtils;

import java.awt.image.BufferedImage;

@AllArgsConstructor
@Getter
@Builder
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Pixeldata {

    @EqualsAndHashCode.Include
    private NitriteId id;

    private NitriteId imageId;

    private byte[] data;

    public Pixeldata(byte[] data) {
        this.data = data;
    }

    public BufferedImage getImage() {
        final var     imageById = DatabaseController.getInstance().getImageByImageId(id);
        BufferedImage img;
        if (imageById.isEmpty()) {
            img = ImageUtils.loadImage(data);
            DatabaseController.getInstance().cacheImage(imageId, img);
        } else {
            img = imageById.get();
        }
        return img;
    }

    @Override
    public String toString() {
        return "Pixeldata{id=%s, imageId=%s, data={size=%d}}".formatted(id, imageId, data == null ? 0 : data.length);
    }
}
