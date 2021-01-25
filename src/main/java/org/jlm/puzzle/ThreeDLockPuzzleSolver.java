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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThreeDLockPuzzleSolver {

    private static final Logger LOGGER = Logger.getLogger(ThreeDLockPuzzleSolver.class.getName());

    private final int size;
    private final List<Constraint> constraints = new ArrayList<>();

    // result when problem is solved
    private final List<int[]> guesses = new ArrayList<>();

    public ThreeDLockPuzzleSolver(final int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be > 0");
        }
        this.size = size;
    }

    public List<int[]> solve() {
        if(constraints.size() == 0) {
            throw new NoConstraintException();
        }

        final double totalCombinations = Math.pow(9, size);
        for (int i = 0; i < totalCombinations; i++) {
            final String format = withLeadingZero(i, size);
            final int[] attempt = toIntArray(format);
            if (checkAttemptAgainstConstraint(attempt)) {
                LOGGER.info("Found match with " + format);
                guesses.add(attempt);
            }
        }

        // return the guess
        return guesses;
    }

    public static String withLeadingZero(final int i, final int size) {
        return String.format("%0" + size + "d", i);
    }

    public static int[] toIntArray(final String withLeadingZero) {
        return withLeadingZero.chars().map(c -> c - '0').toArray();
    }

    private boolean checkAttemptAgainstConstraint(final int[] attempt) {
        boolean isCorrect = true;
        for (Constraint constraint : constraints) {
            isCorrect &= constraint.matches(attempt);
        }
        return isCorrect;
    }

    public void addConstraint(final int[] digits, final int right, final int inPlace) {
        checkDigits(digits);
        if (digits.length != size) {
            throw new IllegalArgumentException("Size of digits must be " + size);
        }
        checkPosition(right, "right", digits.length);
        checkPosition(inPlace, "in place", digits.length);

        constraints.add(new Constraint(digits, right, inPlace));
    }

    public static class Constraint {
        private final int[] digits;
        private final int numberRight;
        private final int numberRightAndInPlace;

        public Constraint(final int[] digits, final int numberRight, final int numberRightAndInPlace) {
            checkDigits(digits);
            checkPosition(numberRight, "right", digits.length);
            checkPosition(numberRightAndInPlace, "right and in place", digits.length);

            this.digits = digits;
            this.numberRight = numberRight;
            this.numberRightAndInPlace = numberRightAndInPlace;
        }

        public int[] getDigits() {
            return digits;
        }

        public int getNumberRight() {
            return numberRight;
        }

        public int getNumberRightAndInPlace() {
            return numberRightAndInPlace;
        }

        public boolean matches(final int[] guess) {
            if (guess == null) {
                throw new IllegalArgumentException("Guess can't be null");
            }
            if (guess.length != digits.length) {
                throw new IllegalArgumentException("Guess must be of length " + digits.length);
            }

            final int digitsRight = numberDigitsRight(guess);
            final int digitsRightInPlace = numberDigitsRightInItsPlace(guess);

            final boolean matches = digitsRight == getNumberRight() &&
                                    digitsRightInPlace == getNumberRightAndInPlace();


            if (LOGGER.isLoggable(Level.FINE)) { // expensive string to build so don't build if log level is not enabled
                LOGGER.fine(Arrays.toString(guess) + " ? " + toString()
                            + "\n"
                            + digitsRight + " / " + digitsRightInPlace
                            + " >> " + matches);
            }

            return matches;
        }

        /**
         * Iterates against the guess and see if some of the digit of the constraint match
         *
         * @param guess the guess to be checking against
         * @return the number of digit in this constraint also available in the guess
         */
        private int numberDigitsRight(final int[] guess) {
            int nb = 0;
            for (int g : guess) {
                for (int d : digits) {
                    nb = nb + (g == d ? 1 : 0);
                }
            }
            return nb;
        }

        /**
         * Check the digit with are right and in place
         *
         * @param guess the guess to be checking against
         * @return the number of digit in this constraint also available in the guess and in the right place
         */
        private int numberDigitsRightInItsPlace(final int[] guess) {
            int nb = 0;
            for (int i = 0; i < guess.length; i++) {
                nb = nb + (guess[i] == digits[i] ? 1 : 0);
            }
            return nb;
        }

        @Override public String toString() {
            return "Constraint{" +
                   "digits=" + Arrays.toString(digits) +
                   ", numberRight=" + numberRight +
                   ", numberRightAndInPlace=" + numberRightAndInPlace +
                   '}';
        }
    }

    private static void checkPosition(final int number, final String what, final int size) {
        if (number < 0 | number > size) {
            throw new IllegalArgumentException("Number of " + what + " digits must be between 0 and " + size);
        }
    }

    private static void checkDigits(final int[] digits) {
        if (digits == null || digits.length <= 0) {
            throw new IllegalArgumentException("Size of digits must be > 0");
        }

        for (int digit : digits) {
            if (digit < 0 || digit > 9) {
                throw new IllegalArgumentException("Each digit must be between 0 and 9");
            }
        }
    }

    public static void main(final String[] args) {
        final Pattern pattern = Pattern.compile("^(\\d{3}) ([0-3]),([0-3])$");
        final Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the 3D Lock Puzzle game.");
        System.out.println("Enter your clues in the following format ddd right, in place. Press 'q' when you are done.");

        final ThreeDLockPuzzleSolver solver = new ThreeDLockPuzzleSolver(3);
        String line = "";
        while(!"q".equals(line = scanner.nextLine())) {
            // check the line matches what we expect
            final Matcher matcher = pattern.matcher(line);
            if (!matcher.matches()) {
                System.out.println("Clue is not in the expected format: " + line + ". Try again...");
                continue;
            }
            final String digits = matcher.group(1);
            final int digitsRight = Integer.parseInt(matcher.group(2));
            final int digitsRightAndInPlace = Integer.parseInt(matcher.group(3));

            solver.addConstraint(toIntArray(digits), digitsRight, digitsRightAndInPlace);
        }

        final List<int[]> guesses = solver.solve();
        if (guesses.size() == 0) {
            System.out.println("No result found.");

        } else {
            System.out.println("Found " + guesses.size() + " result(s).");
            for (int[] guess : guesses) {
                System.out.println(">>> " + Arrays.toString(guess));
            }
        }
    }

}
