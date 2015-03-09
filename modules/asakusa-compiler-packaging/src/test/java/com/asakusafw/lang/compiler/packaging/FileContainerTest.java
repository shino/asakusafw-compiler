package com.asakusafw.lang.compiler.packaging;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.lang.compiler.common.Location;
import com.asakusafw.lang.compiler.common.testing.FileDeployer;
import com.asakusafw.lang.compiler.common.testing.FileEditor;

/**
 * Test for {@link FileContainer}.
 */
public class FileContainerTest extends ResourceTestRoot {

    /**
     * temporary deployer.
     */
    @Rule
    public FileDeployer deployer = new FileDeployer();

    /**
     * adds a resource by output stream.
     * @throws Exception if failed
     */
    @Test
    public void add_output() throws Exception {
        File base = deployer.getFile("base");
        FileContainer container = new FileContainer(base);
        try (OutputStream output = container.addResource(Location.of("a.txt"))) {
            output.write("Hello, world!".getBytes(ENCODING));
        }
        assertThat(FileEditor.get(new File(base, "a.txt")), contains("Hello, world!"));
    }

    /**
     * adds a resource by input stream.
     * @throws Exception if failed
     */
    @Test
    public void add_input() throws Exception {
        File base = deployer.getFile("base");
        FileContainer container = new FileContainer(base);
        ByteArrayItem item = item("a.txt", "Hello, world!");
        try (InputStream input = item.openResource()) {
            container.addResource(item.getLocation(), input);
        }
        assertThat(FileEditor.get(new File(base, "a.txt")), contains("Hello, world!"));
    }

    /**
     * adds a resource by content provider.
     * @throws Exception if failed
     */
    @Test
    public void add_provider() throws Exception {
        File base = deployer.getFile("base");
        FileContainer container = new FileContainer(base);
        ByteArrayItem item = item("a.txt", "Hello, world!");
        container.addResource(item.getLocation(), item);
        assertThat(FileEditor.get(new File(base, "a.txt")), contains("Hello, world!"));
    }

    /**
     * adds a resource twice.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void add_conflict() throws Exception {
        File base = deployer.getFile("base");
        FileContainer container = new FileContainer(base);
        ByteArrayItem item = item("a.txt", "Hello, world!");
        container.addResource(item.getLocation(), item);
        container.addResource(item.getLocation(), item);
    }

    /**
     * use as repository.
     * @throws Exception if failed
     */
    @Test
    public void repository() throws Exception {
        FileContainer container = new FileContainer(open("structured.zip"));
        Map<String, String> dump = dump(container);
        assertThat(dump.keySet(), hasSize(3));
        assertThat(dump, hasEntry("a.txt", "aaa"));
        assertThat(dump, hasEntry("a/b.txt", "bbb"));
        assertThat(dump, hasEntry("a/b/c.txt", "ccc"));
    }

    /**
     * use as repository.
     * @throws Exception if failed
     */
    @Test
    public void repository_missing() throws Exception {
        FileContainer container = new FileContainer(deployer.getFile("missing"));
        Map<String, String> dump = dump(container);
        assertThat(dump.keySet(), hasSize(0));
    }

    /**
     * use as sink.
     * @throws Exception if failed
     */
    @Test
    public void sink() throws Exception {
        FileContainer container = new FileContainer(open("structured.zip"));
        FileContainer target = new FileContainer(deployer.getFile("target"));
        try (ResourceSink sink = target.createSink()) {
            ResourceUtil.copy(container, sink);
        }
        Map<String, String> dump = dump(target);
        assertThat(dump.keySet(), hasSize(3));
        assertThat(dump, hasEntry("a.txt", "aaa"));
        assertThat(dump, hasEntry("a/b.txt", "bbb"));
        assertThat(dump, hasEntry("a/b/c.txt", "ccc"));
    }

    /**
     * visit.
     * @throws Exception if failed
     */
    @Test
    public void visit() throws Exception {
        FileContainer container = new FileContainer(open("structured.zip"));
        final Set<String> locations = new HashSet<>();
        container.accept(new FileVisitor() {
            @Override
            public boolean process(Location location, File file) throws IOException {
                if (file.isFile()) {
                    locations.add(location.toPath());
                }
                return true;
            }
        });
        assertThat(locations, containsInAnyOrder("a.txt", "a/b.txt", "a/b/c.txt"));
    }

    private File open(String name) {
        String path = "ResourceRepository.files/" + name;
        return deployer.extract(path, name);
    }
}
