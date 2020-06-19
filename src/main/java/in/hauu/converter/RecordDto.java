package in.hauu.converter;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RecordDto {
    String eid;
    String dateTime;
    String header;
    String text;
    String privacy;
    String music;
    String mood;
    String commentsNumber;
    String tags;
}
