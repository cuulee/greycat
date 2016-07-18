package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.task.Task;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

class ActionTrigger implements TaskAction {

    private final Task _subTask;

    ActionTrigger(final Task p_subTask) {
        _subTask = p_subTask;
    }

    @Override
    public void eval(final TaskContext context) {
        final TaskResult previous = context.result();
        _subTask.executeFrom(context, previous, new Callback<TaskResult>() {
            @Override
            public void on(TaskResult subTaskResult) {
                if (previous != null) {
                    previous.free();
                }
                context.continueWith(subTaskResult);
            }
        });
    }

    @Override
    public String toString() {
        return "trigger()";
    }

}
