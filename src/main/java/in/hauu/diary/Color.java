package in.hauu.diary;

import java.util.Map;

class Color {

    private static final Map<String, String> colors = Map.of(
            "black", "000000",
            "white", "ffffff"
    );

    String hex;

    public Color(String input) {
        colors.getOrDefault(input, "000000");
    }
}
