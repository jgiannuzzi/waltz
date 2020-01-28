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

import _ from "lodash";

import { CORE_API } from "../../../common/services/core-api-utils";
import { initialiseData } from "../../../common";
import { mkSelectionOptions } from "../../../common/selector-utils";

import template from "./change-initiative-section.html";
import { changeInitiative } from "../../../common/services/enums/change-initiative";
import { getEnumName } from "../../../common/services/enums";
import indexByKeyForType from "../../../enum-value/enum-value-utilities";
import {isSameParentEntityRef} from "../../../common/entity-utils";
import { fakeInitiative, fakeProgramme } from "../../change-initiative-utils";
import { filterByAssessmentRating, mkAssessmentSummaries } from "../../../assessments/assessment-utils";

const bindings = {
    parentEntityRef: "<",
};



const externalIdCellTemplate = `
    <div class="ui-grid-cell-contents"
         style="vertical-align: baseline; ">
        <waltz-entity-link icon-placement="none"
                           entity-ref="COL_FIELD">
        </waltz-entity-link>
    </div>
`;


function mkRefCol(propName) {
    return {
        width: "15%",
        field: propName,
        toSearchTerm: d => _.get(d, [propName, "name"], ""),
        cellTemplate: externalIdCellTemplate
    };
}


const initialState = {
    changeInitiatives: [],
    changeInitiativeLifecyclePhaseByKey: {},
    displayRetiredCis: false,
    selectedAssessmentRating: null,
    selectedChange: null,
    visibility: {
        sourcesOverlay: false
    },
    filterHelpText: "Select an assessment category to filter the change initiatives",
    gridOptions: {
        columnDefs: [
            { width: "15%", field: "kind", name: "Kind" },
            mkRefCol("initiative"),
            mkRefCol("programme"),
            mkRefCol("project"),
            { width: "25%", field: "name", name: "Name" },
            { width: "15%", field: "lifecyclePhase", name: "Phase" }
        ],
        data: []
    }
};


function determineHierarchy(cisById = {}, ci) {
    const none = null;

    switch (ci.changeInitiativeKind) {
        case "INITIATIVE":
            return {
                initiative: ci,
                programme: none,
                project: none,
            };
        case "PROGRAMME":
            const programmeParent = cisById[ci.parentId] || fakeInitiative;
            return {
                initiative: programmeParent,
                programme: ci,
                project: none,
            };
        case "PROJECT":
            const projectParent = cisById[ci.parentId] || fakeProgramme;
            const projectProgrammeParent = cisById[projectParent.parentId] || fakeInitiative;
            return {
                initiative: projectProgrammeParent,
                programme: projectParent || fakeInitiative,
                project: ci,
            };
        default:
            return {
                initiative: none,
                programme: none,
                project: none,
            };
    }
}


function toExtRef(d) {
    if (!d) {
        return null;
    } else {
        return {
            kind: d.kind,
            name: d.externalId,
            id: d.id
        };
    }
}


function mkTableData(changeInitiatives = [], lifecycleNamesByKey = {}) {
    const cisById = _.keyBy(changeInitiatives, d => d.id);

    return _
        .chain(changeInitiatives)
        .map(ci => {
            const hierarchy = determineHierarchy(cisById, ci);
            const phaseName = getEnumName(lifecycleNamesByKey, ci.lifecyclePhase);
            const changeKind = getEnumName(changeInitiative, ci.changeInitiativeKind);
            return {
                initiative: toExtRef(hierarchy.initiative),
                programme: toExtRef(hierarchy.programme),
                project: toExtRef(hierarchy.project),
                name: ci.name,
                description: ci.description,
                lifecyclePhase: phaseName,
                kind: changeKind
            }
        })
        .orderBy(d => ["initiative.name", "programme.name", "project.name", "name"])
        .value();
}


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    function init() {
        const schemesPromise = serviceBroker
            .loadAppData(CORE_API.RatingSchemeStore.findAll)
            .then(r => vm.ratingSchemes = r.data);

        const assessmentDefinitionsPromise = serviceBroker
            .loadAppData(
                CORE_API.AssessmentDefinitionStore.findByKind,
                [ "CHANGE_INITIATIVE" ])
            .then(r => vm.assessmentDefinitions = r.data);

        const enumPromise = serviceBroker
            .loadAppData(CORE_API.EnumValueStore.findAll)
            .then(r => {
                vm.changeInitiativeLifecyclePhaseByKey = indexByKeyForType(
                    r.data,
                    "changeInitiativeLifecyclePhase");
            });

        return $q
            .all([schemesPromise, assessmentDefinitionsPromise, enumPromise]);
    }


    vm.$onChanges = (changes) => {
        const sameParent = isSameParentEntityRef(changes);

        if (vm.parentEntityRef && !sameParent) {
            const selectionOptions = mkSelectionOptions(vm.parentEntityRef);
            const ciPromise = serviceBroker
                .loadViewData(
                    CORE_API.ChangeInitiativeStore.findHierarchyBySelector,
                    [ selectionOptions ])
                .then(r => vm.changeInitiatives = r.data);

            const assessmentRatingsPromise = serviceBroker
                .loadViewData(
                    CORE_API.AssessmentRatingStore.findByTargetKindForRelatedSelector,
                    [ "CHANGE_INITIATIVE", selectionOptions ])
                .then(r => vm.assessmentRatings = r.data);

            $q.all([init(), ciPromise, assessmentRatingsPromise])
                .then(() => applyFilters());
        }
    };

    function applyFilters() {

        const retiredCiFilter =  vm.displayRetiredCis
            ? () => true
            : ci => ci.lifecyclePhase !== "RETIRED";

        const inScopeCis = _.filter(vm.changeInitiatives, retiredCiFilter);
        const inScopeCisById = _.keyBy(inScopeCis, ci => ci.id);

        const inScopeRatings = _.filter(
            vm.assessmentRatings,
            r => inScopeCisById[r.entityReference.id] !== null);

        const relevantCis = vm.selectedAssessmentRating === null
            ? inScopeCis
            : filterByAssessmentRating(
                inScopeCis,
                inScopeRatings,
                {
                    assessmentId: vm.selectedAssessmentSummary.definition.id,
                    ratingId: vm.selectedAssessmentRating.rating.id
                });

        vm.assessmentSummaries = mkAssessmentSummaries(
            vm.assessmentDefinitions,
            vm.ratingSchemes,
            inScopeRatings,
            inScopeCis.length);

        vm.gridOptions.data = mkTableData(
            relevantCis,
            vm.changeInitiativeLifecyclePhaseByKey);

    }

    vm.onSelectAssessmentRating = d => {
        vm.selectedAssessmentRating = d;
        applyFilters();
    };

    vm.onToggleDisplayRetiredCis = () => {
        vm.displayRetiredCis = ! vm.displayRetiredCis;
        vm.selectedAssessmentSummary = null;
        vm.selectedAssessmentRating = null;
        applyFilters();
    };

    vm.onSelectAssessmentSummary = (summary) => {
        vm.selectedAssessmentSummary = summary;
        vm.selectedAssessmentRating = null;
        applyFilters();
    };

}


controller.$inject = [
    "$q",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzChangeInitiativeSection"
};
