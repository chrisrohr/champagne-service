package org.kiwiproject.champagne.junit.jupiter;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.kiwiproject.champagne.model.DeployableSystemThreadLocal;

public class DeployableSystemExtension implements BeforeEachCallback, AfterEachCallback {

    private final long systemId;

    private final boolean admin;

    // Used by Jupiter's ExtendWith
    @SuppressWarnings("unused")
    public DeployableSystemExtension() {
        this(1L, false);
    }

    public DeployableSystemExtension(long systemId, boolean admin) {
        this.systemId = systemId;
        this.admin = admin;
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        DeployableSystemThreadLocal.setCurrentDeployableSystem(systemId, admin);
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        DeployableSystemThreadLocal.clearDeployableSystem();
    }
}
