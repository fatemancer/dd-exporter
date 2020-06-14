package in.hauu;

import in.hauu.connect.Connector;
import in.hauu.connect.ConnectorImpl;
import in.hauu.connect.MockConnector;
import in.hauu.connect.Parser;
import in.hauu.diary.Diary;
import in.hauu.writer.Template;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Main {

    private static final Map<String, String> params = new HashMap<>();

    public static void main(String[] args) throws IOException, InterruptedException {

        Template.checkIfExists();
        Connector connector = provideConnector(args);
        List<String> pages = connector.connectTo(params.get("login"), params.get("password"));
        Diary contents = new Parser().parse(params.get("login"), pages);
        // no need to check each record... only enriching those with comments > 0
        connector.enrich(contents);
        Template.writeDiary(params.get("login"), contents);
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
