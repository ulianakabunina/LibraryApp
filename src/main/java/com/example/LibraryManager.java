package com.example;

import java.util.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;
import java.net.http.*;
import java.net.URI;

// Менеджер библиотеки - основной класс управления
public class LibraryManager {
    private List<Book> books;
    private HttpClient httpClient;

    public LibraryManager() {
        books = new ArrayList<>();
        httpClient = HttpClient.newHttpClient();
    }

    // Запрос к Google Books API для получения данных по ISBN
    private Map<String, String> fetchBookFromGoogleBooks(String isbn) {
        try {
            String apiKey = "AIzaSyCu6fGINP2ztpT8mqfLd4Df0oKhFrhkUss";
            String url = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn + "&key=" + apiKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Java Library App")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String body = response.body();

                if (body.contains("\"totalItems\"")) {
                    int totalItemsStart = body.indexOf("\"totalItems\"");
                    int colonIndex = body.indexOf(":", totalItemsStart);
                    int commaIndex = body.indexOf(",", colonIndex);
                    String totalItemsStr = body.substring(colonIndex + 1, commaIndex).trim();
                    int totalItems = Integer.parseInt(totalItemsStr);

                    if (totalItems > 0) {
                        String title = extractJsonValue(body, "title");
                        String author = extractJsonArrayFirstValue(body, "authors");
                        String genre = extractJsonArrayFirstValue(body, "categories");
                        String pageCountStr = extractJsonValue(body, "pageCount");

                        Map<String, String> bookData = new HashMap<>();
                        bookData.put("title", title != null ? title : "Неизвестно");
                        bookData.put("author", author != null ? author : "Неизвестен");
                        bookData.put("genre", genre != null ? genre : "Разное");
                        bookData.put("pageCount", pageCountStr != null ? pageCountStr : "0");

                        return bookData;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Ошибка подключения: " + e.getMessage());
        }
        return null;
    }

    // Парсинг JSON: извлечение значения по ключу
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex < 0) return null;

        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex < 0) return null;

        int valueStart = json.indexOf("\"", colonIndex);
        if (valueStart < 0) return null;

        int valueEnd = json.indexOf("\"", valueStart + 1);
        if (valueEnd < 0) return null;

