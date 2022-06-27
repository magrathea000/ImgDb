package init;

import database.DatabaseController;
import gui.MainFrameController;
import model.Metadata;
import model.Pixeldata;
import org.apache.commons.io.FilenameUtils;
import utils.ImageUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Init {


    private static final byte[] salt = {(byte) 0x43, (byte) 0x76, (byte) 0x95, (byte) 0xc7, (byte) 0x5b, (byte) 0xd7, (byte) 0x45, (byte) 0x17};

    public static void main(String[] args) throws IOException, GeneralSecurityException {
//        bin();
//        binread();
        //        batchimport(new File("C:\\Users\\scesher\\.lint\\Neuer Ordner"));
        DatabaseController.getInstance();
        MainFrameController.get().show();
    }

//    public static void binread() throws NoSuchAlgorithmException, InvalidKeySpecException {
//
//        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("C:/tmp/db52.db.bin"))) {
//
//            Map<byte[], dbentry> dbentries = (Map<byte[], dbentry>) ois.readObject();
//
//            FileInputStream  fis = new FileInputStream("C:/tmp/db52.store.bin");
//            final AtomicLong idx = new AtomicLong();
//            dbentries.keySet().stream()
//                     .filter(e -> Math.random() < .33)
//                     .limit(10).map(dbentries::get)
//                     .sorted(Comparator.comparingLong(e -> e.offset))
//                     .forEach(e -> {
//                         try {
//                             long off = e.offset - idx.get();
//                             fis.skipNBytes(off);
//                             ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
//                             byte[] buffer      = new byte[(1 << 17)];
//                             long   len         = 0;
//                             long   bytesNeeded = e.getLength() - len;
//                             while (bytesNeeded > 0) {
//                                 int byteToRead = (int) Math.min(bytesNeeded, buffer.length);
//                                 int read       = fis.read(buffer, 0, byteToRead);
//                                 bytesNeeded -= read;
//                                 if (read > 0) baos.write(buffer, 0, read);
//                             }
//                             idx.addAndGet(e.length);
//
//                             byte[] dec = decryptData(baos.toByteArray(), "test1234");
//
//                             BufferedImage read = ImageIO.read(new ByteArrayInputStream(dec));
//                             ImageIO.write(read, "qoi", new File("C:/tmp/bin/" + new BigInteger(e.id).toString(16) + ".qoi"));
//
//                         } catch (IOException | GeneralSecurityException ex) {
//                             ex.printStackTrace();
//                         }
//                     });
//
//            fis.close();
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//
//
//    }
//
//    public static void bin() throws NoSuchAlgorithmException {
//
//        File            dir   = new File("C:\\Users\\scesher\\.lint\\Neuer Ordner");
//        ArrayList<File> ferrs = new ArrayList<>();
//
//        long start = System.currentTimeMillis();
//
//        LinkedList<Long> times = new LinkedList<>();
//
//        AtomicInteger count = new AtomicInteger(0);
//        try (Stream<Path> walkStream = Files.walk(dir.toPath())) {
//            List<Path> files = walkStream.filter(p -> p.toFile().isFile()).filter(f -> {
//                String ext = FilenameUtils.getExtension(f.getFileName().toString());
//                return ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg") || ext.equals("jfif");
//            }).sorted().toList();
//            AtomicInteger i = new AtomicInteger();
//
//            HashMap<byte[], dbentry> dbentries = new HashMap<>();
//
//            FileOutputStream fos = new FileOutputStream("C:/tmp/db52.store.bin");
//
//            dbentry last = new dbentry(new byte[0], 0, 0);
//
//            for (Path p : files) {
//                long          istart = System.currentTimeMillis();
//                int           idx    = i.getAndIncrement();
//                File          f      = p.toFile();
//                BufferedImage img    = ImageUtils.loadImage(f);
//
//                if (img == null) {
//                    ferrs.add(f);
//                    continue;
//                }
//
//                try {
//                    MessageDigest md5 = MessageDigest.getInstance("MD5");
//                    md5.update(Double.toString(Math.random()).getBytes());
//                    byte[] digest = md5.digest();
//                    String hash   = new BigInteger(digest).toString(16);
//
//                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                    ImageUtils.writeImage(img, "qoi", baos);
//
//                    byte[] enc = encryptData(baos.toByteArray(), "test1234");
//
//                    dbentry next = new dbentry(digest, last.offset + last.length, enc.length);
//
//                    fos.write(enc);
//
//                    dbentries.put(digest, next);
//
//                    last = next;
//
//                } catch (GeneralSecurityException | IOException e) {
//                    e.printStackTrace();
//                }
//
//
//                long iend = System.currentTimeMillis();
//
//                if (times.size() >= 10) {
//                    times.pop();
//                }
//                times.add(iend - istart);
//
//                double average = times.stream().mapToLong(l -> l).average().getAsDouble();
//
//                System.out.format("%3d%% - %6d/%6d done. ETA %5ds\r", (int) Math.round(idx / (double) files.size() * 100), idx, files.size(), (int) ((files.size() - idx) * average / 1000));
//            }
//
//            fos.close();
//
//            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("C:/tmp/db52.db.bin"))) {
//                oos.writeObject(dbentries);
//            }
//
//            long end = System.currentTimeMillis();
//            System.out.println("\nImported " + count.get() + "/" + files.size());
//            System.out.println("Took " + ((end - start) / 1000) + "s");
//            System.out.println("Files with errors:\n" + ferrs.stream().map(File::getAbsolutePath).collect(Collectors.joining("\n")));
//            System.out.println("Done!");
//
//        } catch (IOException e) {
//            e.printStackTrace();
//
//        }
//
//    }

    public static void cvt() throws IOException {
        File dir = new File("C:\\Users\\scesher\\.lint\\Neuer Ordner");
        try (Stream<Path> walkStream = Files.walk(dir.toPath())) {
            List<Path> files = walkStream.filter(p -> p.toFile().isFile()).filter(f -> {
                String ext = FilenameUtils.getExtension(f.getFileName().toString());
                return ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg") || ext.equals("jfif");
            }).toList();

            File outdir = new File("c:/tmp/qoi");
            files.forEach(p -> {

                try {
                    File          f    = p.toFile();
                    BufferedImage read = ImageIO.read(f);
                    ImageIO.write(read, "qoi", new File(outdir, FilenameUtils.removeExtension(f.getName()) + ".qoi"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void batchimport(File dir) {
        DatabaseController dbc = DatabaseController.getInstance();

        ArrayList<File> ferrs = new ArrayList<>();

        long start = System.currentTimeMillis();

        LinkedList<Long> times = new LinkedList<>();

        AtomicInteger count = new AtomicInteger(0);
        try (Stream<Path> walkStream = Files.walk(dir.toPath())) {
            List<Path> files = walkStream.filter(p -> p.toFile().isFile()).filter(f -> {
                String ext = FilenameUtils.getExtension(f.getFileName().toString());
                return ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg") || ext.equals("jfif");
            }).toList();
            AtomicInteger i = new AtomicInteger();
            files.forEach(p -> {
                long          istart = System.currentTimeMillis();
                int           idx    = i.getAndIncrement();
                File          f      = p.toFile();
                BufferedImage img    = ImageUtils.loadImage(f);

                if (img == null) {
                    ferrs.add(f);
                    return;
                }

                Set<String> tags = Set.of(f.getParentFile().getName().trim().toLowerCase().replaceAll("\\s", "_"));

                Metadata metadata = new Metadata(img.getWidth(), img.getHeight(), (int) (Math.random() * 5 + 1), tags);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageUtils.writeImage(img, "qoi", baos);

                Pixeldata pixeldata = new Pixeldata(baos.toByteArray());

                boolean store = dbc.store(metadata, pixeldata);
                if (store) count.incrementAndGet();
                long iend = System.currentTimeMillis();

                if (times.size() >= 10) {
                    times.pop();
                }
                times.add(iend - istart);

                double average = times.stream().mapToLong(l -> l).average().getAsDouble();

                System.out.format("%3d%% - %6d/%6d done. ETA %5ds\r", (int) Math.round(idx / (double) files.size() * 100), idx, files.size(), (int) ((files.size() - idx) * average / 1000));
            });
            long end = System.currentTimeMillis();
            System.out.println("\nImported " + count.get() + "/" + files.size());
            System.out.println("Took " + ((end - start) / 1000) + "s");
            System.out.println("Files with errors:\n" + ferrs.stream().map(File::getAbsolutePath).collect(Collectors.joining("\n")));
            System.out.println("Done!");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static Cipher makeCipher(String pass, Boolean decryptMode) throws GeneralSecurityException {

        //Use a KeyFactory to derive the corresponding key from the passphrase:
        PBEKeySpec       keySpec    = new PBEKeySpec(pass.toCharArray());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey        key        = keyFactory.generateSecret(keySpec);

        //Create parameters from the salt and an arbitrary number of iterations:
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 42);

        //Set up the cipher:
        Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");

        //Set the cipher mode to decryption or encryption:
        if (decryptMode) {
            cipher.init(Cipher.DECRYPT_MODE, key, pbeParamSpec);
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, key, pbeParamSpec);
        }

        return cipher;
    }


    /**
     * Encrypts one file to a second file using a key derived from a passphrase:
     *
     * @return
     **/
    public static byte[] encryptData(byte[] decData, String pass) throws IOException, GeneralSecurityException {
        byte[] encData;

        //Generate the cipher using pass:
        Cipher cipher = makeCipher(pass, false);

        return cipher.doFinal(decData);
    }


    /**
     * Decrypts one file to a second file using a key derived from a passphrase:
     *
     * @return
     **/
    public static byte[] decryptData(byte[] encData, String pass) throws GeneralSecurityException, IOException {
        byte[] decData;

        //Generate the cipher using pass:
        Cipher cipher = makeCipher(pass, true);

        //Decrypt the file data:
        return cipher.doFinal(encData);
    }

}
