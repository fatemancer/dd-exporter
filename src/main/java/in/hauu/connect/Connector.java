package in.hauu.connect;

import in.hauu.diary.Diary;

import java.io.IOException;
import java.util.List;

public interface Connector {
    List<String> connectTo(String user, String password) throws IOException, InterruptedException;

    void enrich(Diary diary);
}
