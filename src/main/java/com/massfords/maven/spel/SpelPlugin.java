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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.expression.ParseException;

import javax.annotation.Generated;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Goal for scanning class files looking for annotations with Spring Expression
 * Language expressions and parsing them during the build to report any errors.
 *
 * This is preferrable to finding these errors at runtime.
 */
@Mojo(name = "spel", defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES,
        requiresDependencyResolution = ResolutionScope.TEST)
public class SpelPlugin extends AbstractMojo {

    /**
     * Injected by maven to give us a reference to the project so we can get the
     * output directory and other properties.
     */
    @Component
    private MavenProject project;

    /**
     * The number of errors we've encountered
     */
    private int errorCount = 0;

    /**
     * The maximum number of errors we will tolerate in a module before failing
     * the build. This is similar to a compiler's threshold. A single error will
     * fail the build, but we'll continue processing the annotations until we
     * hit this cap.
     */
    @Parameter(property = "maxValidationErrors", required = false, defaultValue = "100")
    private int maxValidationErrors;

    /**
     * List of annotations that we'll scan for.
     */
    @Parameter(property = "annotations", required = false)
    private List<SpelAnnotation> annotations;

    /**
     * Injected value of our classpath elements. This is used in conjunction with
     * the Reflections API in order to help identify where annotations appear on
     * methods.
     */
    @Parameter( defaultValue = "${project.compileClasspathElements}", readonly = true, required = true )
    private List<String> projectClasspathElements;

    /**
     * Used to validation expressions
     */
    private ExpressionValidator validator = new ExpressionValidator();

    /**
     * Scans the source code for this module to look for instances of the
     * annotations we're looking for.
     *
     * @throws MojoExecutionException
     */
    public void execute() throws MojoExecutionException {

        // There's nothing to do if there are no annotations configured.
        if (annotations==null || annotations.isEmpty()) {
            getLog().warn("There are no annotations configured so there's nothing for this plugin to do.");
            return;
        }

        int processedCount = 0;

        try {
            // these paths should include our module's source root plus any generated code
            List<String> compileSourceOutputs = Collections.singletonList(project.getBuild().getOutputDirectory());
            URL[] sourceFiles = buildMavenClasspath(compileSourceOutputs);

            // the project classpath includes the source roots plus the transitive
            // dependencies according to our Mojo annotation's requiresDependencyResolution
            // attribute.
            URL[] projectClasspath = buildMavenClasspath(this.projectClasspathElements);

            URLClassLoader projectClassloader = new URLClassLoader(projectClasspath);
            // todo - is there any savings to be had here by caching the reflections but tweaking the filters for the given module?
            Reflections reflections = new Reflections(
                    new ConfigurationBuilder()
                            .setUrls(sourceFiles)
                            .addClassLoaders(projectClassloader)
                    .setScanners(new MethodAnnotationsScanner())
                    // todo here is where to filter the packages
//                    .filterInputsBy(new Predicate<String>() {
//                        @Override
//                        public boolean apply(String input) {
//                            return true;
//                        }
//                    })
            );

            // using the newly created classloaders and reflections API, scan the
            // classpath looking for instances of the annotations
            processedCount += processAnnotations(projectClassloader, reflections);
        } catch (Throwable e) {
            throw new MojoExecutionException("A fatal error occurred while validating Spel annotations," +
                    " see stack trace for details.", e);
        }
        getLog().info(String.format("Processed %d annotations", processedCount));
        if (errorCount > 0) {
            throw new MojoExecutionException("Spel validation failed on one or more annotations. See the specific" +
            "error output for more information.");
        }
    }

