package com.project.library;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

public class ModularityTests {

    @Test 
    public void modulesAreDiscoveredAndValid() {
        var modules = ApplicationModules.of(LibraryApplication.class);
        var set = modules.stream()
                         .map(module -> module.getIdentifier().toString())
                         .collect(Collectors.toSet());

        assertEquals(Set.of("catalog", "circulation", "patron", "billing", "notification"), set);

        modules.verify();
    }
}