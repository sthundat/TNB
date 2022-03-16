package org.jboss.fuse.tnb.product.csb.application;

import org.jboss.fuse.tnb.common.config.OpenshiftConfiguration;
import org.jboss.fuse.tnb.common.config.TestConfiguration;
import org.jboss.fuse.tnb.common.openshift.OpenshiftClient;
import org.jboss.fuse.tnb.product.deploystrategy.OpenshiftDeployStrategyFactory;
import org.jboss.fuse.tnb.product.integration.builder.AbstractIntegrationBuilder;
import org.jboss.fuse.tnb.product.integration.builder.AbstractMavenGitIntegrationBuilder;
import org.jboss.fuse.tnb.product.interfaces.OpenshiftDeployer;
import org.jboss.fuse.tnb.product.log.OpenshiftLog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.internal.readiness.Readiness;
import io.fabric8.kubernetes.client.utils.PodStatusUtil;

public class OpenshiftSpringBootApp extends SpringBootApp {
    private static final Logger LOG = LoggerFactory.getLogger(OpenshiftSpringBootApp.class);
    private final Path baseDirectory;
    private final String finalName;
    private final OpenshiftDeployer deploymentStrategy;

    public OpenshiftSpringBootApp(AbstractIntegrationBuilder<?> integrationBuilder) {
        super(integrationBuilder);
        deploymentStrategy = OpenshiftDeployStrategyFactory.getDeployStrategy(integrationBuilder);

        if (integrationBuilder instanceof AbstractMavenGitIntegrationBuilder) {
            AbstractMavenGitIntegrationBuilder<?> mavenGitIntegrationBuilder = (AbstractMavenGitIntegrationBuilder<?>) integrationBuilder;
            baseDirectory = mavenGitApp.getProjectLocation();
            finalName = mavenGitIntegrationBuilder.getFinalName().orElse(getName());
        } else {
            baseDirectory = TestConfiguration.appLocation().resolve(name);
            finalName = getName();
        }
        deploymentStrategy
            .setBaseDirectory(baseDirectory)
            .setName(finalName);
    }

    @Override
    public void start() {
        LOG.info("deploy app using {}", deploymentStrategy.getClass().getSimpleName());
        deploymentStrategy.deploy();
        endpoint = deploymentStrategy.getEndpoint();
        log = deploymentStrategy.getLog();
    }

    @Override
    public void stop() {
        if (getLog() != null) {
            ((OpenshiftLog) getLog()).save(started);
        }
        deploymentStrategy.undeploy();
    }

    @Override
    public boolean isReady() {
        try {
            final List<Pod> pods = OpenshiftClient.get().getLabeledPods(Map.of(OpenshiftConfiguration.openshiftDeploymentLabel(), finalName));
            return !pods.isEmpty() && pods.stream().allMatch(Readiness::isPodReady);
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public boolean isFailed() {
        try {
            final List<Pod> pods = OpenshiftClient.get().getLabeledPods(Map.of(OpenshiftConfiguration.openshiftDeploymentLabel(), finalName));
            return !pods.isEmpty() && pods.stream().map(PodStatusUtil::getContainerStatus)
                .allMatch(containerStatuses -> containerStatuses.stream()
                    .anyMatch(containerStatus -> "error".equalsIgnoreCase(containerStatus.getLastState().getTerminated().getReason())));
        } catch (Exception ignored) {
            return false;
        }
    }
}
