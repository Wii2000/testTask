import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestTask {
    private static final String FILE_NAME_TEMPLATE = "^location_\\d{4}-\\d{2}-\\d{2}\\.csv$";
    private static final String DATE_FETCH_TEMPLATE = "\\d{4}-\\d{2}-\\d{2}";

    public static void main(String[] args) throws IOException {

        List<Path> paths;

        try (Stream<Path> stream = Files.list(Paths.get(args[0]))) {
            paths = stream
                    .filter(path -> !Files.isDirectory(path) &&
                            path.getFileName().toString().matches(FILE_NAME_TEMPLATE))
                    .collect(Collectors.toList());
        }

        Map<String, Map<Integer, Duration>> resultByDay = new HashMap<>();
        Pattern pattern = Pattern.compile(DATE_FETCH_TEMPLATE);
        String day = null;

        for (Path path : paths) {

            try (Stream<String> stream = Files.lines(path)) {

                Map<Integer, Duration> zoneTimeForDay = stream.collect(Collectors.toMap(
                        line -> Integer.parseInt(line.split(";")[2]),
                        line -> {
                            String[] arr = line.split(";");
                            return Duration.between(
                                    LocalDateTime.parse(arr[0]), LocalDateTime.parse(arr[1])
                            );
                        },
                        Duration::plus
                ));

                Matcher matcher = pattern.matcher(path.getFileName().toString());
                if (matcher.find()) {
                    day = matcher.group();
                }
                resultByDay.put(day, zoneTimeForDay);

            }

        }

        resultByDay.forEach((key, value) -> {
            System.out.println(key);
            printResults(value);
        });

        Map<Integer, Duration> total = new HashMap<>();
        resultByDay.values().forEach(map ->
                map.forEach((key, value) ->
                        total.merge(key, value, Duration::plus)));
        printResults(total);
    }

    private static void printResults(Map<Integer, Duration> map) {
        map.forEach((zone, time) ->
                System.out.printf(
                        "%d - %d:%d:%d%n",
                        zone,
                        time.toHoursPart(),
                        time.toMinutesPart(),
                        time.toSecondsPart()
                ));
        System.out.println();
    }
}
