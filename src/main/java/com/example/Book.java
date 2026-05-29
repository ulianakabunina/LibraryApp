package com.example;

import java.time.LocalDate;

public class Book {
    private String isbn;
    private String title;
    private String author;
    private String genre;
    private String format;
    private String status;
    private int rating;
    private String review;
    private LocalDate dateAdded;
    private LocalDate dateStarted;
    private LocalDate dateFinished;
    private int pageCount;
    private int audioLength;

    public Book(String isbn, String title, String author, String genre, String format) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.format = format;
        this.status = "В планах";
        this.rating = 0;
        this.review = "";
        this.dateAdded = LocalDate.now();
        this.pageCount = 0;
        this.audioLength = 0;
    }

    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getGenre() { return genre; }
    public String getFormat() { return format; }
    public String getStatus() { return status; }
    public int getRating() { return rating; }
    public String getReview() { return review; }
    public LocalDate getDateAdded() { return dateAdded; }
    public LocalDate getDateStarted() { return dateStarted; }
    public LocalDate getDateFinished() { return dateFinished; }
    public int getPageCount() { return pageCount; }
    public int getAudioLength() { return audioLength; }

    public void setStatus(String status) { this.status = status; }
    public void setRating(int rating) { this.rating = rating; }
    public void setReview(String review) { this.review = review; }
    public void setDateStarted(LocalDate dateStarted) { this.dateStarted = dateStarted; }
    public void setDateFinished(LocalDate dateFinished) { this.dateFinished = dateFinished; }
    public void setPageCount(int pageCount) { this.pageCount = pageCount; }
    public void setAudioLength(int audioLength) { this.audioLength = audioLength; }

    @Override
    public String toString() {
        String formatPrefix;
        if (format == null || format.isEmpty()) {
            formatPrefix = "?";
        } else if (format.equals("бумажная")) {
            formatPrefix = "бу";
        } else if (format.equals("электронная")) {
            formatPrefix = "эл";
        } else if (format.equals("аудио")) {
            formatPrefix = "ау";
        } else {
            formatPrefix = format.substring(0, Math.min(2, format.length()));
        }

        return String.format("[%s] %s - %s (%s) | Статус: %s | Рейтинг: %d",
                formatPrefix, title, author, genre, status, rating);
    }
}