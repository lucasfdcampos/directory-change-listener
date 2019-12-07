package br.com.directory;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JavaDirectoryChangeListener {

    private static final String directory = "C:\\xml";

    public static void main(String[] args) {

        try (WatchService service = FileSystems.getDefault().newWatchService()) {
            Map<WatchKey, Path> keyMap = new HashMap<>();
            Path path = Paths.get(directory);
            keyMap.put(path.register(service,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY),
                    path);

            WatchKey watchKey;

            do {
                watchKey = service.take();
                Path eventDir = keyMap.get(watchKey);

                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path eventPath = (Path) event.context();

                    if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                        System.out.println(eventDir + ": " + kind + ": " + eventPath);

                        File file = eventPath.toFile();
                        System.out.println("file: " + file);

                        if (eventPath.getFileName().toString().endsWith(".zip")) {
                            File fileZip = new File(eventDir + File.separator + file);
                            System.out.println("fileZip: " + fileZip);

                            unzip(fileZip.getAbsolutePath());
                            fileZip.delete();
                        }

                        if (eventPath.getFileName().toString().endsWith(".xml")) {
                            File fileXml = new File(eventDir + File.separator + file);
                            System.out.println("Arquivo xml criado: " + fileXml);
                        }
                    }
                }

            } while (watchKey.reset());

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static void unzip(String zipFilePath) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipInputStream.getNextEntry();

        // iterates entries (xmls)
        while (entry != null) {
            String filePath = JavaDirectoryChangeListener.directory + File.separator + entry.getName();

            if (!entry.isDirectory()) {
                extractFile(zipInputStream, filePath);

            } else {
                // caso seja um diretorio
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipInputStream.closeEntry();
            entry = zipInputStream.getNextEntry();
        }
        zipInputStream.close();
    }

    /**
     * Extracts a zip entry (xml)
     * @param zipInputStream
     * @param filePath
     * @throws IOException
     */
    private static void extractFile(ZipInputStream zipInputStream, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = zipInputStream.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }
}
