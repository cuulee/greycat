package org.mwg.core.task;

import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

class ActionFromVar implements TaskAction {

    private final String _name;
    private final int _index;


    public ActionFromVar(String p_name, int p_index) {
        this._name = p_name;
        this._index = p_index;
    }

    @Override
    public void eval(final TaskContext context) {
        final String evaluatedName = context.template(_name);
        final TaskResult varResult;
        if(_index != -1) {
            varResult = context.wrap(context.variable(evaluatedName).get(_index));
        } else {
            varResult = context.variable(evaluatedName);
        }
        context.continueWith(varResult.clone());
    }

    @Override
    public String toString() {
        return "fromVar(\'" + _name + "\')";
    }

}
