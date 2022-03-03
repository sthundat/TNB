package org.jboss.fuse.tnb.product.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.jboss.fuse.tnb.common.config.OpenshiftConfiguration;
import org.jboss.fuse.tnb.common.config.TestConfiguration;
import org.jboss.fuse.tnb.product.Product;
import org.jboss.fuse.tnb.product.ProductFactory;
import org.jboss.fuse.tnb.product.ck.CamelK;
import org.jboss.fuse.tnb.product.cq.LocalCamelQuarkus;
import org.jboss.fuse.tnb.product.cq.OpenshiftCamelQuarkus;
import org.jboss.fuse.tnb.product.csb.LocalCamelSpringBoot;
import org.jboss.fuse.tnb.product.csb.OpenshiftCamelSpringBoot;
import org.jboss.fuse.tnb.product.parent.TestParent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@Tag("unit")
public class ProductFactoryTest extends TestParent {
    @AfterEach
    public void afterEach() {
        System.clearProperty(TestConfiguration.PRODUCT);
        System.clearProperty(OpenshiftConfiguration.USE_OPENSHIFT);
    }

    @ParameterizedTest
    @MethodSource("products")
    public void shouldReturnCorrectInstanceTest(String product, boolean openshift, Class<? extends Product> productClass) {
        System.setProperty(TestConfiguration.PRODUCT, product);
        if (openshift) {
            System.setProperty(OpenshiftConfiguration.USE_OPENSHIFT, "true");
        }
        assertThat(ProductFactory.create()).isInstanceOf(productClass);
    }

    private static Stream<Arguments> products() {
        return Stream.of(
            Arguments.of("camelspringboot", false, LocalCamelSpringBoot.class),
            Arguments.of("camelspringboot", true, OpenshiftCamelSpringBoot.class),
            Arguments.of("camelquarkus", false, LocalCamelQuarkus.class),
            Arguments.of("camelquarkus", true, OpenshiftCamelQuarkus.class),
            Arguments.of("camelk", true, CamelK.class)
        );
    }

    @Test
    public void shouldThrowExceptionForUnsupportedConfigurationTest() {
        System.setProperty(TestConfiguration.PRODUCT, "camelk");
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(ProductFactory::create);
    }
}
