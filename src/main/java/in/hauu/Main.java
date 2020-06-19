package in.hauu;

import in.hauu.connect.Connector;
import in.hauu.connect.ConnectorImpl;
import in.hauu.connect.MockConnector;
import in.hauu.diary.Diary;
import in.hauu.writer.DiaryWriter;
import in.hauu.writer.Formatter;
import in.hauu.writer.UserFileSystem;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

class Main {

    private static final Map<String, String> params = new HashMap<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        UserFileSystem.init();
        Formatter.checkIfExists();
        Connector connector = provideConnector(args);
        Diary contents  = connector.retrieveDiary(params.get("login"), params.get("password"));
        // no need to check each record... only enriching those with comments > 0
        connector.enrich(contents);
        DiaryWriter diaryWriter = new DiaryWriter();
        diaryWriter.writeDiary(contents);
    }

    @SneakyThrows
    private static Connector provideConnector(String[] args) {
        if (args.length == 3) {
            params.put("login", args[0]);
            params.put("password", args[1]);
            if (args[2].equals("true")) {
                return new ConnectorImpl();
            } else {
                return new MockConnector();
            }
        } else {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter username:");
            params.put("login", br.readLine());
            System.out.print("Enter password:");
            params.put("password", br.readLine());
            return new ConnectorImpl();
        }
    }

}
