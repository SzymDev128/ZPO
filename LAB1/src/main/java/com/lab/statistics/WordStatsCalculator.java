package com.lab.statistics;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WordStatsCalculator {
    /**
     * Metoda realizująca punkt 2 zadania za pomocą strumieni.
     * Zlicza wyrazy, filtruje je i sortuje malejąco.
     */
    public static Map<String, Long> getLinkedCountedWords(Path path, int wordsLimit) {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return reader.lines() // Pobranie linii z pliku
                    // 1. Podział na słowa (split "\\s+") i spłaszczenie do jednego strumienia
                    .flatMap(line -> Arrays.stream(line.split("\\s+")))
                    // 4. Konwersja na małe litery (niewrażliwość na wielkość liter)
                    .map(String::toLowerCase)
                    // 2. Wycięcie znaków specjalnych, zostawienie tylko liter i cyfr
                    .map(word -> word.replaceAll("[^a-z0-9ąęóżśćźńłĄĘÓŻŚĆŹŃŁ]", ""))
                    // 3. Filtrowanie: słowa o długości min. 3 znaków
                    .filter(word -> word.matches("[a-z0-9ąęóżśćźńłĄĘÓŻŚĆŹŃŁ]{3,}"))
                    // 5. Grupowanie i liczenie wystąpień
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                    .entrySet().stream()
                    // 6. Sortowanie malejące po liczbie wystąpień
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    // 7. Ograniczenie do wordsLimit (np. 10 najczęstszych)
                    .limit(wordsLimit)
                    // 8. Zbiór do LinkedHashMap (zachowanie kolejności sortowania)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (k, v) -> { throw new IllegalStateException("Duplikat klucza"); },
                            LinkedHashMap::new
                    ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}