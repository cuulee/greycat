package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.DeferCounter;
import org.mwg.Node;
import org.mwg.Type;
import org.mwg.plugin.AbstractNode;
import org.mwg.plugin.Job;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

class ActionTraverseOrKeep implements TaskAction {

    private final String _name;

    ActionTraverseOrKeep(final String p_name) {
        this._name = p_name;
    }

    @Override
    public final void eval(final TaskContext context) {
        final TaskResult finalResult = context.wrap(null);
        final String flatName = context.template(_name);
        final TaskResult previousResult = context.result();
        if (previousResult != null) {
            final int previousSize = previousResult.size();
            final DeferCounter defer = context.graph().newCounter(previousSize);
            for (int i = 0; i < previousSize; i++) {
                final Object loop = previousResult.get(i);
                if (loop instanceof AbstractNode) {
                    Node casted = (Node) loop;
                    if (casted.type(flatName) == Type.RELATION) {
                        casted.rel(flatName, new Callback<Node[]>() {
                            @Override
                            public void on(Node[] result) {
                                if (result != null) {
                                    for (int j = 0; j < result.length; j++) {
                                        finalResult.add(result[j]);
                                    }
                                }
                                defer.count();
                            }
                        });
                    } else {
                        finalResult.add(casted.graph().cloneNode(casted));
                        defer.count();
                    }
                } else {
                    //TODO add closable management
                    finalResult.add(loop);
                    defer.count();
                }
            }
            defer.then(new Job() {
                @Override
                public void run() {
                    context.continueWith(finalResult);
                }
            });
        } else {
            context.continueTask();
        }
    }

    @Override
    public String toString() {
        return "traverseOrKeep(\'" + _name + "\')";
    }

}