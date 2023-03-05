package com.based.basedsurvey.data;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class MultiplechoiceQuestion extends Question{
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> options = new ArrayList<>();

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ElementCollection(fetch = FetchType.EAGER)
    private List<Integer> responses = new ArrayList<>();
}
