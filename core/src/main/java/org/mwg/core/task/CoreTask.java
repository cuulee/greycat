package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.Constants;
import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.core.CoreConstants;
import org.mwg.plugin.AbstractNode;
import org.mwg.plugin.Job;
import org.mwg.task.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Pattern;

public class CoreTask implements org.mwg.task.Task {

    private final Graph _graph;
    private TaskAction[] _actions = new TaskAction[10];
    private int _actionCursor = 0;

    public CoreTask(final Graph p_graph) {
        this._graph = p_graph;
    }

    private void addAction(TaskAction task) {
        if (_actionCursor == _actions.length) {
            TaskAction[] temp_actions = new TaskAction[_actions.length * 2];
            System.arraycopy(_actions, 0, temp_actions, 0, _actions.length);
            _actions = temp_actions;
        }
        _actions[_actionCursor] = task;
        _actionCursor++;
    }

    @Override
    public final org.mwg.task.Task world(long world) {
        addAction(new ActionWorld(world));
        return this;
    }

    @Override
    public final org.mwg.task.Task time(long time) {
        addAction(new ActionTime(time));
        return this;
    }

    @Override
    public final org.mwg.task.Task fromIndex(String indexName, String query) {
        addAction(new ActionFromIndex(indexName, query));
        return this;
    }

    @Override
    public final org.mwg.task.Task fromIndexAll(String indexName) {
        addAction(new ActionFromIndexAll(indexName));
        return this;
    }

    @Override
    public final org.mwg.task.Task selectWith(String name, String pattern) {
        addAction(new ActionWith(name, Pattern.compile(pattern)));
        return this;
    }

    @Override
    public final org.mwg.task.Task selectWithout(String name, String pattern) {
        addAction(new ActionWithout(name, Pattern.compile(pattern)));
        return this;
    }

    @Override
    public final org.mwg.task.Task asVar(String variableName) {
        addAction(new ActionAsVar(variableName));
        return this;
    }

    @Override
    public final org.mwg.task.Task fromVar(String variableName) {
        addAction(new ActionFromVar(variableName));
        return this;
    }

    @Override
    public final org.mwg.task.Task select(TaskFunctionSelect filter) {
        addAction(new ActionSelect(filter));
        return this;
    }

    @Override
    public final org.mwg.task.Task selectWhere(org.mwg.task.Task subTask) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public final org.mwg.task.Task traverse(String relationName) {
        addAction(new ActionTraverse(relationName));
        return this;
    }

    @Override
    public Task traverseOrKeep(String relationName) {
        addAction(new ActionTraverseOrKeep(relationName));
        return this;
    }

    @Override
    public final org.mwg.task.Task traverseIndex(String indexName, String query) {
        addAction(new ActionTraverseIndex(indexName, query));
        return this;
    }

    @Override
    public final org.mwg.task.Task traverseIndexAll(String indexName) {
        addAction(new ActionTraverseIndex(indexName, null));
        return this;
    }

    @Override
    public final org.mwg.task.Task map(TaskFunctionMap mapFunction) {
        addAction(new ActionMap(mapFunction));
        return this;
    }

    @Override
    public final org.mwg.task.Task flatMap(TaskFunctionFlatMap flatMapFunction) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public final org.mwg.task.Task group(TaskFunctionGroup groupFunction) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public final org.mwg.task.Task groupWhere(org.mwg.task.Task groupSubTask) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public final org.mwg.task.Task from(Object inputValue) {
        addAction(new ActionFrom(protect(inputValue)));
        return this;
    }

    @Override
    public final org.mwg.task.Task wait(org.mwg.task.Task subTask) {
        addAction(new ActionTrigger(subTask));
        return this;
    }

    @Override
    public final org.mwg.task.Task ifThen(TaskFunctionConditional cond, org.mwg.task.Task then) {
        addAction(new ActionIfThen(cond, then));
        return this;
    }

