package in.hauu.connect;

import in.hauu.writer.UserFileSystem;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class CacheHandler {

    private static final Map<PostKey, Optional<String>> cachedPostData = new HashMap<>();
    private static final Map<PageKey, Optional<String>> cachedPageData = new HashMap<>();
    private final Path cacheFolder;

    public CacheHandler(String user) {
        Path home = UserFileSystem.getHome();
        cacheFolder = Path.of(home.toString(), "tmp");
        File file = new File(cacheFolder.toUri());
        File[] files = file.listFiles();
        putFilesToCache(files);
    }

    public Optional<String> getPage(String user, long pageNumber) {
        var data = cachedPageData.getOrDefault(
                new PageKey(user, String.valueOf(pageNumber)),
                Optional.empty()
        );
        //data.ifPresent((s) -> log.info("Returned data for page {} of user {} from cache", pageNumber, user));
        return data;
    }

    public Optional<String> getPost(String eid) {
        var data = cachedPostData.getOrDefault(new PostKey(eid), Optional.empty());
        //data.ifPresent((s) -> log.info("Returned data for entry eid={}", eid));
        return data;
    }

    public void sync() {
        cachedPageData.entrySet().stream().filter(e -> e.getValue().isPresent()).forEach((entry) -> {
                    String file = String.format(
                            "%s/%s@@%s.page",
                            cacheFolder.toString(),
                            entry.getKey().login,
                            entry.getKey().pageNumber
                    );
                    try {
                        Files.write(Path.of(file), entry.getValue().get().getBytes(), StandardOpenOption.CREATE);
                    } catch (IOException e) {
                        log.error("Failed to write cache entry for {}", entry.getKey(), e);
                    }
                }
        );
        cachedPostData.entrySet().stream().filter(e -> e.getValue().isPresent()).forEach((entry) -> {
                    String file = String.format(
                            "%s/%s.post",
                            cacheFolder.toString(),
                            entry.getKey().eid
                    );
                    try {
                        Files.write(Path.of(file), entry.getValue().get().getBytes(), StandardOpenOption.CREATE);
                    } catch (IOException e) {
                        log.error("Failed to write cache entry for {}", entry.getKey(), e);
                    }
                }
        );
    }

    private void putFilesToCache(File[] files) {
        if (files != null) {
            log.info("Found {} files in cache folder", files.length);
            Map<PostKey, Optional<String>> postData = Arrays.stream(files)
                    .filter(f -> f.getName().endsWith(".post"))
                    .collect((Collectors.toMap(
                            this::toPostKey,
                            this::toValue
                    )));
            cachedPostData.putAll(postData);
            Map<PageKey, Optional<String>> pageData = Arrays.stream(files)
                    .filter(f -> f.getName().endsWith(".page"))
                    .collect((Collectors.toMap(
                            this::toPageKey,
                            this::toValue
                    )));
            cachedPageData.putAll(pageData);
        }
    }

    private PageKey toPageKey(File file) {
        try {
            String name = file.getName();
            String[] params = name.split("@@");
            return new PageKey(params[0], params[1].split(".page")[0]);
        } catch (Exception e) {
            return null;
        }
    }

    private Optional<String> toValue(File v) {
        try {
            List<String> strings = Files.readAllLines(v.toPath());
            return Optional.of(String.join("", strings));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private PostKey toPostKey(File k) {
        return new PostKey(k.getName().split(".post")[0]);
    }

    public void putPage(String user, int page, String data) {
        cachedPageData.put(new PageKey(user, String.valueOf(page)), Optional.of(data));
    }

    public void putPost(String eid, String data) {
        cachedPostData.put(new PostKey(eid), Optional.of(data));
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    private static class PostKey {
        String eid;
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    private static class PageKey {
        String login;
        String pageNumber;
    }
}
