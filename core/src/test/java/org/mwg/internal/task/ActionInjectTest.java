package org.mwg.internal.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Node;
import org.mwg.task.ActionFunction;
import org.mwg.task.TaskContext;

import static org.mwg.internal.task.CoreActions.inject;
import static org.mwg.internal.task.CoreActions.newTask;

public class ActionInjectTest extends AbstractActionTest {

    @Test
    public void test() {
        initGraph();
        newTask()
                .then(inject("uselessPayload"))
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        Assert.assertEquals(ctx.resultAsStrings().get(0), "uselessPayload");
                    }
                })
                .execute(graph, null);
        removeGraph();
    }

    @Test
    public void testFromNodes() {
        initGraph();
        final ActionInjectTest selfPointer = this;
        graph.index(0, 0, "nodes", nodes -> {
            graph.lookupAll(0, 0, nodes.all(), result -> {
                Assert.assertEquals(3, result.length);

                String[] expected = new String[]{(String) result[0].get("name"),
                        (String) result[1].get("name"),
                        (String) result[2].get("name")};

                newTask()
                        .then(inject(result))
                        .thenDo(new ActionFunction() {
                            @Override
                            public void eval(TaskContext ctx) {
                                //empty task
                            }
                        })
                        .execute(selfPointer.graph, null);

                String[] resultName = new String[3];
                try {
                    int i = 0;
                    for (Node n : result) {
                        resultName[i] = (String) n.get("name");
                        i++;
                    }
                } catch (Exception e) {
                    resultName[0] = "fail";
                    e.printStackTrace();
                }

                Assert.assertArrayEquals(expected, resultName);
            });
        });

        removeGraph();
    }

    @Test
    public void testFromNode() {
        initGraph();
        final ActionInjectTest selfPointer = this;
        graph.index(0, 0, "roots", rootsIndex -> {
            rootsIndex.find(result -> {
                Assert.assertEquals(1, result.length);

                newTask()
                        .then(inject(result[0]))
                        .thenDo(new ActionFunction() {
                            @Override
                            public void eval(TaskContext ctx) {
                                //empty task
                            }
                        })
                        .execute(graph, null);
                String name;
                try {
                    name = (String) result[0].get("name");
                } catch (Exception e) {
                    name = "fail";
                }

                Assert.assertEquals("root", name);
            },"name","root");
        });

        removeGraph();
    }


}