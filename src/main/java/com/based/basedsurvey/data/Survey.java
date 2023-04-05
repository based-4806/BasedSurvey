package com.based.basedsurvey.data;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Survey {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @NonNull
    private String name;
    public enum SurveyStatuses {
        BEING_EDITED, BEING_FILLED, FINISHED;
    }
    @Enumerated(EnumType.STRING)
    private SurveyStatuses status;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Question> questions = new ArrayList<>();

    public int yo = 2;
    public Survey(String name){
        this.name = name;
        this.status = SurveyStatuses.BEING_EDITED;
    }
}
