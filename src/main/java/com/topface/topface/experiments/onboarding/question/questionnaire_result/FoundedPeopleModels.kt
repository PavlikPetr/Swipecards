package com.topface.topface.experiments.onboarding.question.questionnaire_result

import com.topface.topface.data.FeedUser

data class QuestionnaireResult(var userList: MutableList<FeedUser>, val productId: String, val buyMessage: String, val foundTitle: String)
