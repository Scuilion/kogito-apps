/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.trusty.service.common.handlers;

import java.util.List;
import java.util.stream.Stream;

import javax.enterprise.inject.Instance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.explainability.api.CounterfactualExplainabilityResult;
import org.kie.kogito.explainability.api.LIMEExplainabilityResult;
import org.kie.kogito.persistence.api.Storage;
import org.kie.kogito.persistence.api.query.Query;
import org.kie.kogito.trusty.storage.api.model.Decision;
import org.kie.kogito.trusty.storage.common.TrustyStorageService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExplainerServiceHandlerRegistryTest {

    private static final String EXECUTION_ID = "executionId";

    private static final String COUNTERFACTUAL_ID = "counterfactualId";

    private static final String SOLUTION_ID = "solutionId";

    private LIMEExplainerServiceHandler limeExplainerServiceHandler;
    private CounterfactualExplainerServiceHandler counterfactualExplainerServiceHandler;
    private Storage<String, LIMEExplainabilityResult> storageLIME;
    private Storage<String, CounterfactualExplainabilityResult> storageCounterfactual;
    private Decision decision;

    private ExplainerServiceHandlerRegistry registry;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setup() {
        TrustyStorageService trustyStorage = mock(TrustyStorageService.class);
        limeExplainerServiceHandler = spy(new LIMEExplainerServiceHandler(trustyStorage));
        counterfactualExplainerServiceHandler = spy(new CounterfactualExplainerServiceHandler(trustyStorage,
                mock(CounterfactualExplainabilityResultsManagerSlidingWindow.class),
                mock(CounterfactualExplainabilityResultsManagerDuplicates.class)));
        Instance<ExplainerServiceHandler<?>> explanationHandlers = mock(Instance.class);
        when(explanationHandlers.stream()).thenReturn(Stream.of(limeExplainerServiceHandler, counterfactualExplainerServiceHandler));
        registry = new ExplainerServiceHandlerRegistry(explanationHandlers);

        storageLIME = mock(Storage.class);
        storageCounterfactual = mock(Storage.class);
        when(trustyStorage.getLIMEResultStorage()).thenReturn(storageLIME);
        when(trustyStorage.getCounterfactualResultStorage()).thenReturn(storageCounterfactual);

        decision = mock(Decision.class);
    }

    @Test
    public void testLIME_getExplainabilityResultById() {
        when(storageLIME.containsKey(eq(EXECUTION_ID))).thenReturn(true);

        registry.getExplainabilityResultById(EXECUTION_ID, LIMEExplainabilityResult.class);

        verify(limeExplainerServiceHandler).getExplainabilityResultById(eq(EXECUTION_ID));
    }

    @Test
    public void testLIME_storeExplainabilityResult() {
        when(storageLIME.containsKey(eq(EXECUTION_ID))).thenReturn(false);
        LIMEExplainabilityResult result = mock(LIMEExplainabilityResult.class);

        registry.storeExplainabilityResult(EXECUTION_ID, result);

        verify(limeExplainerServiceHandler).storeExplainabilityResult(eq(EXECUTION_ID), eq(result));
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testCounterfactual_getExplainabilityResultByIdWithFinalResult() {
        Query query = mock(Query.class);
        CounterfactualExplainabilityResult result = mock(CounterfactualExplainabilityResult.class);
        when(result.getStage()).thenReturn(CounterfactualExplainabilityResult.Stage.FINAL);
        when(storageCounterfactual.containsKey(eq(EXECUTION_ID))).thenReturn(true);
        when(storageCounterfactual.query()).thenReturn(query);
        when(query.filter(any())).thenReturn(query);
        when(query.execute()).thenReturn(List.of(result));

        CounterfactualExplainabilityResult actual = registry.getExplainabilityResultById(EXECUTION_ID, CounterfactualExplainabilityResult.class);

        verify(counterfactualExplainerServiceHandler).getExplainabilityResultById(eq(EXECUTION_ID));
        assertEquals(result, actual);
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testCounterfactual_getExplainabilityResultByIdWithOnlyIntermediateResults() {
        Query query = mock(Query.class);
        CounterfactualExplainabilityResult result = mock(CounterfactualExplainabilityResult.class);
        when(result.getStage()).thenReturn(CounterfactualExplainabilityResult.Stage.INTERMEDIATE);
        when(storageCounterfactual.containsKey(eq(EXECUTION_ID))).thenReturn(true);
        when(storageCounterfactual.query()).thenReturn(query);
        when(query.filter(any())).thenReturn(query);
        when(query.execute()).thenReturn(List.of(result));

        assertThrows(IllegalArgumentException.class, () -> registry.getExplainabilityResultById(EXECUTION_ID, CounterfactualExplainabilityResult.class));
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testCounterfactual_getExplainabilityResultByIdWithAllResults() {
        Query query = mock(Query.class);
        CounterfactualExplainabilityResult result1 = mock(CounterfactualExplainabilityResult.class);
        CounterfactualExplainabilityResult result2 = mock(CounterfactualExplainabilityResult.class);
        when(result1.getStage()).thenReturn(CounterfactualExplainabilityResult.Stage.INTERMEDIATE);
        when(result2.getStage()).thenReturn(CounterfactualExplainabilityResult.Stage.FINAL);
        when(storageCounterfactual.containsKey(eq(EXECUTION_ID))).thenReturn(true);
        when(storageCounterfactual.query()).thenReturn(query);
        when(query.filter(any())).thenReturn(query);
        when(query.execute()).thenReturn(List.of(result1, result2));

        CounterfactualExplainabilityResult actual = registry.getExplainabilityResultById(EXECUTION_ID, CounterfactualExplainabilityResult.class);

        verify(counterfactualExplainerServiceHandler).getExplainabilityResultById(eq(EXECUTION_ID));
        assertEquals(result2, actual);
    }

    @Test
    public void testCounterfactual_storeExplainabilityResult() {
        when(storageCounterfactual.containsKey(eq(EXECUTION_ID))).thenReturn(false);
        CounterfactualExplainabilityResult result = mock(CounterfactualExplainabilityResult.class);

        registry.storeExplainabilityResult(EXECUTION_ID, result);

        verify(counterfactualExplainerServiceHandler).storeExplainabilityResult(eq(EXECUTION_ID), eq(result));
    }
}
