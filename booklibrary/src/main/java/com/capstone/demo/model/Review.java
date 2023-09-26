package com.capstone.demo.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Review {
    private int rating;
    private String comment;
}
