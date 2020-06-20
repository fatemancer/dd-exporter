package in.hauu.connect;

import in.hauu.writer.UserFileSystem;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class CacheHandler {

    private static final Map<PostKey, Optional<String>> cachedPostData = new HashMap<>();
    private static final Map<PageKey, Optional<String>> cachedPageData = new HashMap<>();

    public CacheHandler(String user) {
        Path home = UserFileSystem.getHome();
        Path cacheFolder = Path.of(home.toString(), "tmp");
        File file = new File(cacheFolder.toUri());
        File[] files = file.listFiles();
        putFilesToCache(files);
    }

    public Optional<String> getPage(String user, long pageNumber) {
        var data = cachedPageData.getOrDefault(
                new PageKey(user, String.valueOf(pageNumber)),
                Optional.empty()
        );
        data.ifPresent((s) -> log.info("Returned data for page {} of user {} from cache", pageNumber, user));
        return data;
    }

    public Optional<String> getPost(String eid) {
        var data = cachedPostData.getOrDefault(new PostKey(eid), Optional.empty());
        data.ifPresent((s) -> log.info("Returned data for entry eid={}", eid));
        return data;
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
            return new PageKey(params[0], params[1]);
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
        return new PostKey(k.getName());
    }

    @AllArgsConstructor
    private static class PostKey {
        String eid;
    }

    @AllArgsConstructor
    private static class PageKey {
        String login;
        String pageNumber;
    }
}
