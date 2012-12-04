package com.plexiti.activiti.test.fluent.assertions;

import com.plexiti.activiti.test.fluent.ActivitiFluentTestHelper;
import org.activiti.engine.runtime.Execution;
import org.fest.assertions.api.AbstractAssert;
import org.fest.assertions.api.Assertions;

import java.util.List;

/**
 *
 * @author Martin Schimak <martin.schimak@plexiti.com>
 * @author Rafael Cordones <rafael.cordones@plexiti.com>
 *
 */
public class ExecutionAssert extends AbstractAssert<ExecutionAssert, Execution> {

    protected ExecutionAssert(Execution actual) {
        super(actual, ExecutionAssert.class);
    }

    public static ExecutionAssert assertThat(Execution actual) {
        return new ExecutionAssert(actual);
    }

    // TODO: add isNotFinished()

    public ExecutionAssert isFinished() {
        /*
         * TODO: we need to review this
         * If the incomming Execution instance is null we consider the execution finished
         */
        if (actual == null) {
            return this;
        }

        /*
         * If it is not null we make sure that it is actually finished.
         */
        Assertions.assertThat(actual.isEnded()).
                overridingErrorMessage("Expected execution %s to be finished but it is not!", actual.getId()).
                isTrue();

        return this;
    }

    public ExecutionAssert isStarted() {
        isNotNull();

        Assertions.assertThat(actual.isEnded()).
                overridingErrorMessage("Expected execution %s to be started but it is not!", actual.getId()).
                isFalse();

        return this;
    }

    public ExecutionAssert isWaitingAt(String activityId) {
        isNotNull();

        List<String> activeActivityIds = ActivitiFluentTestHelper.getRuntimeService()
                                                                 .getActiveActivityIds(actual.getId());
        Assertions.assertThat(activeActivityIds)
                .overridingErrorMessage("Expected execution with id '%s' to be waiting at activity with id '%s' but it actually waiting at: %s",
                                        actual.getId(), activityId, activeActivityIds)
                .contains(activityId);

        return this;
    }

}