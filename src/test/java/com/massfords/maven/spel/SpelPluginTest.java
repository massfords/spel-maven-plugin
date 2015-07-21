package com.massfords.maven.spel;

import org.junit.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author slazarus
 */
public class SpelPluginTest {

    private SpelPlugin plugin = new SpelPlugin();

    private static final List<String> classNames = Arrays.asList(
            "foo.jar",
            "baz.jar");

    /**
     * The only purpose of this test is to have a reference to the SpelPlugin
     * appear somewhere in code so the IDE (IntelliJ) doesn't show a warning
     * marker that the SpelPlugin class isn't used. I like having that rule
     * enabled but can't suppress this particular appearance of it w/o risking
     * suppressing legitimate unused declaration exceptions within that class.
     * @throws Exception
     */
    @Test
    public void createMavenClasspathTest() throws Exception {
        URL[] urls = plugin.buildMavenClasspath(classNames);
        assertEquals(urls.length, classNames.size());
    }

}
