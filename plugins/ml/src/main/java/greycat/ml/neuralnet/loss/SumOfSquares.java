/**
 * Copyright 2017 The GreyCat Authors.  All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greycat.ml.neuralnet.loss;

import greycat.ml.common.matrix.MatrixOps;
import greycat.ml.neuralnet.process.ExMatrix;

class SumOfSquares implements Loss {

    private static SumOfSquares static_unit = null;

    public static SumOfSquares instance() {
        if (static_unit == null) {
            static_unit = new SumOfSquares();
        }
        return static_unit;
    }

    @Override
    public void backward(ExMatrix actualOutput, ExMatrix targetOutput) {

        int len = targetOutput.length();

        for (int i = 0; i < len; i++) {
            double errDelta = actualOutput.unsafeGet(i) - targetOutput.unsafeGet(i);  //double errDelta = actualOutput.w[i] - targetOutput.w[i];
            actualOutput.getDw().unsafeSet(i, actualOutput.getDw().unsafeGet(i) + errDelta); //actualOutput.dw[i] += errDelta;
        }
    }

    @Override
    public double forward(ExMatrix actualOutput, ExMatrix targetOutput) {
        MatrixOps.testDim(actualOutput,targetOutput);
        double sum = 0;
        int len = targetOutput.length();
        for (int i = 0; i < len; i++) {
            double errDelta = actualOutput.unsafeGet(i) - targetOutput.unsafeGet(i);
            sum += 0.5 * errDelta * errDelta;
        }
        return sum;
    }
}
