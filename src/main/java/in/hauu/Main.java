package in.hauu;

import in.hauu.connect.Connector;
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

        getCredentials();

        List<String> pages = new Connector().connectTo(params.get("login"), params.get("password"));
        Diary contents = new Parser().parse(params.get("login"), pages);
        Template.writeDiary(params.get("login"), contents);
    }

    @SneakyThrows
    private static void getCredentials() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter username:");
        params.put("login", br.readLine());
        System.out.print("Enter password:");
        params.put("password", br.readLine());
    }

}
