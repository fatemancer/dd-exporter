package in.hauu.converter;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RecordDto {
    String dateTime;
    String header;
    String text;
    String privacy;
    String music;
    String mood;
    // todo
    // List<CommentDto> comments;
}
