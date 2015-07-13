package com.massfords.maven.spel;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * @author slazarus
 */
public class SpelPluginTest {

    private SpelPlugin plugin = new SpelPlugin();

    private static final List<String> classNames = new ArrayList<String>(
        Arrays.asList(new String[] {
            "com/massfords/maven/spel/SpelPluginTest.java",
            "com/massfords/maven/spel/SpellPlugin.java"
        })
    );

    @Test
    public void createMavenClasspathTest() throws Exception {
        URL[] urls = plugin.buildMavenClasspath(classNames);
        assertEquals(urls.length, classNames.size());
    }

}
