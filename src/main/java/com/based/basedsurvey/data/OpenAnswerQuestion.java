package com.based.basedsurvey.data;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class OpenAnswerQuestion extends Question{
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> responses = new ArrayList<>();

}
