package in.hauu.connect;

import in.hauu.diary.Diary;

import java.io.IOException;

public interface Connector {
    Diary retrieveDiary(String user, String password) throws IOException, InterruptedException;

    void enrich(Diary diary);
}
