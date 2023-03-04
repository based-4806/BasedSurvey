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
public class OpenAnswerQuestion extends Question{
    @ElementCollection
    private List<String> responses = new ArrayList<>();

}
