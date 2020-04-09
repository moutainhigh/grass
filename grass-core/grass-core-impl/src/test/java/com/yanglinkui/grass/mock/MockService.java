/*
 * Copyright 2019 Jonas Yang
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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yanglinkui.grass.mock;

import com.yanglinkui.grass.annotation.GrassService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@GrassService(prefix = "service")
public class MockService implements MockClient{
    private List<String> userNameList;

    private CompletableFuture<Integer> userAge;

    private String defaultMethod;

    public MockService() {
    }

    public MockService(List<String> userNameList, CompletableFuture<Integer> userAge, String defaultMethod) {
        this.userNameList = userNameList;
        this.userAge = userAge;
        this.defaultMethod = defaultMethod;
    }

    @Override
    public List<String> getUseName(Long id) {
        return this.userNameList;
    }

    @Override
    public CompletableFuture<Integer> getUserAge(Long id, String name) {
        return this.userAge;
    }

    @Override
    public String defaultMethod(String name) {
        return this.defaultMethod;
    }
}
