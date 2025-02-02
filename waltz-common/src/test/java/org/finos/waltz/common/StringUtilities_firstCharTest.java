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

package org.finos.waltz.common;

import org.junit.Test;

import static org.finos.waltz.common.StringUtilities.firstChar;
import static org.junit.Assert.assertEquals;

public class StringUtilities_firstCharTest {

    @Test
    public void getsFirstCharacter() {
        assertEquals('F', (char) StringUtilities.firstChar("Foo", 'Z'));
    }

    @Test
    public void defaultsOnNull() {
        assertEquals('Z', (char) StringUtilities.firstChar(null, 'Z'));
    }

    @Test
    public void defaultsOnEmpty() {
        assertEquals('Z', (char) StringUtilities.firstChar("", 'Z'));
    }

}
