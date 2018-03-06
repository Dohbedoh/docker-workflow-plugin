/*
 * The MIT License
 *
 * Copyright (c) 2015, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.docker.workflow;

import com.google.common.collect.ImmutableSet;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Node;
import hudson.model.Run;
import org.jenkinsci.plugins.docker.commons.fingerprint.DockerFingerprints;
import org.jenkinsci.plugins.docker.workflow.client.DockerClient;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.util.Set;

public class RunFingerprintStep extends Step {

    private final String containerId;
    private String toolName;

    @DataBoundConstructor public RunFingerprintStep(String containerId) {
        this.containerId = containerId;
    }

    public String getContainerId() {
        return containerId;
    }

    public String getToolName() {
        return toolName;
    }

    @DataBoundSetter public void setToolName(String toolName) {
        this.toolName = Util.fixEmpty(toolName);
    }

    @Override
    public StepExecution start(StepContext stepContext) throws Exception {
        return new Execution(stepContext, this);
    }

    public static class Execution extends SynchronousNonBlockingStepExecution<Void> {
        
        private static final long serialVersionUID = 1L;

        private transient RunFingerprintStep step;

        protected Execution(@Nonnull StepContext context, RunFingerprintStep step) {
            super(context);
            this.step = step;
        }

        @SuppressWarnings("SynchronizeOnNonFinalField") // run is quasi-final
        @Override protected Void run() throws Exception {
            DockerClient client = new DockerClient(getContext().get(Launcher.class), getContext().get(Node.class),
                step.toolName);
            EnvVars env = getContext().get(EnvVars.class);
            Run run = getContext().get(Run.class);
            DockerFingerprints.addRunFacet(client.getContainerRecord(env, step.containerId), run);
            String image = client.inspect(env, step.containerId, ".Config.Image");
            if (image != null) {
                ImageAction.add(image, run);
            }
            return null;
        }

    }

    @Extension public static class DescriptorImpl extends StepDescriptor {

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(
                EnvVars.class,
                Launcher.class,
                Node.class,
                Run.class);
        }

        @Override public String getFunctionName() {
            return "dockerFingerprintRun";
        }

        @Override public String getDisplayName() {
            return "Record trace of a Docker image run in a container";
        }

        @Override public boolean isAdvanced() {
            return true;
        }

    }

}
