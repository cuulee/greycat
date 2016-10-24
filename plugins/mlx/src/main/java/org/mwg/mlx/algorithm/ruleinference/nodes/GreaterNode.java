package org.mwg.mlx.algorithm.ruleinference.nodes;

/**
 * Created by andrey.boytsov on 24/10/2016.
 */
public class GreaterNode implements ConditionGraphNode{
    private final ConditionGraphNode leftSide;
    private final ConditionGraphNode rightSide;

    public GreaterNode(ConditionGraphNode leftSide, ConditionGraphNode rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    @Override
    public double getValue() {
        return (leftSide.getValue() > rightSide.getValue()) ? 1 : -1;
    }
}