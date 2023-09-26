package com.capstone.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Book {
    private String title;
    private double price;
    private boolean isOriginal;
    private Author author;
    private List<Review> reviews;
}
