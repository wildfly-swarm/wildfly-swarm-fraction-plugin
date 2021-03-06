package org.wildfly.swarm.plugin.process;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.wildfly.swarm.plugin.DependencyMetadata;
import org.wildfly.swarm.plugin.FractionMetadata;
import org.wildfly.swarm.plugin.MavenDependencyData;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * @author Bob McWhirter
 */
public class FractionManifestGenerator {

    private final Log log;
    private final MavenProject project;
    private final Set<MavenDependencyData> mavenDependencyData;

    public FractionManifestGenerator(Log log, MavenProject project, Set<MavenDependencyData> mavenDependencyData) {
        this.log = log;
        this.project = project;
        this.mavenDependencyData = mavenDependencyData;
    }

    public FractionMetadata apply(FractionMetadata meta) throws MojoExecutionException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(options);

        Map<String, Object> data = new LinkedHashMap<String, Object>() {{
            put("name", meta.getName());
            put("description", meta.getDescription());
            put("groupId", meta.getGroupId());
            put("artifactId", meta.getArtifactId());
            put("version", meta.getVersion());
            if (meta.hasJavaCode() && meta.getModule() != null) {
                put("module", meta.getModule());
            }
            put("stability", new HashMap<String, Object>() {{
                put("level", meta.getStabilityIndex().toString());
                put("index", meta.getStabilityIndex().ordinal());
            }});
            put("internal", meta.isInternal());
            // module dependencies per the JBoss Modules module.xml declarations
            put("dependencies",
                meta.getDependencies()
                        .stream()
                        .map(DependencyMetadata::toString)
                        .collect(Collectors.toList())
            );
            // transitive module dependencies per the JBoss Modules module.xml declarations
            put("transitive-dependencies",
                meta.getTransitiveDependencies()
                        .stream()
                        .map(DependencyMetadata::toString)
                        .collect(Collectors.toList())
            );
            put("maven-dependencies",
                    mavenDependencyData.stream()
                            .map(MavenDependencyData::toString)
                            .collect(Collectors.toList()));
        }};

        Path file = Paths.get(this.project.getBuild().getOutputDirectory(), "META-INF", "fraction-manifest.yaml");
        try {
            Files.createDirectories(file.getParent());
            try (Writer out = new FileWriter(file.toFile())) {
                yaml.dump(data, out);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Failed writing fraction-manifest.yaml", e);
        }

        return meta;
    }
}
