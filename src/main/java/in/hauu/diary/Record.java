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

    private final int eid;
    private final LocalDateTime dateTime;
    private final String header;
    private final String text;
    private final String mood;
    private final String music;
    private Enrichment enrichment = Enrichment.UNKNOWN;
    private final String tags;

    List<Comment> comments = new ArrayList<>();

    public Record(RecordDto dto) {
        eid = Integer.parseInt(dto.getEid());
        dateTime = LocalDateTime.parse(dto.getDateTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        header = dto.getHeader();
        text = dto.getText();
        music = dto.getMusic();
        mood = dto.getMood();
        tags = dto.getTags();
        int commentsNumber = Integer.parseInt(dto.getCommentsNumber());
        if (commentsNumber == 0) {
            enrichment = Enrichment.NOT_NEEDED;
        } else  {
            enrichment = Enrichment.NEEDED;
        }
    }

    public Record setEnrichment(Enrichment enrichment) {
        this.enrichment = enrichment;
        return this;
    }

    public enum Enrichment {
        UNKNOWN,
        NOT_NEEDED,
        NEEDED,
    }

}
