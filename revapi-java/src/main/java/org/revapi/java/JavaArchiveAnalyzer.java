/*
 * Copyright 2014 Lukas Krejci
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package org.revapi.java;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;

import org.revapi.Archive;
import org.revapi.ArchiveAnalyzer;
import org.revapi.java.compilation.CompilationFuture;
import org.revapi.java.compilation.CompilationValve;
import org.revapi.java.compilation.Compiler;
import org.revapi.java.compilation.ProbingEnvironment;
import org.revapi.java.model.JavaTree;

/**
 * @author Lukas Krejci
 * @since 0.1
 */
public final class JavaArchiveAnalyzer implements ArchiveAnalyzer {
    private final Iterable<? extends Archive> archives;
    private final Iterable<? extends Archive> additionalClassPath;
    private final ExecutorService executor;
    private final ProbingEnvironment probingEnvironment;
    private CompilationValve compilationValve;

    public JavaArchiveAnalyzer(Iterable<? extends Archive> archives,
        Iterable<? extends Archive> additionalClassPath, ExecutorService compilationExecutor) {
        this.archives = archives;
        this.additionalClassPath = additionalClassPath;
        this.executor = compilationExecutor;
        this.probingEnvironment = new ProbingEnvironment(archivesToString());
    }

    @Override
    public JavaTree analyze() {
        StringWriter output = new StringWriter();
        Compiler compiler = new Compiler(executor, output, archives, additionalClassPath);
        try {
            compilationValve = compiler.compile(probingEnvironment);

            probingEnvironment.getTree()
                .setCompilationFuture(new CompilationFuture(compilationValve, output));

            return probingEnvironment.getTree();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to analyze archives " + archivesToString(), e);
        }
    }

    public ProbingEnvironment getProbingEnvironment() {
        return probingEnvironment;
    }

    public CompilationValve getCompilationValve() {
        return compilationValve;
    }

    private String archivesToString() {
        Iterator<? extends Archive> it = archives.iterator();
        StringBuilder bld = new StringBuilder("{");

        if (it.hasNext()) {
            bld.append(it.next().getName());
        }

        while (it.hasNext()) {
            bld.append(", ").append(it.next().getName());
        }

        bld.append("}");

        return bld.toString();
    }

}
