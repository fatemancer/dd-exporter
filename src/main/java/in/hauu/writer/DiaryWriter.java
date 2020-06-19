package in.hauu.writer;

import in.hauu.diary.Diary;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;

public class DiaryWriter {

    private Formatter formatter = new Formatter();

    @SneakyThrows
    public void writeDiary(Diary diary) {
        Path pathToFile = Path.of(UserFileSystem.getHome() + "/" + diary.getLogin() + ".html");
        prepareFile(pathToFile);
        Files.write(pathToFile, injectFormatting(diary.getMeta()), StandardOpenOption.CREATE);
        Files.write(pathToFile, (Iterable<String>) formatter.format(diary)::iterator, StandardOpenOption.APPEND);
        Files.write(pathToFile, formatter.getTail().getBytes(), StandardOpenOption.APPEND);
    }

    private byte[] injectFormatting(HashMap<String, String> meta) {
        String data = String.format(
                formatter.getHead(),
                meta.getOrDefault("colorBack", "#000000"),
                meta.getOrDefault("colorText", "#FFFFFF")
        );
        return data.getBytes();
    }

    @SneakyThrows
    private void prepareFile(Path pathToFile) {
        Files.deleteIfExists(pathToFile);
    }
}
