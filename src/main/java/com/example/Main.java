package com.example;

import java.util.Scanner;

// Главный класс приложения
public class Main {
    private static LibraryManager library;
    private static Scanner scanner;

    public static void main(String[] args) {
        library = new LibraryManager();
        scanner = new Scanner(System.in);

        System.out.println("СИСТЕМА УПРАВЛЕНИЯ БИБЛИОТЕКОЙ");
        System.out.println("С подключением к Google Books API\n");

        library.addDemoBooks();

        while (true) {
            showMenu();
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Введите число от 1 до 7");
                continue;
            }

            switch (choice) {
                case 1:
                    addBookMenu();
                    break;
                case 2:
                    library.searchBooks(scanner);
                    break;
                case 3:
                    library.filterByStatus();
                    break;
                case 4:
                    library.updateStatus(scanner);
                    break;
                case 5:
                    library.showAnalytics();
                    break;
                case 6:
                    library.showAllBooks();
                    break;
                case 7:
                    System.out.println("До свидания!");
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("Неверный выбор");
            }
        }
    }

    // Вывод главного меню
    private static void showMenu() {
        System.out.println("\nГЛАВНОЕ МЕНЮ");
        System.out.println("1. Добавить книгу");
        System.out.println("2. Поиск книги");
        System.out.println("3. Фильтр по статусам");
        System.out.println("4. Обновить статус чтения");
        System.out.println("5. Аналитика");
        System.out.println("6. Все книги");
        System.out.println("7. Выход");
        System.out.print("Ваш выбор: ");
    }

    // Подменю добавления книги
    private static void addBookMenu() {
        System.out.println("\nДОБАВЛЕНИЕ КНИГИ");
        System.out.println("1. По ISBN (Google Books API)");
        System.out.println("2. Вручную");
        System.out.print("Выбор: ");
        String choice = scanner.nextLine();

        if (choice.equals("1")) {
            library.addBookByIsbn(scanner);
        } else {
            library.addBookManually(scanner);
        }
    }
}