package in.hauu.diary;

import in.hauu.converter.RecordDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class Record {

    private final LocalDateTime dateTime;
    private final String header;
    private final String text;
    private final String mood;
    private final String music;
    //private final String privacyLevel;

    List<Comment> comments = new ArrayList<>();

    public Record(RecordDto dto) {
        dateTime = LocalDateTime.parse(dto.getDateTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        header = dto.getHeader();
        text = dto.getText();
        music = dto.getMusic();
        mood = dto.getMood();
        //privacyLevel = dto.getPrivacy();
    }
}
