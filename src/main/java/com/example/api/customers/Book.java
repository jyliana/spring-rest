package com.example.api.customers;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(namespace = "noNamespaceSchemaLocation", localName = "Book")
@JsonPropertyOrder({"genre", "author", "title", "year", "series", "price"})
public class Book {
    private String genre;
    private String author;
    private String title;
    private int year;
    private String series;
    private Double price;

    public Book() {

    }

    public Book(String genre, String author, String title, int year, String series, Double price) {
        this.genre = genre;
        this.author = author;
        this.title = title;
        this.year = year;
        this.series = series;
        this.price = price;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
