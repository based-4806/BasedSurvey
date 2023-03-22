package com.based.basedsurvey.data;

/**
 * Useful for rendering
 */
public enum QuestionTypes {
    MULTIPLE_CHOICE,
    RANGE,
    OPEN_ENDED;

    public static Question makeQuestionFromType(QuestionTypes qt){

        switch(qt){
            case MULTIPLE_CHOICE:
                return new MultipleChoiceQuestion();
            case RANGE:
                return new RangeQuestion();
            case OPEN_ENDED:
                return new OpenAnswerQuestion();
            default:
                throw new IllegalArgumentException("cringe");

        }
    }
}
