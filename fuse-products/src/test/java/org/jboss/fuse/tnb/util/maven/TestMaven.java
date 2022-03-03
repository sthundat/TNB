package org.jboss.fuse.tnb.util.maven;

import org.jboss.fuse.tnb.product.util.maven.Maven;

import org.apache.maven.shared.invoker.Invoker;

public final class TestMaven extends Maven {
    public static void setupDefaultMaven() {
        initialized = false;
        invoker = null;
        Maven.setupMaven();
    }

    public static void setupTestMaven(Invoker i) {
        setupDefaultMaven();
        invoker = i;
    }
}
