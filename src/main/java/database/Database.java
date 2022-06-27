package database;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.IndexOptions;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteCollection;

import java.io.File;

@Slf4j
public class Database implements AutoCloseable {

    public final MetadataRepository  metadata;
    public final PixeldataRepository pixeldata;
    public final TagRepository       tags;
    final        Nitrite             db;
    final        NitriteCollection   cMetadata;
    final        NitriteCollection   cPixeldata;
    final        NitriteCollection   cTags;

    //############################################
    //#   INIT SECTION                           #
    //############################################

    protected Database() {

        File file = new File("C:/tmp/db5");
//        file.delete();

        boolean firstTimeOpen = !file.exists();

        db = Nitrite.builder()
                    .compressed()
                    .filePath(file)
                    .openOrCreate();

        cMetadata = db.getCollection("metadata");
        cPixeldata = db.getCollection("pixeldata");
        cTags = db.getCollection("tags");

        if (firstTimeOpen) {
            cPixeldata.createIndex("imageId", IndexOptions.indexOptions(IndexType.Unique));
            cMetadata.createIndex("imageId", IndexOptions.indexOptions(IndexType.Unique));
        }

        metadata = new MetadataRepository(this);
        pixeldata = new PixeldataRepository(this);
        tags = new TagRepository(this);

        log.info("Database opened successful");
    }

    //############################################
    //#   GENERAL SECTION                        #
    //############################################

    @Override
    public void close() {
        db.close();
    }

}
