import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StorageManager {

    public static long calculateFolderSize(File folder) {
        if (folder == null || !folder.exists()) {
            return 0L;
        }

        try (Stream<Path> stream = Files.walk(folder.toPath())) {
            return stream
                    .filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        try {
                            return Files.size(p);
                        } catch (IOException ex) {
                            return 0L;
                        }
                    })
                    .sum();
        } catch (IOException e) {
            return 0L;
        }
    }

    public static List<File> listLargestFiles(File folder, int maxFiles) {
        List<File> files = new ArrayList<>();
        if (folder == null || !folder.exists()) {
            return files;
        }

        try (Stream<Path> stream = Files.walk(folder.toPath())) {
            files = stream
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .sorted(Comparator.comparingLong(File::length).reversed())
                    .limit(maxFiles)
                    .collect(Collectors.toList());
        } catch (IOException ignored) {
        }

        return files;
    }

    public static int organizeFilesByType(File sourceFolder, File destinationRoot) {
        if (sourceFolder == null || destinationRoot == null || !sourceFolder.exists()) {
            return 0;
        }

        if (!destinationRoot.exists()) {
            destinationRoot.mkdirs();
        }

        int movedCount = 0;
        try (Stream<Path> stream = Files.walk(sourceFolder.toPath())) {
            List<Path> paths = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> !path.startsWith(destinationRoot.toPath()))
                    .collect(Collectors.toList());

            for (Path path : paths) {
                File file = path.toFile();
                String category = categorizeFile(file.getName());
                File targetDir = new File(destinationRoot, category);
                if (!targetDir.exists()) {
                    targetDir.mkdirs();
                }

                Path targetPath = targetDir.toPath().resolve(file.getName());
                try {
                    Files.move(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    movedCount++;
                } catch (IOException ignored) {
                }
            }
        } catch (IOException ignored) {
        }

        return movedCount;
    }

    public static int syncFolders(File sourceFolder, File destinationFolder) {
        if (sourceFolder == null || destinationFolder == null || !sourceFolder.exists()) {
            return 0;
        }

        if (!destinationFolder.exists()) {
            destinationFolder.mkdirs();
        }

        int copiedCount = 0;
        try (Stream<Path> stream = Files.walk(sourceFolder.toPath())) {
            List<Path> paths = stream
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());

            for (Path sourcePath : paths) {
                File sourceFile = sourcePath.toFile();
                String relativePath = sourceFolder.toPath().relativize(sourcePath).toString();
                File destinationFile = new File(destinationFolder, relativePath);
                File destinationParent = destinationFile.getParentFile();
                if (destinationParent != null && !destinationParent.exists()) {
                    destinationParent.mkdirs();
                }
                try {
                    Files.copy(sourcePath, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    copiedCount++;
                } catch (IOException ignored) {
                }
            }
        } catch (IOException ignored) {
        }

        return copiedCount;
    }

    private static String categorizeFile(String filename) {
        String extension = "Other";
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            extension = filename.substring(lastDot + 1).toUpperCase();
        }

        switch (extension) {
            case "JPG":
            case "PNG":
            case "GIF":
            case "BMP":
            case "JPEG":
                return "Images";
            case "MP4":
            case "AVI":
            case "MKV":
            case "MOV":
                return "Videos";
            case "MP3":
            case "WAV":
            case "FLAC":
                return "Audio";
            case "PDF":
            case "DOC":
            case "DOCX":
            case "XLS":
            case "XLSX":
            case "PPT":
            case "PPTX":
            case "TXT":
            case "CSV":
                return "Documents";
            case "ZIP":
            case "RAR":
            case "7Z":
            case "TAR":
                return "Archives";
            case "EXE":
            case "MSI":
                return "Executables";
            case "APK":
                return "Apps";
            default:
                return "Other";
        }
    }
}
