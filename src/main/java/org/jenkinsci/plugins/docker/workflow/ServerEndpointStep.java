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
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Job;
import org.jenkinsci.plugins.docker.commons.credentials.DockerServerEndpoint;
import org.jenkinsci.plugins.docker.commons.credentials.KeyMaterialFactory;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Set;

public class ServerEndpointStep extends Step {
    
    private final @Nonnull DockerServerEndpoint server;

    @DataBoundConstructor public ServerEndpointStep(@Nonnull DockerServerEndpoint server) {
        assert server != null;
        this.server = server;
    }
    
    public DockerServerEndpoint getServer() {
        return server;
    }

    @Override
    public StepExecution start(StepContext stepContext) throws Exception {
        return new Execution(stepContext, this);
    }

    public static class Execution extends AbstractEndpointStepExecution {

        private static final long serialVersionUID = 1;

        private transient ServerEndpointStep step;

        public Execution(StepContext context, ServerEndpointStep step) {
            super(context);
            this.step = step;
        }

        @Override protected KeyMaterialFactory newKeyMaterialFactory() throws IOException, InterruptedException {
            return step.server.newKeyMaterialFactory(
                getContext().get(Job.class),
                getContext().get(FilePath.class).getChannel());
        }

    }

    @Extension public static class DescriptorImpl extends StepDescriptor {

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(FilePath.class, Job.class);
        }

        @Override public String getFunctionName() {
            return "withDockerServer";
        }

        @Override public String getDisplayName() {
            return "Sets up Docker server endpoint";
        }

        @Override public boolean takesImplicitBlockArgument() {
            return true;
        }

        @Override public boolean isAdvanced() {
            return true;
        }

    }

}
