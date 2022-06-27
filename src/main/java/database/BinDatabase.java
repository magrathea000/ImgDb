package database;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.awt.*;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class BinDatabase implements AutoCloseable {

    private final File               dir;
    private final String             fname;
    private final PBEParameterSpec   pbeParamSpec;
    private       SecretKey          key;
    private       Map<Long, DbEntry> entries;
    private       Map<Long, DbTag>   tags;

    private AtomicLong currentId = new AtomicLong(System.currentTimeMillis());

    public BinDatabase(final File dir, final String fname, final String password) {
        this.dir = dir;
        this.fname = fname;

        File fStore = new File(dir, fname + ".store.bin");
        File fDb    = new File(dir, fname + ".db.bin");

        if (fStore.exists() && fDb.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fStore))) {
                //noinspection unchecked
                entries = (Map<Long, DbEntry>) ois.readObject();
                //noinspection unchecked
                tags = (Map<Long, DbTag>) ois.readObject();
                Objects.requireNonNull(entries);
                Objects.requireNonNull(tags);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("unable to open database", e);
                System.exit(1);
            }
        } else {
            //noinspection ResultOfMethodCallIgnored
            fStore.delete();
            //noinspection ResultOfMethodCallIgnored
            fDb.delete();
            entries = new HashMap<>();
            tags = new HashMap<>();
        }

        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            key = keyFactory.generateSecret(keySpec);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            log.error("unable to open database", e);
            System.exit(1);
        }

        byte[] salt = {(byte) 0xa3, (byte) 0x76, (byte) 0x97, (byte) 0xc3, (byte) 0x5b, (byte) 0xde, (byte) 0xff, (byte) 0xb7};
        pbeParamSpec = new PBEParameterSpec(salt, 42);
    }

    @Override
    public void close() throws Exception {
        log.info("closing db");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(dir, fname + ".db.bin")))) {
            oos.writeObject(entries);
            oos.writeObject(tags);
        }
    }

    public Optional<byte[]> readDbImage(DbEntry entry) {
        try (FileInputStream fis = new FileInputStream(new File(dir, fname + ".store.bin"))) {
            byte[] bytes = new byte[(int) entry._dbLength];
            fis.skip(entry._dbOffset);
            fis.read(bytes);
            return Optional.of(decryptData(bytes));
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            log.info("unable to load image. {}", e.getMessage());
        }
        return Optional.empty();
    }

    public List<byte[]> readDbImages(Collection<DbEntry> entries) {
        try (FileInputStream fis = new FileInputStream(new File(dir, fname + ".store.bin"))) {
            final AtomicLong offset = new AtomicLong();
            return entries.stream().sorted(Comparator.comparingLong(e -> e._dbOffset)).map(e -> {
                byte[] bytes = new byte[(int) e._dbLength];
                try {
                    fis.skip(e._dbOffset - offset.get());
                    fis.read(bytes);
                    offset.set(e._dbOffset + e._dbLength);
                    return decryptData(bytes);
                } catch (IOException | GeneralSecurityException ex) {
                    ex.printStackTrace();
                }
                return null;
            }).filter(Objects::nonNull).toList();
        } catch (IOException e) {
            e.printStackTrace();
            log.info("unable to load image. {}", e.getMessage());
        }
        return new ArrayList<>(0);
    }

    private Cipher makeCipher(Boolean decryptMode) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
        if (decryptMode) {
            cipher.init(Cipher.DECRYPT_MODE, key, pbeParamSpec);
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, key, pbeParamSpec);
        }
        return cipher;
    }

    private byte[] encryptData(byte[] decData) throws GeneralSecurityException {
        return makeCipher(false).doFinal(decData);
    }

    private byte[] decryptData(byte[] encData) throws GeneralSecurityException {
        return makeCipher(true).doFinal(encData);
    }

    public List<DbEntry> getEntriesByTags(List<String> stags) {
        List<DbTag> byNames = getTagsByNames(stags);
        return entries.values().stream()
                      .filter(e -> e.tags.stream().map(this.tags::get).toList().containsAll(byNames))
                      .toList();
    }

    public List<DbEntry> getRecentEntries() {
        return entries.values().stream().sorted((e1, e2) -> Long.compare(e2.modified, e1.modified)).toList();
    }

    public Optional<DbEntry> getEntryById(long id) {
        return Optional.ofNullable(entries.get(id));
    }

    public List<DbEntry> getEntriesByIds(List<Long> ids) {
        return ids.stream().map(entries::get).filter(Objects::nonNull).toList();
    }

    public List<DbEntry> getEntriesWithTags(List<String> stags) {
        HashSet<String> setTags = new HashSet<>(stags);
        return entries.values()
                      .stream()
                      .filter(e -> e.tags.stream().map(this.tags::get).anyMatch(t -> setTags.contains(t.name)))
                      .toList();
    }

    public List<DbImage> getImagesByIds(List<Long> ids) {
        return ids.stream()
                  .map(entries::get)
                  .filter(Objects::nonNull)
                  .map(e -> readDbImage(e).map(bytes -> new DbImage(e.id, bytes)))
                  .filter(Optional::isPresent)
                  .map(Optional::get)
                  .toList();
    }

    public Optional<DbImage> getImageById(long id) {
        DbEntry e = entries.get(id);
        if (e == null) return Optional.empty();
        Optional<byte[]> img = readDbImage(e);
        return img.map(bytes -> new DbImage(e.id, bytes));
    }

    public List<DbTag> getTagsByNames(List<String> names) {
        HashSet<String> setNames = new HashSet<>(names);
        return tags.values().stream().filter(t -> setNames.contains(t.name)).toList();
    }

    public List<DbTag> getAllTags() {
        return new ArrayList<>(tags.values());
    }

    public void deleteTag(DbTag tagToDelete) {
        tags.remove(tagToDelete.id);
        entries.values().forEach(e -> {
            e.tags.remove(tagToDelete.id);
        });
    }

    public void updateTag(DbTag tagToUpdate) {
        tags.put(tagToUpdate.id, tagToUpdate);
    }

    public List<DbTag> storeTags(List<String> tags) {
        return tags.stream()
                   .map(t -> {
                       String   name = DbTag.getNameFromString(t);
                       Category cat  = DbTag.getCategoryFromString(t);
                       Optional<DbTag> oldtag = this.tags.values()
                                                         .stream()
                                                         .filter(tag -> tag.getName().equals(name))
                                                         .findAny()
                                                         .map(tag -> {
                                                             tag.uses = tag.uses + 1;
                                                             return tag;
                                                         });
                       return oldtag.orElse(new DbTag(currentId.incrementAndGet(), name, cat, 1));
                   })
                   .peek(this::updateTag)
                   .toList();
    }

    public boolean store(DbEntry e, DbImage i, List<String> stags) {
        try {
            File db = new File(dir, fname + ".store.bin");

            byte[]           enc = encryptData(i.getData());
            FileOutputStream fos = new FileOutputStream(new File(dir, fname + ".store.bin"), true);
            fos.write(enc);

            List<Long> newTags = storeTags(stags).stream().map(DbTag::getId).toList();

            DbEntry dbEntry = new DbEntry(
                    e.id,
                    db.length(),
                    enc.length,
                    e.width,
                    e.height,
                    e.rating,
                    System.currentTimeMillis(),
                    newTags
            );

            entries.put(dbEntry.id, dbEntry);
            return true;
        } catch (GeneralSecurityException | IOException ex) {
            ex.printStackTrace();
        }
        return false;
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

    @AllArgsConstructor
    @Getter
    private static class DbEntry implements Serializable {
        @Getter(AccessLevel.NONE)
        protected final long       _dbOffset;
        @Getter(AccessLevel.NONE)
        protected final long       _dbLength;
        private final   long       id;
        private final   int        width;
        private final   int        height;
        private final   int        rating;
        private final   long       modified;
        private final   List<Long> tags;
    }

    @AllArgsConstructor
    @Getter
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    private static class DbTag implements Serializable {
        @EqualsAndHashCode.Include
        private final long     id;
        @EqualsAndHashCode.Include
        private final String   name;
        private final Category category;
        private       int      uses;

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
    }

    @AllArgsConstructor
    @Getter
    private static class DbImage implements Serializable {
        private final long   id;
        private final byte[] data;
    }

}
