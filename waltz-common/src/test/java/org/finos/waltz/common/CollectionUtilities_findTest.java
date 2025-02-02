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

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class CollectionUtilities_findTest {

    private static final List<String> words = ListUtilities.newArrayList(
            "a",
            "hello",
            "world");


    @Test(expected = IllegalArgumentException.class)
    public void predicateCannotBeNull() {
        CollectionUtilities.find(null, words);
    }


    @Test(expected = IllegalArgumentException.class)
    public void collectionCannotBeNull() {
        CollectionUtilities.find(d -> true, null);
    }


    @Test
    public void canFindThings() {
        Optional<String> r = CollectionUtilities.find(
                d -> d.contains("e"),
                words);
        assertEquals(Optional.of("hello"), r);
    }


    @Test
    public void emptyReturnedIfNotFound() {
        Optional<String> r = CollectionUtilities.find(
                d -> d.contains("z"),
                words);
        assertEquals(Optional.empty(), r);
    }

}
