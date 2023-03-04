package com.based.basedsurvey.data;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public abstract class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String prompt;
    @ManyToOne
    private Survey survey;

}