    @Override
    public final org.mwg.task.Task whileDo(TaskFunctionConditional cond, org.mwg.task.Task then) {
        //addAction(new ActionWhileDo(cond, then));
        //return this;
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public final org.mwg.task.Task then(Action p_action) {
        addAction(new ActionWrapper(p_action, true));
        return this;
    }

    @Override
    public final org.mwg.task.Task thenAsync(Action p_action) {
        addAction(new ActionWrapper(p_action, false));
        return this;
    }

    @Override
    public final <T> org.mwg.task.Task foreachThen(Callback<T> action) {
        org.mwg.task.Task task = _graph.newTask().then(new Action() {
            @Override
            public void eval(org.mwg.task.TaskContext context) {
                Object previousResult = context.getPreviousResult();
                if (previousResult != null) {
                    action.on((T) previousResult);
                }
            }
        });
        foreach(task);
        return this;
    }

    @Override
    public final org.mwg.task.Task foreach(org.mwg.task.Task subTask) {
        addAction(new ActionForeach(subTask));
        return this;
    }

    @Override
    public final org.mwg.task.Task foreachPar(org.mwg.task.Task subTask) {
        addAction(new ActionForeachPar(subTask));
        return this;
    }

    @Override
    public Task save() {
        addAction(new ActionSave());
        return this;
    }

    @Override
    public final void execute() {
        executeThenAsync(null, null, null);
    }

    @Override
    public final void executeThen(Action p_action) {
        executeThenAsync(null, null, new Action() {
            @Override
            public void eval(TaskContext context) {
                p_action.eval(new TaskContextWrapper(context));
                context.next();
            }
        });
    }

    @Override
    public final void executeThenAsync(final org.mwg.task.TaskContext parent, final Object initialResult, final Action p_finalAction) {
        final TaskAction[] final_actions = new TaskAction[_actionCursor + 2];
        System.arraycopy(_actions, 0, final_actions, 0, _actionCursor);
        if (p_finalAction != null) {
            final_actions[_actionCursor] = new ActionWrapper(p_finalAction, false);
        } else {
            final_actions[_actionCursor] = new ActionNoop();
        }
        final_actions[_actionCursor + 1] = new TaskAction() {
            @Override
            public void eval(org.mwg.task.TaskContext context) {
                context.clean();
            }
        };
        final org.mwg.task.TaskContext context = new CoreTaskContext(parent, protect(initialResult), _graph, final_actions);
        if (parent != null) {
            context.setWorld(parent.getWorld());
            context.setTime(parent.getTime());
        }
        _graph.scheduler().dispatch(new Job() {
            @Override
            public void run() {
                TaskAction first = final_actions[0];
                first.eval(context);
            }
        });
    }


    @Override
    public Task action(String name, String flatParams) {
        TaskActionFactory actionFactory = _graph.actions().get(name);
        if (actionFactory == null) {
            throw new RuntimeException("Unknown task action: " + name);
        }
        int paramsCapacity = CoreConstants.MAP_INITIAL_CAPACITY;
        String[] params = new String[paramsCapacity];
        int paramsIndex = 0;
        int cursor = 0;
        int flatSize = flatParams.length();
        int previous = 0;
        while (cursor < flatSize) {
            char current = flatParams.charAt(cursor);
            if (current == Constants.QUERY_SEP) {
                String param = flatParams.substring(previous, cursor);
                if (param.length() > 0) {
                    if (paramsIndex >= paramsCapacity) {
                        int newParamsCapacity = paramsCapacity * 2;
                        String[] newParams = new String[newParamsCapacity];
                        System.arraycopy(params, 0, newParams, 0, paramsCapacity);
                        params = newParams;
                        paramsCapacity = newParamsCapacity;
                    }
                    params[paramsIndex] = param;
                    paramsIndex++;
                }
                previous = cursor + 1;
            }
            cursor++;
        }
        //add last param
        String param = flatParams.substring(previous, cursor);
        if (param.length() > 0) {
            if (paramsIndex >= paramsCapacity) {
                int newParamsCapacity = paramsCapacity * 2;
                String[] newParams = new String[newParamsCapacity];
                System.arraycopy(params, 0, newParams, 0, paramsCapacity);
                params = newParams;
                paramsCapacity = newParamsCapacity;
            }
            params[paramsIndex] = param;
            paramsIndex++;
        }
        //schrink
        if (paramsIndex < params.length) {
            String[] shrinked = new String[paramsIndex];
            System.arraycopy(params, 0, shrinked, 0, paramsIndex);
            params = shrinked;
        }
        //add the action to the action
        addAction(actionFactory.create(params));
        return this;
    }

    @Override
    public Task parse(final String flat) {
        int cursor = 0;
        int flatSize = flat.length();
        int previous = 0;
        String actionName = null;
        while (cursor < flatSize) {
            char current = flat.charAt(cursor);
            switch (current) {
                case Constants.TASK_SEP:
                    actionName = null;
                    previous = cursor + 1;
                    break;
                case Constants.TASK_PARAM_OPEN:
                    actionName = flat.substring(previous, cursor);
                    previous = cursor + 1;
                    break;
                case Constants.TASK_PARAM_CLOSE:
                    //ADD LAST PARAM
                    action(actionName, flat.substring(previous, cursor));
                    actionName = null;
                    previous = cursor + 1;
                    //ADD TASK
                    break;
            }
            cursor++;
        }
        return this;
    }

    private Object protect(Object input) {
        if (input instanceof AbstractNode) {
            return _graph.cloneNode((Node) input);
        } else if (input instanceof Object[]) {
            Object[] casted = (Object[]) input;
            Object[] cloned = new Object[casted.length];
            boolean isAllNode = true;
            for (int i = 0; i < casted.length; i++) {
                cloned[i] = protect(casted[i]);
                isAllNode = isAllNode && (cloned[i] instanceof AbstractNode);
            }
            if (isAllNode) {
                Node[] typedResult = new Node[cloned.length];
                System.arraycopy(cloned, 0, typedResult, 0, cloned.length);
                return typedResult;
            }
            return cloned;
        } else {
            return protectIterable(input);
        }
    }

    /**
     * @native ts
     * if(input != null && input != undefined && input['iterator'] != undefined){
     * var flat = [];
     * var it = input['iterator']();
     * while(it.hasNext()){
     * flat.push(it.next());
     * }
     * return flat;
     * } else {
     * return input;
     * }
     */
    private Object protectIterable(Object input) {
        if (input instanceof Collection) {
            Collection casted = (Collection) input;
            Object[] flat = new Object[casted.size()];
            int flat_index = 0;
            Iterator it = casted.iterator();
            while (it.hasNext()) {
                flat[flat_index] = it.next();
                flat_index++;
            }
            return flat;
        }

        return input;
    }

}
