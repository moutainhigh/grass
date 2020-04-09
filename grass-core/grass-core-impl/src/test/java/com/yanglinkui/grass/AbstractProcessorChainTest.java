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

package com.yanglinkui.grass;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class AbstractProcessorChainTest {

    @Test
    public void testChain() {
        List<Processor<Integer>> processorList = new ArrayList<>();
        processorList.add(new Processor1());
        processorList.add(new Processor1());
        processorList.add(new Processor1());
        processorList.add(new Processor1());
        processorList.add(new Processor1());
        processorList.add(new Processor1());
        processorList.add(new Processor1());
        processorList.add(new Processor1());

        GrassRequest request = new DefaultGrassRequest();
        TestProcessorChain chain = new TestProcessorChain(request, processorList);
        Integer result = chain.proceed(request);
        assertEquals(6, result);
    }

    @Test
    public void testChain_exception() {
        List<Processor<Integer>> processorList = new ArrayList<>();
        processorList.add(new Processor1());
        processorList.add(new Processor1());
        processorList.add(new Processor1());
        processorList.add(new ExceptionProcessor());
        processorList.add(new Processor1());
        processorList.add(new Processor1());
        processorList.add(new Processor1());
        processorList.add(new Processor1());

        GrassRequest request = new DefaultGrassRequest();
        TestProcessorChain chain = new TestProcessorChain(request, processorList);
        assertThrows(IllegalStateException.class, () -> chain.proceed(request));
    }

    private static class Processor1 implements Processor<Integer> {
        @Override
        public Integer process(Chain<Integer> chain) {
            GrassRequest request = chain.getRequest();
            Integer value = request.getAttribute("value");
            if (value == null) {
                value = new Integer(0);
            } else {
                value = value + 1;
            }

            if (value == 6) { //必须满足5个processor以上
                return value;
            } else {
                request.setAttribute("value", value);
                return chain.proceed(request);
            }
        }
    }

    private static class ExceptionProcessor implements Processor<Integer> {
        @Override
        public Integer process(Chain<Integer> chain) {
            throw new IllegalStateException("Fail");
        }
    }


    private static class TestProcessorChain extends AbstractProcessorChain<Integer> {

        public TestProcessorChain(GrassRequest request, List list) {
            super(request, list);
        }

        public TestProcessorChain(GrassRequest request, List list, int index) {
            super(request, list, index);
        }

        @Override
        protected Processor.Chain createNextChain (GrassRequest request, List list,int index){
            return new TestProcessorChain(request, list, index);
        }
        }
}
