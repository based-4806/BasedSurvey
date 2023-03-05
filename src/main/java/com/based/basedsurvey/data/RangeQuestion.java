package com.based.basedsurvey.data;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
public class RangeQuestion extends Question{
    private float low, high; //range inclusive
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ElementCollection(fetch = FetchType.EAGER)
    private List<Float> responses = new ArrayList<>();

    public RangeQuestion(String prompt, float low, float high){
        this.setPrompt(prompt);
        this.low = low;
        this.high = high;
    }

    /**
     * @param rsp the candidate response
     * @return true if the candidate response is in range
     */
    public boolean isInRange(float rsp){
        return low <= rsp && rsp <= high;
    }
}
