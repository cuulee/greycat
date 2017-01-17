package org.mwg.internal.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.task.*;

import static org.mwg.internal.task.CoreActions.*;
import static org.mwg.internal.task.CoreActions.newTask;

public class ActionIfThenTest extends AbstractActionTest {

    @Test
    public void test() {
        initGraph();
        final boolean[] result = {false, false};

        Task modifyResult0 = newTask().thenDo(new ActionFunction() {
            @Override
            public void eval(TaskContext ctx) {
                result[0] = true;
            }
        });

        Task modifyResult1 = newTask().thenDo(new ActionFunction() {
            @Override
            public void eval(TaskContext ctx) {
                result[0] = true;
            }
        });

        newTask().ifThen(new ConditionalFunction() {
            @Override
            public boolean eval(TaskContext ctx) {
                return true;
            }
        }, modifyResult0).execute(graph, null);

        newTask().ifThen(new ConditionalFunction() {
            @Override
            public boolean eval(TaskContext ctx) {
                return false;
            }
        }, modifyResult0).execute(graph, null);

        Assert.assertEquals(true, result[0]);
        Assert.assertEquals(false, result[1]);
        removeGraph();
    }

    @Test
    public void testChainAfterIfThen() {
        initGraph();
        Task addVarInContext = newTask().then(inject(5)).then(defineAsGlobalVar("variable")).thenDo(new ActionFunction() {
            @Override
            public void eval(TaskContext ctx) {
                ctx.continueTask();
                //empty action
            }
        });

        newTask().ifThen(context -> true, addVarInContext).then(readVar("variable"))
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        Integer val = (Integer) ctx.result().get(0);
                        Assert.assertEquals(5, (int) val);
                    }
                }).execute(graph, null);
        removeGraph();
    }

    @Test
    public void testScriptIf() {
        initGraph();
        newTask().inject("hello").defineAsVar("name").clearResult().ifThenScript("ctx.variable('name').get(0) == 'hello'", newTask().inject("success")).execute(graph, result -> {
            Assert.assertEquals("success", result.get(0));
        });
        newTask().inject("hello2").defineAsVar("name").clearResult().ifThenScript("ctx.variable('name').get(0) == 'hello'", newTask().inject("false")).execute(graph, result -> {
            Assert.assertEquals(0,result.size());
        });
        removeGraph();

    }

    @Test
    public void accessContextVariableInThenTask() {
        initGraph();
        Task accessVar = newTask().thenDo(new ActionFunction() {
            @Override
            public void eval(TaskContext ctx) {
                Integer variable = (Integer) ctx.variable("variable").get(0);
                Assert.assertEquals(5, (int) variable);
                ctx.continueTask();
            }
        });

        newTask().then(inject(5)).then(defineAsGlobalVar("variable")).ifThen(new ConditionalFunction() {
            @Override
            public boolean eval(TaskContext ctx) {
                return true;
            }
        }, accessVar).execute(graph, null);
        removeGraph();
    }
}