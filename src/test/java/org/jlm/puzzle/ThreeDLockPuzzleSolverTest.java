/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jlm.puzzle;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ThreeDLockPuzzleSolverTest {

    private ThreeDLockPuzzleSolver solver;

    @BeforeEach
    public void initSolver() {
        solver = new ThreeDLockPuzzleSolver(3);
    }

    @Test
    public void sizeMustBeCorrect() {
        // wrong
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new ThreeDLockPuzzleSolver(-3);
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new ThreeDLockPuzzleSolver(0);
        });

        // correct
        new ThreeDLockPuzzleSolver(10);
    }

    @Test
    public void solveWithConstraintShouldFail() {
        Assertions.assertThrows(NoConstraintException.class, () -> {
            solver.solve();
        });
    }

    @Test
    public void constraintsMustBeCorrect() {
        // wrong
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            solver.addConstraint(new int[] {0, 1, 2}, -1, 1);
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            solver.addConstraint(new int[] {0, 1, 2}, 0, -1);
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            solver.addConstraint(null, 0, 0);
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            solver.addConstraint(new int[] {0, 1, 2}, 0, 10);
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            solver.addConstraint(new int[] {0, 1, 2, 3}, 0, 1);
        });

        // correct
        solver.addConstraint(new int[] {0, 1, 2}, 0, 1);
    }

    @Test
    public void arthy() {
        solver.addConstraint(new int[] {1, 4, 7}, 1, 0);
        solver.addConstraint(new int[] {1, 8, 9}, 1, 1);
        solver.addConstraint(new int[] {9, 6, 4}, 2, 0);
        solver.addConstraint(new int[] {5, 2, 3}, 0, 0);
        solver.addConstraint(new int[] {2, 8, 6}, 1, 0);

        // solve the lock
        final List<int[]> guesses = solver.solve();
        Assertions.assertNotNull(guesses);

        // make sure only one solution matched
        Assertions.assertEquals(1, guesses.size());

        // and of course make sure it's the expected one
        final int[] guess = guesses.get(0);
        Assertions.assertNotNull(guess);
        Assertions.assertEquals(3, guess.length);
        assertMatchGuess(new int[] {6, 7, 9}, guess);
    }

    @Test
    public void multipleMatch() {
        // yank one constraint and make sure we have more than one matching guess
        solver.addConstraint(new int[] {1, 8, 9}, 1, 1);
        solver.addConstraint(new int[] {9, 6, 4}, 2, 0);
        solver.addConstraint(new int[] {5, 2, 3}, 0, 0);
        solver.addConstraint(new int[] {2, 8, 6}, 1, 0);

        // solve the lock
        final List<int[]> guesses = solver.solve();
        Assertions.assertNotNull(guesses);

        // 609, 679
        Assertions.assertEquals(2, guesses.size());

        assertMatchGuess(new int[] {6, 0, 9}, guesses.get(0));
        assertMatchGuess(new int[] {6, 7, 9}, guesses.get(1));
    }

    private void assertMatchGuess(final int[] expected, final int[] guess) {
        Assertions.assertNotNull(guess);
        Assertions.assertEquals(expected.length, guess.length);
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertEquals(expected[i], guess[i]);
        }
    }

}
