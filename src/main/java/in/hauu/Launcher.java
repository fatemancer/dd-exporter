package in.hauu;

import in.hauu.connect.*;
import in.hauu.diary.Diary;
import in.hauu.gui.GuiManager;
import in.hauu.writer.DiaryWriter;
import in.hauu.writer.Formatter;
import in.hauu.writer.UserFileSystem;
import lombok.SneakyThrows;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Launcher {

    private static final Map<String, String> params = new HashMap<>();

    public static void core(String[] args) {
        class rTask implements Runnable {
            @Override
            public void run() {
                UserFileSystem.init();
                Formatter.checkIfExists();
                fillArgs(args);
                CacheHandler cacheHandler = new CacheHandler(params.get("login"));
                Connector connector = provideConnector(cacheHandler);
                runSync(connector, cacheHandler);
            }
        };
        Thread t = new Thread(new rTask());
        t.start();
    }

    private static void runSync(Connector connector, CacheHandler cacheHandler) {
        try {
            Diary contents = connector.retrieveDiary(params.get("login"), params.get("password"));
            // no need to check each record... only enriching those with comments > 0
            connector.enrich(contents);
            DiaryWriter diaryWriter = new DiaryWriter();
            diaryWriter.writeDiary(contents);
            String results = String.format(
                    "Готово. Файлик можно забрать по адресу: %s \n Ссылка скопирована в буфер обмена",
                    UserFileSystem.getDiary(params.get("login"))
            );
            System.out.println(results);
            copyToClipboard();
            GuiManager.popUp(results);
        } catch (Exception e) {
            System.err.println("Что-то пошло не так: " + Arrays.toString(e.getStackTrace()));
        } finally {
            cacheHandler.sync();
            // not elegant, but works for now
            System.exit(0);
        }
    }

    private static void copyToClipboard() {
        StringSelection stringSelection = new StringSelection(UserFileSystem.getDiary(params.get("login")));
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    @SneakyThrows
    private static void fillArgs(String[] args) {
        if (args.length == 3) {
            // this also fires for GUI launch
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
