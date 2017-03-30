package com.topface.topface.experiments.onboarding.question

/**
 * Question models
 */

/**
    "minValue": 17,
    "maxValue": 103,
    "startValue": 19,
    "endValue": 41,
    "fieldNameStart": "ageStart",
    "fieldNameEnd": "ageEnd"
*/
data class QuestionType1(val minValue: Int, val maxValue:Int, val startValue: Int, val endValue: Int, val fieldNameStart: String, val fieldNameEnd: String)

/**
     "fieldName": "sex",
    "buttons": [
        {
        "title": "М",
        "value": "0"
        },
        {
        "title": "Ж",
        "value": "1"
        }
    ]
 */
data class QuestionType3(val fieldName: String, val buttons: List<QuestionSingleChoiceButton>)

data class QuestionSingleChoiceButton(val title: String, val value: String)

data class Question(val type: Int, val title: String, val type1: QuestionType1?, val type3: QuestionType3?)