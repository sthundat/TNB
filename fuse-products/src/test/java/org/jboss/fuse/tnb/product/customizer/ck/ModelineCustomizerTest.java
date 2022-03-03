package org.jboss.fuse.tnb.product.customizer.ck;

import static org.junit.jupiter.api.Assertions.fail;

import static org.assertj.core.api.Assertions.assertThat;

import org.jboss.fuse.tnb.common.product.ProductType;
import org.jboss.fuse.tnb.common.utils.IOUtils;
import org.jboss.fuse.tnb.product.ck.customizer.ModelineCustomizer;
import org.jboss.fuse.tnb.product.ck.integration.builder.CamelKIntegrationBuilder;
import org.jboss.fuse.tnb.product.customizer.Customizer;
import org.jboss.fuse.tnb.product.customizer.CustomizerTestParent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

@Tag("unit")
public class ModelineCustomizerTest extends CustomizerTestParent {
    private String yamlIntegration;
    private String javaIntegration;

    @Override
    public ProductType product() {
        return ProductType.CAMEL_K;
    }

    @BeforeEach
    public void loadIntegrations() {
        try {
            javaIntegration = IOUtils.readFile(
                Paths.get(this.getClass().getResource("/org/jboss/fuse/tnb/product/customizer/ck/Integration.java").toURI()));
            yamlIntegration = IOUtils.readFile(
                Paths.get(this.getClass().getResource("/org/jboss/fuse/tnb/product/customizer/ck/Integration.yaml").toURI()));
        } catch (Exception e) {
            fail("Unable to load integrations", e);
        }
    }

    private String exampleClassWithModeline(String... modeline) {
        StringBuilder sb = new StringBuilder();
        for (String m : modeline) {
            sb.append(m).append("\n");
        }
        sb.append(javaIntegration);
        return sb.toString().trim();
    }

    @Test
    public void shouldCreateModelineInRBTest() {
        customizer.doCustomize();

        assertThat(ib.getRouteBuilder().get().getComment()).isPresent();
        assertThat(ib.getRouteBuilder().get().getComment().get().toString().trim()).isEqualTo("// camel-k: test=property");
    }

    @Test
    public void shouldPrependToExistingModelineInRBTest() {
        ib.getRouteBuilder().get().setLineComment("camel-k: k1=v1 k2=v2");
        customizer.doCustomize();

        assertThat(ib.getRouteBuilder().get().getComment()).isPresent();
        assertThat(ib.getRouteBuilder().get().getComment().get().toString().trim()).isEqualTo("// camel-k: test=property k1=v1 k2=v2");
    }

    @Test
    public void shouldCreateModelineInStringTest() {
        final String cls = exampleClassWithModeline();

        CamelKIntegrationBuilder ib = new CamelKIntegrationBuilder("").fromString(cls);
        customizer.setIntegrationBuilder(ib);
        customizer.doCustomize();

        assertThat(ib.getContent().trim()).isEqualTo("// camel-k: test=property\n" + cls);
    }

    @Test
    public void shouldPrependToExistingModelineInStringTest() {
        final String cls = exampleClassWithModeline("// camel-k: k1=v1");
        CamelKIntegrationBuilder ib = new CamelKIntegrationBuilder("").fromString(cls);
        customizer.setIntegrationBuilder(ib);
        customizer.doCustomize();

        assertThat(ib.getContent().trim()).isEqualTo("// camel-k: test=property k1=v1\n" + cls.substring(cls.indexOf("\n") + 1));
    }

    @Test
    public void shouldPrependToExistingMultipleModelinesInStringTest() {
        final String cls = exampleClassWithModeline("// camel-k: k1=v1", "// camel-k: k2=v2");

        CamelKIntegrationBuilder ib = new CamelKIntegrationBuilder("").fromString(cls);
        customizer.setIntegrationBuilder(ib);
        customizer.doCustomize();

        String remainder = cls.substring(cls.indexOf("\n") + 1);
        remainder = remainder.substring(remainder.indexOf("\n") + 1);
        assertThat(ib.getContent().trim()).isEqualTo("// camel-k: test=property k1=v1 k2=v2\n" + remainder);
    }

    @Test
    public void shouldPrependToModelineWithNoSpaceInStringTest() {
        final String cls = exampleClassWithModeline("//camel-k: k1=v1");

        CamelKIntegrationBuilder ib = new CamelKIntegrationBuilder("").fromString(cls);
        customizer.setIntegrationBuilder(ib);
        customizer.doCustomize();

        assertThat(ib.getContent().trim()).isEqualTo("// camel-k: test=property k1=v1\n" + cls.substring(cls.indexOf("\n") + 1));
    }

    @Test
    public void shouldCreateModelineInYamlStringTest() {
        CamelKIntegrationBuilder ib = new CamelKIntegrationBuilder("").fromString(yamlIntegration).fileName("test.yaml");
        customizer.setIntegrationBuilder(ib);
        customizer.doCustomize();

        assertThat(ib.getContent().trim()).isEqualTo("# camel-k: test=property\n" + yamlIntegration.trim());
    }

    @Test
    public void shouldPrependToExistingModelineInYamlStringTest() {
        final String source = "# camel-k: k1=v1\n" + yamlIntegration;
        CamelKIntegrationBuilder ib = new CamelKIntegrationBuilder("").fromString(source).fileName("test.yaml");
        customizer.setIntegrationBuilder(ib);
        customizer.doCustomize();

        assertThat(ib.getContent().trim()).isEqualTo("# camel-k: test=property k1=v1\n" + source.substring(source.indexOf("\n") + 1).trim());
    }

    @Override
    public Customizer newCustomizer() {
        return new ModelineCustomizer("test=property");
    }
}
