package com.massfords.maven.spel;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.reflections.Reflections;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.annotation.Generated;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Goal for scanning class files looking for annotations with Spring Expression
 * Language expressions and parsing during the build to report any errors.
 */
@Mojo(name = "spel", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class SpelPlugin extends AbstractMojo {

    @Component
    private MavenProject project;

    // todo - find and add any other common annotations
    private final List<SpelAnnotation> defaults = Arrays.asList( new SpelAnnotation(PreAuthorize.class.getName()) );

    private int errorCount = 0;

    @Parameter(property = "maxValidationErrors", required = false, defaultValue = "100")
    private int maxValidationErrors;

    @Parameter(property = "annotations", required = false)
    private List<SpelAnnotation> annotations = defaults;

    public void execute() throws MojoExecutionException {

        ExpressionParser parser = new SpelExpressionParser();

        try {
            URL[] urls = buildMavenClasspath();
            // ... and now you can pass the above classloader to Reflections
            Reflections reflections = new Reflections(urls);

            for(SpelAnnotation sa : annotations) {
                Class<? extends Annotation> annoType;
                try {
                    //noinspection unchecked
                    annoType = (Class<? extends Annotation>) Class.forName(sa.getName());
                } catch (Exception e) {
                    reportError("Could not find and instantiate class for annotation with name: " + sa.getName());
                    continue;
                }
                // todo - Allow configurable restriction of search to specific packages
                Set<Method> set = reflections.getMethodsAnnotatedWith(annoType);
                for(Method m : set) {
                    Annotation anno = m.getAnnotation(annoType);

                    Method attrGetter = annoType.getDeclaredMethod(sa.getAttribute());

                    Object expressionObj = attrGetter.invoke(anno);
                    if (!(expressionObj instanceof String)) {
                        reportError("Attribute " + sa.getAttribute() + " in " + sa.getName() + " was not a string");
                        continue;
                    }
                    String expression = (String) attrGetter.invoke(anno);
                    try {
                        Expression exp = parser.parseExpression(expression);

                    } catch (ParseException e) {
                        reportError("Spel annotation " + sa.getName() + " attribute " + sa.getAttribute() +
                                " failed to parse: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException("A fatal error occurred while validating Spel annotations," +
                    " see stack trace for details.", e);
        }
    }

    protected URL[] buildMavenClasspath() throws DependencyResolutionRequiredException, MojoExecutionException {
        return buildMavenClasspath(project.getCompileClasspathElements());
    }

    protected URL[] buildMavenClasspath(List<String> classpathElements) throws MojoExecutionException {
        List<URL> projectClasspathList = new ArrayList<URL>();
        for (String element : classpathElements) {
            try {
                projectClasspathList.add(new File(element).toURI().toURL());
            } catch (MalformedURLException e) {
                throw new MojoExecutionException(element + " is an invalid classpath element", e);
            }
        }

        return new URL[projectClasspathList.size()];
    }

    /**
     * Reports the occurrence of an error while testing
     * @param message the error message to report
     * @throws SpelValidationException if the amount of errors exceeds the specified maximum
     */
    private void reportError(String message) throws SpelValidationException {
        getLog().error(message);
        ++errorCount;
        if (errorCount >= maxValidationErrors) {
            throw new SpelValidationException("Reached Maximum Amount of Errors");
        }
    }

    @Generated("generated by IDE")
    public MavenProject getProject() {
        return project;
    }

    @Generated("generated by IDE")
    public void setProject(MavenProject project) {
        this.project = project;
    }

    @Generated("generated by IDE")
    public List<SpelAnnotation> getAnnotations() {
        return annotations;
    }

    @Generated("generated by IDE")
    public void setAnnotations(List<SpelAnnotation> annotations) {
        this.annotations = annotations;
    }

    @Generated("generated by IDE")
     public SpelPlugin setMaxValidationErrors(int maxValidationErrors) {
        this.maxValidationErrors = maxValidationErrors;
        return this;
    }
}
