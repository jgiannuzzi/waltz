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

package org.finos.waltz.data.role;

import org.finos.waltz.schema.tables.records.RoleRecord;
import org.finos.waltz.model.role.ImmutableRole;
import org.finos.waltz.model.role.Role;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static org.finos.waltz.schema.tables.Role.ROLE;
import static org.finos.waltz.common.Checks.checkNotNull;


@Repository
public class RoleDao {

    private final DSLContext dsl;

    @Autowired
    public RoleDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");

        this.dsl = dsl;
    }

    public boolean create(RoleRecord role) {
            int execute = dsl.insertInto(ROLE)
                    .set(role).execute();
            return execute > 0;
    }

    public Set<Role> findAllRoles() {
        return dsl
                .select(ROLE.fields())
                .from(ROLE)
                .fetchSet(TO_ROLE_RECORD);
    }

    private final static RecordMapper<Record, Role> TO_ROLE_RECORD = r -> {
        RoleRecord record = r.into(ROLE);
        return ImmutableRole.builder()
                .key(record.getKey())
                .name(record.getName())
                .description(record.getDescription())
                .isCustom(record.getIsCustom())
                .build();
    };
}
