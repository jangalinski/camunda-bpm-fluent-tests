/*
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
package org.camunda.bpm.engine.fluent;

import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 * @author Rafael Cordones <rafael.cordones@plexiti.com>
 */
public class FluentProcessInstanceImpl extends AbstractFluentDelegate<ProcessInstance> implements FluentProcessInstance {

    private static Logger log = Logger.getLogger(FluentProcessInstanceImpl.class.getName());

    protected String processDefinitionKey;
    protected Map<String, Object> processVariables = new HashMap<String, Object>();
    protected FluentProcessInstance.Move move = new FluentProcessInstance.Move() { public void along() {} };

    public FluentProcessInstanceImpl(FluentProcessEngine engine, String processDefinitionKey) {
        super(engine, null);
        this.processDefinitionKey = processDefinitionKey;
        boolean processDefinitionKeyExists = !engine.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).list().isEmpty();
        if (!processDefinitionKeyExists)
            throw new IllegalArgumentException("Process Definition with processDefinitionKey '" + processDefinitionKey + "' is not deployed.");
    }

    @Override
    public String getProcessDefinitionId() {
        return delegate != null ? delegate.getProcessDefinitionId() : null;
    }

    @Override
    public String getBusinessKey() {
        return delegate.getBusinessKey();
    }

    @Override
    public boolean isSuspended() {
        return delegate.isSuspended();
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public boolean isEnded() {
        return delegate != null && engine.getRuntimeService().createExecutionQuery().processInstanceId(delegate.getId()).list().isEmpty() || delegate.isEnded();
    }

    @Override
    public String getProcessInstanceId() {
        return delegate.getProcessInstanceId();
    }

    @Override
    public ProcessInstance getDelegate() {
        if (delegate == null)
            throw new IllegalStateException("Process instance not yet started. You cannot access that method before starting the processInstance instance.");
        return delegate;
    }

    @Override
    public FluentTask task() {
        if (delegate != null) {
            List<Task> tasks = engine.getTaskService().createTaskQuery().processInstanceId(getProcessInstanceId()).list();
            if (tasks.size() > 1)
                throw new IllegalStateException
                        ("By calling task() you implicitly assumed that maximum one user task is currently waiting to be completed in the context " +
                                "of this process instance. But instead " + tasks.size() + " tasks are currently waiting to be completed.");
            if (tasks.size() == 1)
                return new FluentTaskImpl(engine, tasks.get(0));
        }
        return null;
    }
    

    @Override
    public FluentJob job() {
        if (delegate != null) {
            List<Job> jobs = engine.getManagementService().createJobQuery().processInstanceId(getProcessInstanceId()).list();
            if (jobs.size() > 1)
                throw new IllegalStateException
                    ("By calling job() you implicitly assumed that maximum one job is currently waiting to be executed in the context " +
                        "of this process instance. But instead " + jobs.size() + " jobs are currently waiting to be executed.");
            if (jobs.size() == 1) 
                return new FluentJobImpl(engine, jobs.get(0));
            }
        return null;
    }

    public void moveAlong(FluentProcessInstance.Move move) {
        this.move = move;
    }

    @Override
    public FluentProcessInstance startAndMove() {
        Throwable expected = null;
        try {
            move.along();
        } catch (Throwable t) {
            expected = t;
        }
        if (expected == null)
            throw new IllegalArgumentException("Process could not be moved to the given activityId.");
        return this;
    }

    @Override
    public FluentProcessInstance start() {
        this.delegate = engine.getRuntimeService().startProcessInstanceByKey(processDefinitionKey, processVariables);
        log.info("Started processInstance (definition key '" + processDefinitionKey + "', definition id: '" + delegate.getProcessDefinitionId() + "', instance id: '" + delegate.getId() + "').");
        return this;
    }

    @Override
    public FluentProcessInstance setVariable(String name, Object value) {
        if (delegate == null)
            this.processVariables.put(name, value);
        else
            engine.getRuntimeService().setVariable(getId(), name, value);
        return this;
    }

    @Override
    public FluentProcessVariable getVariable(String name) {
        Object value = delegate != null ? engine.getRuntimeService().getVariable(getId(), name) : processVariables.get(name);
        if (value == null)
            throw new IllegalArgumentException("Unable to find processVariable '" + name + "'");
        return new FluentProcessVariableImpl(name, value);
    }

}
