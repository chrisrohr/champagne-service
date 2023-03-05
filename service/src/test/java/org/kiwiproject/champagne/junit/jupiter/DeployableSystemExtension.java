package org.kiwiproject.champagne.junit.jupiter;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.kiwiproject.champagne.model.DeployableSystemThreadLocal;

public class DeployableSystemExtension implements BeforeEachCallback, AfterEachCallback {

    private final long systemId;

    // Used by Jupiter's ExtendWith
    @SuppressWarnings("unused")
    public DeployableSystemExtension() {
        this(1L);
    }

    public DeployableSystemExtension(long systemId) {
        this.systemId = systemId;
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        DeployableSystemThreadLocal.setCurrentDeployableSystem(systemId);
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        DeployableSystemThreadLocal.clearDeployableSystem();
    }
}
