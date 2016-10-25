package ml.ruleinference;

import org.junit.Test;
import org.mwg.*;
import org.mwg.core.scheduler.NoopScheduler;
import org.mwg.ml.algorithm.regression.PolynomialNode;
import org.mwg.mlx.MLXPlugin;
import org.mwg.mlx.algorithm.classifier.GaussianClassifierNode;
import org.mwg.mlx.algorithm.ruleinference.RuleNode;

import static org.junit.Assert.assertEquals;

/**
 * Created by andrey.boytsov on 24/10/2016.
 */
public class RuleNodeTest {

    public void checkRule(String condition, boolean expectedResult){
        final Graph graph = new GraphBuilder().withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                RuleNode ruleNode = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);

                ruleNode.set(RuleNode.INTERNAL_CONDITION_STRING, condition);

                assertEquals(expectedResult, ruleNode.ruleTriggered());

                ruleNode.free();
                graph.disconnect(null);
            }
        });
    }

    @Test
    public void testTrueTrigger() {
        checkRule("True", true);
    }

    @Test
    public void testFalseTrigger() {
        checkRule("False", false);
    }

    @Test
    public void testConstantTrueTrigger() {
        checkRule("1.1", true);
    }

    @Test
    public void testFalseConstantTrigger() {
        checkRule("-3", false);
    }

    @Test
    public void testZeroConstantFalseTrigger() {
        checkRule("0", false);
    }

    @Test
    public void testGreaterTrueTrigger() {
        checkRule("1 > 0", true);
    }

    @Test
    public void testGreaterFalseTrigger() {
        checkRule("0 > 1", false);
    }

    @Test
    public void testGreaterFalseEqualTrigger() {
        checkRule("1 > 1", false);
    }

    @Test
    public void testGreaterEqualsTrueTrigger() {
        checkRule("1 >= 0", true);
    }

    @Test
    public void testGreaterEqualsFalseTrigger() {
        checkRule("0 >= 1", false);
    }

    @Test
    public void testGreaterEqualsTrueEqualTrigger() {
        checkRule("1 >= 1", true);
    }

    @Test
    public void testLessTrueTrigger() {
        checkRule("1 < 2", true);
    }

    @Test
    public void testLessFalseTrigger() {
        checkRule("2 < 1", false);
    }

    @Test
    public void testLessFalseEqualTrigger() {
        checkRule("2 < 2", false);
    }

    @Test
    public void testLessEqualsTrueTrigger() {
        checkRule("1 <= 2", true);
    }

    @Test
    public void testLessEqualsFalseTrigger() {
        checkRule("2 <= 1", false);
    }

    @Test
    public void testLessEqualsTrueEqualTrigger() {
        checkRule("2 <= 2", true);
    }

    @Test
    public void testAndTTSimple(){
        checkRule("True && tRue", true);
    }

    @Test
    public void testAndTFSimple(){
        checkRule("true && false", false);
    }

    @Test
    public void testAndFTSimple(){
        checkRule("falSE && tRue", false);
    }

    @Test
    public void testAndFFSimple(){
        checkRule("False && false", false);
    }

    @Test
    public void testAndTTValues(){
        checkRule("1 && 2", true);
    }

    @Test
    public void testAndTFValues(){
        checkRule("1 && -1", false);
    }

    @Test
    public void testOrTTSimple(){
        checkRule("True || tRue", true);
    }

    @Test
    public void testOrTFSimple(){
        checkRule("true || false", true);
    }

    @Test
    public void testOrFTSimple(){
        checkRule("falSE || tRue", true);
    }

    @Test
    public void testOrFFSimple(){
        checkRule("False || false", false);
    }

    @Test
    public void testOrTTValues(){
        checkRule("1 || 2", true);
    }

    @Test
    public void testOrTFValues(){
        checkRule("-2 || -1", false);
    }

    @Test
    public void testNestedTTConjuncts(){
        checkRule("(2 < 3) && (1.5 >= 0.8)", true);
    }

    @Test
    public void testNestedTFConjuncts(){
        checkRule("(2 < 3) && (1.5 >= 1.8)", false);
    }

    @Test
    public void testMultipleTConjuncts(){
        checkRule("(2 < 3) && (1.5 >= 0.8) && true && (2 == 2)", true);
    }

    @Test
    public void testMultipleTConjunctsBracketsTrick(){
        //Should properly understand 2==2 without brackets
        checkRule("(2 < 3) && (1.5 >= 0.8) && true && 2 == 2", true);
    }

    @Test
    public void testEqualsTrue(){
        checkRule("1 == 1.0", true);
    }

    @Test
    public void testEqualsFalse(){
        checkRule("1 == 1.001", false);
    }

    @Test
    public void testNonEqualsFalse(){
        checkRule("1 != 1.0", false);
    }

    @Test
    public void testNonEqualsTrue(){
        checkRule("1 != 1.001", true);
    }

    @Test
    public void testBracketsTrue(){
        checkRule("( 1 != 1.001 )", true);
    }

    @Test
    public void testMultipleBracketsTrue() {
        checkRule("(( 1 != 1.001 ) ) ", true);
    }

    @Test
    public void testBracketsFalse(){
        checkRule("( 1 != 1.00 )", false);
    }

    @Test
    public void testMultipleBracketsFalse() {
        checkRule("(( 1 != 1.00 ) ) ", false);
    }

    @Test
    public void testNotTrue(){
        checkRule("! true", false);
    }

    @Test
    public void testNotFalse(){
        checkRule("!false", true);
    }

    @Test
    public void testNegationEqualsTrue(){
        checkRule("! (1 == 1.0)", false);
    }

    @Test
    public void testNegationEqualsFalse(){
        checkRule("!(1 == 1.001)", true);
    }

    @Test
    public void checkDoubleValueRetrieval(){
        final Graph graph = new GraphBuilder().withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                RuleNode ruleNode1 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode2 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode3 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode4 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);

                GaussianClassifierNode justNodeForValue = (GaussianClassifierNode)
                        graph.newTypedNode(0, 0, GaussianClassifierNode.NAME);

                justNodeForValue.setProperty(GaussianClassifierNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 1.2345);

                String requestStr = justNodeForValue.id()+"."+GaussianClassifierNode.HIGH_ERROR_THRESH_KEY;

                ruleNode1.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} > 1.23");
                ruleNode2.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} < 1.23");
                ruleNode3.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} == 1.2345");
                ruleNode4.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} != 1.2345");

                assertEquals(true, ruleNode1.ruleTriggered());
                assertEquals(false, ruleNode2.ruleTriggered());
                assertEquals(true, ruleNode3.ruleTriggered());
                assertEquals(false, ruleNode4.ruleTriggered());

                ruleNode1.free();
                ruleNode2.free();
                ruleNode3.free();
                ruleNode4.free();

                justNodeForValue.free();

                graph.disconnect(null);
            }
        });
    }

    @Test
    public void checkBooleanTrueValueRetrieval(){
        final Graph graph = new GraphBuilder().withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                RuleNode ruleNode1 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode2 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode3 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode4 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode5 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode6 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);

                GaussianClassifierNode justNodeForValue = (GaussianClassifierNode)
                        graph.newTypedNode(0, 0, GaussianClassifierNode.NAME);

                String requestStr = justNodeForValue.id()+".someValue";

                justNodeForValue.setProperty("someValue", Type.BOOL, true);

                //Bootstrap mode is on by default
                ruleNode1.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"}");
                ruleNode2.set(RuleNode.INTERNAL_CONDITION_STRING, "!{"+requestStr+"}");
                ruleNode3.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} == True");
                ruleNode4.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} == false");
                ruleNode5.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} != trUe");
                ruleNode6.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} != false");

                assertEquals(true, ruleNode1.ruleTriggered());
                assertEquals(false, ruleNode2.ruleTriggered());
                assertEquals(true, ruleNode3.ruleTriggered());
                assertEquals(false, ruleNode4.ruleTriggered());
                assertEquals(false, ruleNode5.ruleTriggered());
                assertEquals(true, ruleNode6.ruleTriggered());

                ruleNode1.free();
                ruleNode2.free();
                ruleNode3.free();
                ruleNode4.free();
                ruleNode5.free();
                ruleNode6.free();

                justNodeForValue.free();

                graph.disconnect(null);
            }
        });
    }

    @Test
    public void checkBooleanFalseValueRetrieval(){
        final Graph graph = new GraphBuilder().withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                RuleNode ruleNode1 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode2 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode3 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode4 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode5 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode6 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);

                GaussianClassifierNode justNodeForValue = (GaussianClassifierNode)
                        graph.newTypedNode(0, 0, GaussianClassifierNode.NAME);

                String requestStr = justNodeForValue.id()+".someValue";

                justNodeForValue.setProperty("someValue", Type.BOOL, false);

                //Bootstrap mode is on by default
                ruleNode1.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"}");
                ruleNode2.set(RuleNode.INTERNAL_CONDITION_STRING, "!{"+requestStr+"}");
                ruleNode3.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} == True");
                ruleNode4.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} == false");
                ruleNode5.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} != trUe");
                ruleNode6.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} != false");

                assertEquals(false, ruleNode1.ruleTriggered());
                assertEquals(true, ruleNode2.ruleTriggered());
                assertEquals(false, ruleNode3.ruleTriggered());
                assertEquals(true, ruleNode4.ruleTriggered());
                assertEquals(true, ruleNode5.ruleTriggered());
                assertEquals(false, ruleNode6.ruleTriggered());

                ruleNode1.free();
                ruleNode2.free();
                ruleNode3.free();
                ruleNode4.free();
                ruleNode5.free();
                ruleNode6.free();

                justNodeForValue.free();

                graph.disconnect(null);
            }
        });
    }

    @Test
    public void checkDoubleChangedValueRetrieval(){
        final Graph graph = new GraphBuilder().withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                RuleNode ruleNode1 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode2 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode3 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode4 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode5 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);

                GaussianClassifierNode justNodeForValue = (GaussianClassifierNode)
                        graph.newTypedNode(0, 0, GaussianClassifierNode.NAME);

                justNodeForValue.setProperty(GaussianClassifierNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 1.2345);

                justNodeForValue.jump(10, new Callback<Node>() {
                    @Override
                    public void on(Node result) {
                        result.setProperty(GaussianClassifierNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 0.11);
                    }
                });

                String requestStr = justNodeForValue.id()+"."+GaussianClassifierNode.HIGH_ERROR_THRESH_KEY;

                ruleNode1.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} > 1.23");
                ruleNode2.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} < 1.23");
                ruleNode3.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} == 1.2345");
                ruleNode4.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} != 1.2345");
                ruleNode5.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} == 0.11");

                assertEquals(false, ruleNode1.ruleTriggered());
                assertEquals(true, ruleNode2.ruleTriggered());
                assertEquals(false, ruleNode3.ruleTriggered());
                assertEquals(true, ruleNode4.ruleTriggered());
                assertEquals(true, ruleNode5.ruleTriggered());

                ruleNode1.free();
                ruleNode2.free();
                ruleNode3.free();
                ruleNode4.free();
                ruleNode5.free();

                justNodeForValue.free();

                graph.disconnect(null);
            }
        });
    }

    @Test
    public void checkBooleanChangedToFalseValueRetrieval(){
        final Graph graph = new GraphBuilder().withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                RuleNode ruleNode1 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode2 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode3 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode4 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode5 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode6 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);

                GaussianClassifierNode justNodeForValue = (GaussianClassifierNode)
                        graph.newTypedNode(0, 0, GaussianClassifierNode.NAME);

                String requestStr = justNodeForValue.id()+".someValue";

                justNodeForValue.setProperty("someValue", Type.BOOL, true);

                justNodeForValue.jump(10, new Callback<Node>() {
                    @Override
                    public void on(Node result) {
                        result.setProperty("someValue", Type.BOOL, false);
                    }
                });

                //Bootstrap mode is on by default
                ruleNode1.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"}");
                ruleNode2.set(RuleNode.INTERNAL_CONDITION_STRING, "!{"+requestStr+"}");
                ruleNode3.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} == True");
                ruleNode4.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} == false");
                ruleNode5.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} != trUe");
                ruleNode6.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} != false");

                assertEquals(false, ruleNode1.ruleTriggered());
                assertEquals(true, ruleNode2.ruleTriggered());
                assertEquals(false, ruleNode3.ruleTriggered());
                assertEquals(true, ruleNode4.ruleTriggered());
                assertEquals(true, ruleNode5.ruleTriggered());
                assertEquals(false, ruleNode6.ruleTriggered());

                ruleNode1.free();
                ruleNode2.free();
                ruleNode3.free();
                ruleNode4.free();
                ruleNode5.free();
                ruleNode6.free();

                justNodeForValue.free();

                graph.disconnect(null);
            }
        });
    }

    @Test
    public void checkDoubleDerivativeRetrieval(){
        final Graph graph = new GraphBuilder().withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                RuleNode ruleNode1 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode2 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode3 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);

                GaussianClassifierNode justNodeForValue = (GaussianClassifierNode)
                        graph.newTypedNode(0, 0, GaussianClassifierNode.NAME);

                justNodeForValue.setProperty(GaussianClassifierNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 5.0);

                justNodeForValue.jump(2000, new Callback<Node>() {
                    @Override
                    public void on(Node result) {
                        result.setProperty(GaussianClassifierNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 1.0);
                    }
                });

                //From 5 to 1 for 2 seconds
                //Derivative = -2
                String requestStr = justNodeForValue.id()+"."+GaussianClassifierNode.HIGH_ERROR_THRESH_KEY;

                ruleNode1.set(RuleNode.INTERNAL_CONDITION_STRING, "d{"+requestStr+"} < 0");
                ruleNode2.set(RuleNode.INTERNAL_CONDITION_STRING, "d{"+requestStr+"} >= 0");
                ruleNode3.set(RuleNode.INTERNAL_CONDITION_STRING, "d{"+requestStr+"} == -0.002");

                assertEquals(true, ruleNode1.ruleTriggered());
                assertEquals(false, ruleNode2.ruleTriggered());
                assertEquals(true, ruleNode3.ruleTriggered());

                ruleNode1.free();
                ruleNode2.free();
                ruleNode3.free();

                justNodeForValue.free();

                graph.disconnect(null);
            }
        });
    }

    @Test
    public void checkDoubleDerivativeNeverChange(){
        final Graph graph = new GraphBuilder().withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                RuleNode ruleNode1 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode2 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode3 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);

                GaussianClassifierNode justNodeForValue = (GaussianClassifierNode)
                        graph.newTypedNode(0, 0, GaussianClassifierNode.NAME);

                justNodeForValue.setProperty(GaussianClassifierNode.HIGH_ERROR_THRESH_KEY, Type.DOUBLE, 5.0);

                //From 5 to 1 for 2 seconds
                //Derivative = -2
                String requestStr = justNodeForValue.id()+"."+GaussianClassifierNode.HIGH_ERROR_THRESH_KEY;

                ruleNode1.set(RuleNode.INTERNAL_CONDITION_STRING, "d{"+requestStr+"} < 0");
                ruleNode2.set(RuleNode.INTERNAL_CONDITION_STRING, "d{"+requestStr+"} > 0");
                ruleNode3.set(RuleNode.INTERNAL_CONDITION_STRING, "d{"+requestStr+"} == 0");

                assertEquals(false, ruleNode1.ruleTriggered());
                assertEquals(false, ruleNode2.ruleTriggered());
                assertEquals(true, ruleNode3.ruleTriggered());

                ruleNode1.free();
                ruleNode2.free();
                ruleNode3.free();

                justNodeForValue.free();

                graph.disconnect(null);
            }
        });
    }

    @Test
    public void checkDoubleStringComparison(){
        final Graph graph = new GraphBuilder().withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                RuleNode ruleNode1 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode2 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode3 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode4 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode5 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode6 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);

                GaussianClassifierNode justNodeForValue = (GaussianClassifierNode)
                        graph.newTypedNode(0, 0, GaussianClassifierNode.NAME);

                justNodeForValue.setProperty("someValue", Type.STRING, "1.2345");

                String requestStr = justNodeForValue.id()+".someValue";

                ruleNode1.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} == '1.2345'");
                ruleNode2.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} != 'abcd'");
                ruleNode3.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} == 1.2345");
                ruleNode4.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} == 1.234500");
                ruleNode5.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} == '1.234500'");
                ruleNode6.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} != '1.234500'");

                assertEquals(true, ruleNode1.ruleTriggered());
                assertEquals(true, ruleNode2.ruleTriggered());
                assertEquals(true, ruleNode3.ruleTriggered());
                assertEquals(true, ruleNode4.ruleTriggered());
                assertEquals(false, ruleNode5.ruleTriggered());
                assertEquals(true, ruleNode6.ruleTriggered());

                ruleNode1.free();
                ruleNode2.free();
                ruleNode3.free();
                ruleNode4.free();
                ruleNode5.free();
                ruleNode6.free();

                justNodeForValue.free();

                graph.disconnect(null);
            }
        });
    }

    @Test
    public void checkConstantStringComparison(){
        final Graph graph = new GraphBuilder().withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                RuleNode ruleNode1 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode2 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode3 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode4 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);

                ruleNode1.set(RuleNode.INTERNAL_CONDITION_STRING, "1.234500 == '1.2345'");
                ruleNode2.set(RuleNode.INTERNAL_CONDITION_STRING, "'cdef' != 'abcd'");
                ruleNode3.set(RuleNode.INTERNAL_CONDITION_STRING, "'1.2345' == '1.2345'");
                ruleNode4.set(RuleNode.INTERNAL_CONDITION_STRING, "'1.2345' != '1.234500'");

                assertEquals(true, ruleNode1.ruleTriggered()); //Double will be transferred to string
                assertEquals(true, ruleNode2.ruleTriggered());
                assertEquals(true, ruleNode3.ruleTriggered());
                assertEquals(true, ruleNode4.ruleTriggered());

                ruleNode1.free();
                ruleNode2.free();
                ruleNode3.free();
                ruleNode4.free();

                graph.disconnect(null);
            }
        });
    }

    @Test
    public void checkStringValueComparison(){
        final Graph graph = new GraphBuilder().withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                RuleNode ruleNode1 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode2 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode3 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode4 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);

                GaussianClassifierNode justNodeForValue = (GaussianClassifierNode)
                        graph.newTypedNode(0, 0, GaussianClassifierNode.NAME);

                justNodeForValue.setProperty("someValue", Type.STRING, "ON");

                String requestStr = justNodeForValue.id()+".someValue";

                ruleNode1.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} == 'ON'");
                ruleNode2.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} != 'OFF'");
                ruleNode3.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} == 'OFF'");
                ruleNode4.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} != 'ON'");

                assertEquals(true, ruleNode1.ruleTriggered());
                assertEquals(true, ruleNode2.ruleTriggered());
                assertEquals(false, ruleNode3.ruleTriggered());
                assertEquals(false, ruleNode4.ruleTriggered());

                ruleNode1.free();
                ruleNode2.free();
                ruleNode3.free();
                ruleNode4.free();

                justNodeForValue.free();

                graph.disconnect(null);
            }
        });
    }

    @Test
    public void checkStringValueComparisonKeywordConfusions(){
        final Graph graph = new GraphBuilder().withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                RuleNode ruleNode1 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode2 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode3 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode4 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);

                GaussianClassifierNode justNodeForValue = (GaussianClassifierNode)
                        graph.newTypedNode(0, 0, GaussianClassifierNode.NAME);

                justNodeForValue.setProperty("someValue", Type.STRING, "{4.someValue}");

                String requestStr = justNodeForValue.id()+".someValue";

                ruleNode1.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} == '{4.someValue}'");
                ruleNode2.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} != '3 > 1'");
                ruleNode3.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} == '3 > 1'");
                ruleNode4.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} != '{4.someValue}'");

                assertEquals(true, ruleNode1.ruleTriggered());
                assertEquals(true, ruleNode2.ruleTriggered());
                assertEquals(false, ruleNode3.ruleTriggered());
                assertEquals(false, ruleNode4.ruleTriggered());

                ruleNode1.free();
                ruleNode2.free();
                ruleNode3.free();
                ruleNode4.free();

                justNodeForValue.free();

                graph.disconnect(null);
            }
        });
    }

    @Test
    public void checkStringValueComparisonKeywordConfusions2(){
        final Graph graph = new GraphBuilder().withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                RuleNode ruleNode1 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode2 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode3 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode4 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);

                GaussianClassifierNode justNodeForValue = (GaussianClassifierNode)
                        graph.newTypedNode(0, 0, GaussianClassifierNode.NAME);

                justNodeForValue.setProperty("someValue", Type.STRING, "1 > 3");

                String requestStr = justNodeForValue.id()+".someValue";

                ruleNode1.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} == '{4.someValue}'");
                ruleNode2.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} != '1 > 3'");
                ruleNode3.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} == '1 > 3'");
                ruleNode4.set(RuleNode.INTERNAL_CONDITION_STRING, "{"+requestStr+"} != '{4.someValue}'");

                assertEquals(false, ruleNode1.ruleTriggered());
                assertEquals(false, ruleNode2.ruleTriggered());
                assertEquals(true, ruleNode3.ruleTriggered());
                assertEquals(true, ruleNode4.ruleTriggered());

                ruleNode1.free();
                ruleNode2.free();
                ruleNode3.free();
                ruleNode4.free();

                justNodeForValue.free();

                graph.disconnect(null);
            }
        });
    }

    private void fitPoly(PolynomialNode polynomialNode, long times[], double values[]){
        for (int i = 0; i < times.length; i++) {
            final int ia = i;
            polynomialNode.jump(times[ia], new Callback<PolynomialNode>() {
                @Override
                public void on(PolynomialNode result) {
                    result.learn(values[ia], new Callback<Boolean>() {
                        @Override
                        public void on(Boolean result) {

                        }
                    });
                }
            });
        }
    }

    @Test
    public void checkPolynomialNodeDegreeZeroDerivativeRetrieval(){
        final Graph graph = new GraphBuilder().withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                RuleNode ruleNode1 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode2 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);

                long[] times = new long[100];
                double[] values = new double[times.length];
                //test degree 0
                for (int i = 0; i < times.length; i++) {
                    times[i] = i * 10 + 5000;
                    values[i] = 42.0;
                }

                PolynomialNode polynomialNode = (PolynomialNode) graph.newTypedNode(0, times[0], PolynomialNode.NAME);
                polynomialNode.set(PolynomialNode.PRECISION, 0.5);

                fitPoly(polynomialNode, times, values);

                String requestStr = polynomialNode.id()+"."+PolynomialNode.VALUE;

                ruleNode1.set(RuleNode.INTERNAL_CONDITION_STRING, "d{"+requestStr+"} == 0");
                ruleNode2.set(RuleNode.INTERNAL_CONDITION_STRING, "d{"+requestStr+"} != 0");

                assertEquals(true, ruleNode1.ruleTriggered());
                assertEquals(false, ruleNode2.ruleTriggered());

                ruleNode1.free();
                ruleNode2.free();

                polynomialNode.free();

                graph.disconnect(null);
            }
        });
    }

    @Test
    public void checkPolynomialNodeOneDegreeDerivativeRetrieval(){
        final Graph graph = new GraphBuilder().withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                RuleNode ruleNode1 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode2 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode3 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);

                long[] times = new long[100];
                double[] values = new double[times.length];
                //test degree 0
                for (int i = 0; i < times.length; i++) {
                    times[i] = i * 10 + 5000;
                    values[i] = 3 * i - 20;
                }

                PolynomialNode polynomialNode = (PolynomialNode) graph.newTypedNode(0, times[0], PolynomialNode.NAME);
                polynomialNode.set(PolynomialNode.PRECISION, 0.5);

                fitPoly(polynomialNode, times, values);

                String requestStr = polynomialNode.id()+"."+PolynomialNode.VALUE;

                ruleNode1.set(RuleNode.INTERNAL_CONDITION_STRING, "d{"+requestStr+"} > 2.999");
                ruleNode2.set(RuleNode.INTERNAL_CONDITION_STRING, "d{"+requestStr+"} < 3.001");
                ruleNode3.set(RuleNode.INTERNAL_CONDITION_STRING, "d{"+requestStr+"} == 2.998");

                assertEquals(true, ruleNode1.ruleTriggered());
                assertEquals(true, ruleNode2.ruleTriggered());
                assertEquals(false, ruleNode3.ruleTriggered());

                ruleNode1.free();
                ruleNode2.free();
                ruleNode3.free();

                polynomialNode.free();

                graph.disconnect(null);
            }
        });
    }

    @Test
    public void checkPolynomialNodeTwoDegreeDerivativeRetrieval(){
        final Graph graph = new GraphBuilder().withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                RuleNode ruleNode1 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode2 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);
                RuleNode ruleNode3 = (RuleNode) graph.newTypedNode(0, 0, RuleNode.NAME);

                long[] times = new long[100];
                double[] values = new double[times.length];
                //test degree 0
                for (int i = 0; i < times.length; i++) {
                    times[i] = i * 10 + 5000;
                    values[i] = 3 * i * i - 99 * i - 20;
                }

                PolynomialNode polynomialNode = (PolynomialNode) graph.newTypedNode(0, times[0], PolynomialNode.NAME);
                polynomialNode.set(PolynomialNode.PRECISION, 0.5);

                fitPoly(polynomialNode, times, values);

                String requestStr = polynomialNode.id()+"."+PolynomialNode.VALUE;

                ruleNode1.set(RuleNode.INTERNAL_CONDITION_STRING, "d{"+requestStr+"} < -98.9999");
                ruleNode2.set(RuleNode.INTERNAL_CONDITION_STRING, "d{"+requestStr+"} > -99.0001");
                ruleNode3.set(RuleNode.INTERNAL_CONDITION_STRING, "d{"+requestStr+"} == -100");

                assertEquals(true, ruleNode1.ruleTriggered());
                assertEquals(true, ruleNode2.ruleTriggered());
                assertEquals(false, ruleNode3.ruleTriggered());

                ruleNode1.free();
                ruleNode2.free();
                ruleNode3.free();

                polynomialNode.free();

                graph.disconnect(null);
            }
        });
    }

}