        return json.substring(valueStart + 1, valueEnd);
    }

    // Парсинг JSON: извлечение первого элемента из массива
    private String extractJsonArrayFirstValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex < 0) return null;

        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex < 0) return null;

        int arrayStart = json.indexOf("[", colonIndex);
        if (arrayStart < 0) return null;

        int valueStart = json.indexOf("\"", arrayStart);
        if (valueStart < 0) return null;

        int valueEnd = json.indexOf("\"", valueStart + 1);
        if (valueEnd < 0) return null;

        return json.substring(valueStart + 1, valueEnd);
    }

    // Добавление книги по ISBN через API
    public void addBookByIsbn(Scanner scanner) {
        System.out.print("Введите ISBN: ");
        String isbn = scanner.nextLine();

        System.out.println("Поиск в Google Books...");
        Map<String, String> bookData = fetchBookFromGoogleBooks(isbn);

        if (bookData != null) {
            System.out.println("Книга найдена в Google Books");
            System.out.println("Название: " + bookData.get("title"));
            System.out.println("Автор: " + bookData.get("author"));
            System.out.println("Жанр: " + bookData.get("genre"));
            System.out.println("Страниц: " + bookData.get("pageCount"));

            System.out.print("Формат (бумажная/электронная/аудио): ");
            String format = scanner.nextLine();

            Book book = new Book(isbn, bookData.get("title"), bookData.get("author"),
                    bookData.get("genre"), format);
            try {
                book.setPageCount(Integer.parseInt(bookData.get("pageCount")));
            } catch (NumberFormatException e) {}

            books.add(book);
            System.out.println("Книга добавлена");
        } else {
            System.out.println("Книга не найдена в Google Books. Добавьте вручную.");
            addBookManually(scanner);
        }
    }

    // Ручное добавление книги
    public void addBookManually(Scanner scanner) {
        System.out.print("Введите ISBN (или 0 для пропуска): ");
        String isbn = scanner.nextLine();
        if (isbn.equals("0")) isbn = "";

        System.out.print("Название: ");
        String title = scanner.nextLine();
        if (title.isEmpty()) title = "Без названия";

        System.out.print("Автор: ");
        String author = scanner.nextLine();
        if (author.isEmpty()) author = "Неизвестен";

        System.out.print("Жанр: ");
        String genre = scanner.nextLine();
        if (genre.isEmpty()) genre = "Разное";

        System.out.print("Формат (бумажная/электронная/аудио): ");
        String format = scanner.nextLine();
        if (format.isEmpty()) format = "бумажная";

        Book book = new Book(isbn, title, author, genre, format);

        if (format.equals("бумажная") || format.equals("электронная")) {
            System.out.print("Количество страниц: ");
            try {
                book.setPageCount(Integer.parseInt(scanner.nextLine()));
            } catch (NumberFormatException e) {
                book.setPageCount(0);
            }
        } else if (format.equals("аудио")) {
            System.out.print("Длительность (минуты): ");
            try {
                book.setAudioLength(Integer.parseInt(scanner.nextLine()));
            } catch (NumberFormatException e) {
                book.setAudioLength(0);
            }
        }

        books.add(book);
        System.out.println("Книга добавлена");
    }

    // Поиск книг по названию, автору или жанру
    public void searchBooks(Scanner scanner) {
        System.out.print("Поиск по (название/автор/жанр): ");
        String query = scanner.nextLine().toLowerCase();

        long startTime = System.currentTimeMillis();

        List<Book> results = books.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(query) ||
                        b.getAuthor().toLowerCase().contains(query) ||
                        b.getGenre().toLowerCase().contains(query))
                .collect(Collectors.toList());

        long endTime = System.currentTimeMillis();

        System.out.println("\nНайдено " + results.size() + " книг за " + (endTime - startTime) + " мс");
        for (int i = 0; i < results.size(); i++) {
            System.out.println((i + 1) + ". " + results.get(i));
        }
    }

    // Фильтрация книг по статусу чтения
    public void filterByStatus() {
        System.out.println("\nФильтрация по статусам:");
        String[] statuses = {"Прочитано", "В процессе", "В планах"};

        for (String status : statuses) {
            List<Book> filtered = books.stream()
                    .filter(b -> b.getStatus().equals(status))
                    .collect(Collectors.toList());
            System.out.printf("%s: %d книг\n", status, filtered.size());
            filtered.forEach(b -> System.out.println("  - " + b.getTitle()));
        }
    }

    // Обновление статуса чтения книги
    public void updateStatus(Scanner scanner) {
        System.out.print("Введите название книги: ");
        String title = scanner.nextLine();

        Book book = findBookByTitle(title);
        if (book != null) {
            System.out.print("Новый статус (Прочитано/В процессе/В планах): ");
            String status = scanner.nextLine();
            book.setStatus(status);

            if (status.equals("В процессе") && book.getDateStarted() == null) {
                book.setDateStarted(LocalDate.now());
            } else if (status.equals("Прочитано") && book.getDateFinished() == null) {
                book.setDateFinished(LocalDate.now());
                System.out.print("Оценка (1-5): ");
                try {
                    book.setRating(Integer.parseInt(scanner.nextLine()));
                } catch (NumberFormatException e) {}
                System.out.print("Рецензия: ");
                book.setReview(scanner.nextLine());
            }
            System.out.println("Статус обновлен");
        } else {
            System.out.println("Книга не найдена");
        }
    }

    // Статистика и аналитика библиотеки
    public void showAnalytics() {
        if (books.isEmpty()) {
            System.out.println("Библиотека пуста");
            return;
        }

        System.out.println("\nАНАЛИТИКА");

        long totalBooks = books.size();
        long readBooks = books.stream().filter(b -> b.getStatus().equals("Прочитано")).count();

        System.out.printf("Всего книг: %d\n", totalBooks);
        System.out.printf("Прочитано: %d (%.1f%%)\n", readBooks, (readBooks * 100.0 / totalBooks));

        System.out.println("\nПо жанрам:");
        Map<String, Long> genreStats = books.stream()
                .collect(Collectors.groupingBy(Book::getGenre, Collectors.counting()));
        genreStats.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> System.out.println("  " + e.getKey() + ": " + e.getValue()));

        System.out.println("\nПо авторам:");
        Map<String, Long> authorStats = books.stream()
                .collect(Collectors.groupingBy(Book::getAuthor, Collectors.counting()));
        authorStats.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> System.out.println("  " + e.getKey() + ": " + e.getValue()));

        // Потерянные книги - в планах более 3 месяцев
        long lostBooks = books.stream()
                .filter(b -> b.getStatus().equals("В планах"))
                .filter(b -> ChronoUnit.MONTHS.between(b.getDateAdded(), LocalDate.now()) > 3)
                .count();
        if (lostBooks > 0) {
            System.out.printf("\nКниг, затерявшихся в планах (>3 мес): %d\n", lostBooks);
        }
    }

    // Поиск книги по названию
    private Book findBookByTitle(String title) {
        return books.stream()
                .filter(b -> b.getTitle().equalsIgnoreCase(title))
                .findFirst()
                .orElse(null);
    }

    // Вывод всех книг в библиотеке
    public void showAllBooks() {
        if (books.isEmpty()) {
            System.out.println("\nБиблиотека пуста");
            return;
        }

        System.out.println("\nВСЕ КНИГИ");
        for (int i = 0; i < books.size(); i++) {
            System.out.println((i + 1) + ". " + books.get(i));
        }
    }

    // Добавление демонстрационных книг для тестирования
    public void addDemoBooks() {
        Book demo1 = new Book("9780134685991", "Effective Java", "Джошуа Блох", "Программирование", "бумажная");
        demo1.setStatus("Прочитано");
        demo1.setRating(5);
        demo1.setDateFinished(LocalDate.now().minusMonths(2));
        demo1.setPageCount(416);

        Book demo2 = new Book("9780544003415", "Властелин колец", "Джон Толкин", "Фэнтези", "электронная");
        demo2.setStatus("В процессе");
        demo2.setDateStarted(LocalDate.now().minusWeeks(2));
        demo2.setPageCount(1178);

        Book demo3 = new Book("9780451524935", "Убить пересмешника", "Харпер Ли", "Классика", "бумажная");
        demo3.setStatus("В планах");

        Book demo4 = new Book("9780006546065", "1984", "Джордж Оруэлл", "Антиутопия", "аудио");
        demo4.setStatus("Прочитано");
        demo4.setRating(5);
        demo4.setAudioLength(660);
        demo4.setDateFinished(LocalDate.now().minusMonths(1));

        books.add(demo1);
        books.add(demo2);
        books.add(demo3);
        books.add(demo4);

        System.out.println("Добавлены демонстрационные книги (4 шт.)");
    }
}