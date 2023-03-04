package com.based.basedsurvey.data;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class MultiplechoiceQuestion extends Question{
    @ElementCollection
    private List<String> options = new ArrayList<>();

    @ElementCollection
    private List<Integer> responses = new ArrayList<>();
}
