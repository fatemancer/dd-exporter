package in.hauu.writer;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;

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

    private static void initCacheFolder(Path dd) {
        Path tmp = Path.of(dd.toString(), "tmp");
        if (!new File(tmp.toString()).exists()) {
            new File(tmp.toString()).mkdir();
        }
    }
}
