package org.jboss.fuse.tnb.product.ck.configuration;

import org.jboss.fuse.tnb.common.config.Configuration;

public abstract class CamelKConfiguration extends Configuration {
    private static final String FORCE_UPSTREAM = "force.upstream";

    protected static final String SUBSCRIPTION_CHANNEL = "subscription.channel";
    protected static final String SUBSCRIPTION_OPERATOR_NAME = "subscription.operatorName";
    protected static final String SUBSCRIPTION_SOURCE = "subscription.source";
    protected static final String SUBSCRIPTION_SOURCE_NAMESPACE = "subscription.sourceNamespace";

    public static final String SUBSCRIPTION_NAME = "test-camel-k";

    public abstract String subscriptionChannel();

    public abstract String subscriptionOperatorName();

    public abstract String subscriptionSource();

    public abstract String subscriptionSourceNamespace();

    public static boolean forceUpstream() {
        return getBoolean(FORCE_UPSTREAM, false);
    }

    public static CamelKConfiguration getConfiguration() {
        if (forceUpstream()) {
            return new CamelKUpstreamConfiguration();
        } else {
            return new CamelKProdConfiguration();
        }
    }
}