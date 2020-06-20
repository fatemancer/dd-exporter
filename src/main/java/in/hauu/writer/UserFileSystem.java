package in.hauu.writer;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Path;
import java.util.stream.Collectors;

@Slf4j
public class UserFileSystem {

    public static void init() {
        log.info("User folder: {}", getHome());
    }

    public static Path getHome() {
        String home = System.getProperty("user.home");
        Path dd = Path.of(home, "dd-export");
        File file = new File(dd.toString());
        if (!file.exists()) {
            file.mkdir();
        }
        initCacheFolder(dd);
        return dd;
    }

    public static Path getErrFile() {
        String home = System.getProperty("user.home");
        return Path.of(home, "dd-export-error.log");

    }

    private static void initCacheFolder(Path dd) {
        Path tmp = Path.of(dd.toString(), "tmp");
        if (!new File(tmp.toString()).exists()) {
            new File(tmp.toString()).mkdir();
        }
    }

    public static String getResourceAsString(String filename) {
        try (InputStream resourceAsStream = UserFileSystem.class.getResourceAsStream("/main/resources/" + filename)) {
            // small files, no need to buffer
            return new BufferedReader(new InputStreamReader(resourceAsStream)).lines().collect(Collectors.joining());
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to read resource %s", filename));
        }
    }

    public static String getDiary(String login) {
        String home = System.getProperty("user.home");
        Path dd = Path.of(home, "dd-export");
        try {
            return Path.of(dd.toString(), login + ".html").toRealPath().toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
