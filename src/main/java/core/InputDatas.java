package core;

import java.io.File;
import java.util.Scanner;

public class InputDatas {
    private String alphabets;
    private String numStates;
    private String transitionTable;
    private String startState;
    private String finalStates;
    private String testString;

    // --- CONSOLE INPUT ---
    public static DFA buildDFAFromConsole(Scanner sc) {
        System.out.println("--- DFA Configuration ---");

        // 1. Alphabet (Sigma)
        System.out.print("Alphabet (e.g., 'a b c', 'a,b,c', or 'abc'): ");
        String alphaInput = sc.nextLine();
        String cleanAlphabet = alphaInput.replaceAll("[\\s,]+", "");
        char[] alphabet = cleanAlphabet.chars()
                .distinct()
                .mapToObj(c -> (char) c)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString().toCharArray();

        int sizeE = alphabet.length;
        System.out.println("(|Sigma| = " + sizeE + ")");

        // 2. States (Q)
        System.out.print("Enter number of states (|Q|): ");
        int n = sc.nextInt();

        // 3. Transition Function (delta)
        int[][] transitions = new int[n][sizeE];
        System.out.println("--- Transition Table (delta) ---");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < sizeE; j++) {
                System.out.print("delta(q" + i + ", '" + alphabet[j] + "') -> q");
                transitions[i][j] = sc.nextInt();
                if (transitions[i][j] < 0 || transitions[i][j] >= n) {
                    System.out.println("Transition q" + i + " does not exist");
                    --j;
                }
            }
        }

        // 4. Start State (q0) and Final States (F)
        System.out.print("Enter start state (q0): ");
        int q0;
        do {
            q0 = sc.nextInt();
            if (q0 < 0 || q0 >= n) {
                System.out.println("Invalid start state, re-enter: ");
            }
        } while (q0 < 0 || q0 >= n);

        System.out.print("Enter number of final states (|F|): ");
        int sizeF;
        do {
            sizeF = sc.nextInt();
            if (sizeF < 0 || sizeF > n) {
                System.out.println("Invalid number of final states, re-enter: ");
            }
        } while (sizeF < 0 || sizeF > n);

        int[] finalStates = new int[sizeF];
        for (int i = 0; i < sizeF; i++) {
            System.out.print("Final state [" + i + "]: ");
            finalStates[i] = sc.nextInt();
            if (finalStates[i] < 0 || finalStates[i] >= n) {
                System.out.println("Final state out of range");
                --i;
            }
        }

        sc.nextLine(); // Clear scanner buffer

        return new DFA(q0, finalStates, transitions, alphabet);
    }

    // --- FILE PARSING ---
    public static InputDatas parseFromFile(File file) throws Exception {
        InputDatas data = new InputDatas();

        try (Scanner scanner = new Scanner(file)) {
            if (scanner.hasNextLine()) data.alphabets = scanner.nextLine().trim();
            if (scanner.hasNextLine()) data.numStates = scanner.nextLine().trim();
            if (scanner.hasNextLine()) data.transitionTable = scanner.nextLine().trim();
            if (scanner.hasNextLine()) data.startState = scanner.nextLine().trim();
            if (scanner.hasNextLine()) data.finalStates = scanner.nextLine().trim();

            // Test string line might be optional
            if (scanner.hasNextLine()) {
                data.testString = scanner.nextLine().trim();
            } else {
                data.testString = "";
            }
        }
        return data;
    }

    // --- UI DATA VALIDATION (Called by MainApp) ---

    // Validates the number of states input
    public static int validateNumStates(String numStatesText) throws IllegalArgumentException {
        try {
            int n = Integer.parseInt(numStatesText.trim());
            if (n <= 0) throw new IllegalArgumentException("Number of states must be greater than 0.");
            return n;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number of states. Please enter a valid integer.");
        }
    }

    // Validates a single cell in the transition table
    public static void validateTransitionInput(String targetText, int numStates, String alphabetChar, int rowIndex) throws IllegalArgumentException {
        String text = targetText.trim();
        if (text.isEmpty()) return; // Allow empty cells for incomplete graphs

        try {
            int targetState = Integer.parseInt(text);
            if (targetState < 0 || targetState >= numStates) {
                throw new IllegalArgumentException(
                        "Target state '" + targetState + "' at cell (q" + rowIndex + ", " + alphabetChar + ") does not exist!\n" +
                                "With " + numStates + " states, you can only enter values from 0 to " + (numStates - 1) + "."
                );
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Please enter a valid integer! Error at cell (q" + rowIndex + ", " + alphabetChar + ").");
        }
    }

    // --- Getters to pass data to MainApp ---
    public String getAlphabets() { return alphabets; }
    public String getNumStates() { return numStates; }
    public String getTransitionTable() { return transitionTable; }
    public String getStartState() { return startState; }
    public String getFinalStates() { return finalStates; }
    public String getTestString() { return testString; }

    // Utility: Split transition table string into target states array
    public String[] getTransitionTokens() {
        if (transitionTable == null || transitionTable.isEmpty()) return new String[0];
        return transitionTable.split("\\s+");
    }

    public static String getTestString(Scanner sc) {
        System.out.print("\nEnter input string (or 'exit' to quit): ");
        return sc.nextLine();
    }
}