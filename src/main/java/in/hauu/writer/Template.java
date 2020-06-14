package in.hauu.writer;

import in.hauu.diary.Diary;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.stream.Collectors;

public class Template {

    private static String TEMPLATE_HEAD;
    private static String TEMPLATE_ARTICLE;

    static {
        try {
            TEMPLATE_ARTICLE = new String(Files.readAllBytes(Path.of("src/main/resources/template.html")));
            TEMPLATE_HEAD = new String(Files.readAllBytes(Path.of("src/main/resources/head.html")));
        } catch (IOException e) {
            throw new RuntimeException("No template files loaded", e);
        }
    }

    @SneakyThrows
    public static void writeDiary(String login, Diary diary) {
        // some buffering ? mine will weight 200k * 100 ~ 20 Mb
        String contents;

        // ugly af, but who cares with current load
        contents = String.format(TEMPLATE_HEAD, diary.getFlatRecords().map(r ->
                MessageFormat.format(
                        TEMPLATE_ARTICLE,
                        r.getDateTime(),
                        r.getHeader(),
                        r.getText(),
                        r.getMood(),
                        r.getMusic()
                )).collect(Collectors.joining()));

        Files.write(Path.of("src/main/resources/private" + login + ".html"), contents.getBytes(), StandardOpenOption.CREATE);
    }

    public static void checkIfExists() {
        // static must fire and successfully load resources
    }
}
