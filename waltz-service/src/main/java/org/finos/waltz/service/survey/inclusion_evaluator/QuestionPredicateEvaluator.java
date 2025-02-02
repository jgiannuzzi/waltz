/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.service.survey.inclusion_evaluator;

import org.finos.waltz.service.DIConfiguration;
import org.apache.commons.jexl3.*;
import org.finos.waltz.data.survey.SurveyInstanceDao;
import org.finos.waltz.data.survey.SurveyQuestionDao;
import org.finos.waltz.data.survey.SurveyQuestionResponseDao;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.survey.SurveyInstance;
import org.finos.waltz.model.survey.SurveyInstanceQuestionResponse;
import org.finos.waltz.model.survey.SurveyQuestion;
import org.finos.waltz.model.survey.SurveyQuestionResponse;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.common.MapUtilities.newHashMap;
import static org.finos.waltz.common.StringUtilities.isEmpty;

@Service
public class QuestionPredicateEvaluator {

    private final DSLContext dsl;
    private final SurveyQuestionDao questionDao;
    private final SurveyInstanceDao instanceDao;
    private final SurveyQuestionResponseDao responseDao;


    @Autowired
    public QuestionPredicateEvaluator(DSLContext dsl,
                                      SurveyQuestionDao questionDao,
                                      SurveyInstanceDao instanceDao,
                                      SurveyQuestionResponseDao responseDao) {
        this.dsl = dsl;
        this.questionDao = questionDao;
        this.instanceDao = instanceDao;
        this.responseDao = responseDao;
    }


    public List<SurveyQuestion> determineActiveQuestions(long surveyInstanceId) {
        List<SurveyQuestion> qs = loadQuestions(surveyInstanceId);
        Map<Long, SurveyQuestionResponse> responsesByQuestionId = loadResponses(surveyInstanceId);

        SurveyInstance instance = instanceDao.getById(surveyInstanceId);
        EntityReference subjectRef = instance.surveyEntity();
        return eval(qs, subjectRef, responsesByQuestionId);
    }


    private List<SurveyQuestion> eval(List<SurveyQuestion> qs,
                                      EntityReference subjectRef,
                                      Map<Long, SurveyQuestionResponse> responsesByQuestionId) {

        QuestionBasePredicateNamespace namespace = mkPredicateNameSpace(qs, subjectRef, responsesByQuestionId);

        JexlBuilder builder = new JexlBuilder();
        JexlEngine jexl = builder
                .namespaces(newHashMap(null, namespace))
                .create();

        namespace.usingEvaluator(jexl);

        return determineActiveQs(qs, jexl);
    }


    private QuestionBasePredicateNamespace mkPredicateNameSpace(List<SurveyQuestion> qs, EntityReference subjectRef, Map<Long, SurveyQuestionResponse> responsesByQuestionId) {
        switch (subjectRef.kind()) {
            case APPLICATION:
                return new QuestionAppPredicateNamespace(
                        dsl,
                        subjectRef,
                        qs,
                        responsesByQuestionId);
            case CHANGE_INITIATIVE:
                return new QuestionChangeInitiativePredicateNamespace(
                        dsl,
                        subjectRef,
                        qs,
                        responsesByQuestionId);
            default:
                return new QuestionBasePredicateNamespace(qs, responsesByQuestionId);
        }
    }


    private static List<SurveyQuestion> determineActiveQs(List<SurveyQuestion> qs, JexlEngine jexl) {
        List<SurveyQuestion> activeQs = qs
                .stream()
                .filter(q -> q
                        .inclusionPredicate()
                        .map(p -> {
                            if (isEmpty(p)) {
                                return true;
                            } else {
                                JexlExpression expr = jexl.createExpression(p);
                                JexlContext jexlCtx = new MapContext();
                                Boolean result = Boolean.valueOf(expr.evaluate(jexlCtx).toString());
                                return result;
                            }
                        })
                        .orElse(true))
                .collect(Collectors.toList());
        return activeQs;
    }


    private Map<Long, SurveyQuestionResponse> loadResponses(long surveyInstanceId) {
        return indexBy(
                responseDao.findForInstance(surveyInstanceId),
                r -> r.questionResponse().questionId(),
                SurveyInstanceQuestionResponse::questionResponse);
    }


    private List<SurveyQuestion> loadQuestions(long surveyInstanceId) {
        return questionDao.findForSurveyInstance(surveyInstanceId);
    }


    // --- TEST ---

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        QuestionPredicateEvaluator evaluator = ctx.getBean(QuestionPredicateEvaluator.class);

        long surveyInstanceId = 147L; // 95L;
        List<SurveyQuestion> activeQs = evaluator.determineActiveQuestions(surveyInstanceId);
        System.out.println("-------------");
        activeQs.forEach(System.out::println);
    }
}
