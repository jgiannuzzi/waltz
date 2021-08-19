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

import template from "./recalculate-view.html";
import {CORE_API} from "../common/services/core-api-utils";
import toasts from "../svelte-stores/toast-store";


function controller(serviceBroker) {
    const vm = this;

    vm.recalcFlowRatings = () => {
        toasts.info("Flow Ratings recalculation requested");
        serviceBroker
            .execute(CORE_API.FlowClassificationRuleStore.recalculateAll)
            .then(() => toasts.success("Flow Ratings recalculated"));
    };

    vm.recalcDataTypeUsages = () => {
        toasts.info("Data Type Usage recalculation requested");
        serviceBroker
            .execute(CORE_API.DataTypeUsageStore.recalculateAll)
            .then(() => toasts.success("Data Type Usage recalculated"));
    };

}


controller.$inject = [
    "ServiceBroker"
];


const page = {
    template,
    controller,
    controllerAs: "ctrl"
};


export default page;