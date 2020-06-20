package in.hauu;

import in.hauu.connect.*;
import in.hauu.diary.Diary;
import in.hauu.writer.DiaryWriter;
import in.hauu.writer.Formatter;
import in.hauu.writer.UserFileSystem;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class Main {

    private static final Map<String, String> params = new HashMap<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        UserFileSystem.init();
        Formatter.checkIfExists();
        fillArgs(args);
        CacheHandler cacheHandler = new CacheHandler(params.get("login"));
        Connector connector = provideConnector(cacheHandler);
        try {
            Diary contents = connector.retrieveDiary(params.get("login"), params.get("password"));
            // no need to check each record... only enriching those with comments > 0
            connector.enrich(contents);
            DiaryWriter diaryWriter = new DiaryWriter();
            diaryWriter.writeDiary(contents);
            System.out.println(String.format(
                    "Готово. Файлик можно забрать по адресу: %s",
                    UserFileSystem.getDiary(params.get("login"))
            ));
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Нажмите Enter для выхода");
            br.readLine();
        } catch (Exception e) {
            System.err.println("Что-то пошло не так. Детали в файле ~/dd-export-err.log \n" + e);
            Files.write(UserFileSystem.getErrFile(),
                    Arrays.toString(e.getStackTrace()).getBytes(), StandardOpenOption.CREATE
            );
        } finally {
            cacheHandler.sync();
        }
    }

    @SneakyThrows
    private static void fillArgs(String[] args) {
        if (args.length == 3) {
            params.put("login", args[0]);
            params.put("password", args[1]);
            params.put("real-connect", args[2]);
        } else {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter username:");
            params.put("login", br.readLine());
            PasswordManager passwordManager = new PasswordManager();
            String password = passwordManager.readPassword("Enter password: ");
            params.put("password", password);
            params.put("real-connect", "true");
        }
    }

    @SneakyThrows
    private static Connector provideConnector(CacheHandler cacheHandler) {
        if (params.get("real-connect").equals("true")) {
            return new ConnectorImpl(cacheHandler);
        } else {
            return new LocalConnectorImpl(cacheHandler);
        }
    }

}