    /**
     * Walks all of the annotations and looks for instances of the annotation
     * on various methods using the Reflections API.
     *
     * @param projectClassloader
     * @param reflections
     * @return
     * @throws Exception
     */
    private int processAnnotations(URLClassLoader projectClassloader, Reflections reflections) throws Exception {
        int processedCount = 0;
        for(SpelAnnotation sa : annotations) {

            initAnnotationType(projectClassloader, sa);

            // if we don't know about the class for the annotation then it's
            // likely that the module was configured to scan for an annotation
            // that's not on its classpath. This could be a parent module or
            // a misconfiguration. Either way, we would have logged it above
            // and hopefully at some point we'll encounter a module that knows
            // about the annotation we're trying to validate.
            if (sa.getClazz() != null) {
                processedCount += validateAllAnnotationExpressions(reflections, sa);
            }
        }
        return processedCount;
    }

    /**
     * Scans the classpath looking for instances of the annotation on methods
     * to validate the expressions on the annotation if present.
     *
     * @param reflections
     * @param sa
     * @return
     * @throws Exception
     */
    private int validateAllAnnotationExpressions(Reflections reflections, SpelAnnotation sa) throws Exception {
        int processedCount = 0;
        Class<? extends Annotation> annoType = sa.getClazz();
        Set<Method> set = reflections.getMethodsAnnotatedWith(annoType);
        for(Method m : set) {
            Annotation anno = m.getAnnotation(annoType);

            Method attrGetter = annoType.getDeclaredMethod(sa.getAttribute());

            Object expressionObj = attrGetter.invoke(anno);
            if (expressionObj instanceof String) {
                getLog().info(String.format("Validating expression: %s", expressionObj));
                String expression = (String) attrGetter.invoke(anno);
                try {
                    processedCount++;
                    validator.validate(expression);
                } catch(ParseException e) {
                    String message = "Spel annotation %s.%s with expression %s failed to parse. Error message: %s";
                    reportError(String.format(message, sa.getName(), sa.getAttribute(), expression, e.getMessage()));
                } catch(ExpressionValidationException e) {
                    String message = "Spel annotation %s.%s with expression %s has validation errors. Validation error message: %s";
                    reportError(String.format(message, sa.getName(), sa.getAttribute(), expression, e.getMessage()));
                }
            } else if (expressionObj != null) {
                String message = "Spel annotation %s.%s is not configured with a string.";
                reportError(String.format(message, sa.getName(), sa.getAttribute()));
            }
        }
        return processedCount;
    }

    /**
     * Initializes the class for the given SpelAnnotation if it can be loaded
     * from the current classpath. This method handles the possibility that the
     * annotation cannot be loaded in case the user configured the plugin at
     * a parent module or similar in the idea that child modules would inherit
     * the configuration.
     *
     * @param projectClassloader
     * @param sa
     */
    private void initAnnotationType(URLClassLoader projectClassloader, SpelAnnotation sa) {
        if (sa.getClazz() == null) {
            try {
                //noinspection unchecked
                Class<? extends Annotation> clazz = (Class<? extends Annotation>) projectClassloader.loadClass(sa.getName());
                sa.setClazz(clazz);
                getLog().info(String.format("Loaded annotation %s", sa.getName()));
            } catch (Exception e) {
                getLog().warn("Could not find and instantiate class for annotation with name: " + sa.getName());
            }
        }
    }

    /**
     * Extracted this method simply for unit testing.
     * @param classpathElements
     * @return
     * @throws MojoExecutionException
     */
    protected URL[] buildMavenClasspath(List<String> classpathElements) throws MojoExecutionException {
        List<URL> projectClasspathList = new ArrayList<>();
        for (String element : classpathElements) {
            try {
                projectClasspathList.add(new File(element).toURI().toURL());
            } catch (MalformedURLException e) {
                throw new MojoExecutionException(element + " is an invalid classpath element", e);
            }
        }

        return projectClasspathList.toArray(new URL[projectClasspathList.size()]);
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

    @Generated("generated by IDE")
    public List<String> getProjectClasspathElements() {
        return projectClasspathElements;
    }

    @Generated("generated by IDE")
    public void setProjectClasspathElements(List<String> projectClasspathElements) {
        this.projectClasspathElements = projectClasspathElements;
    }
}
