package core;

public class DFA {
    private int startState;          // q0
    private int[] finalStates;       // arFinalState[]
    private int[][] transitionTable; // arTrans[][]
    private char[] alphabet;         // arEdgeVal[]

    // Variable visualizing the algorithm at core processing
    private int currentState;
    private String inputString;
    private int currentStepIndex;
    private ProcessingStatus status;
        enum ProcessingStatus {
            IDLE, RUNNING, ACCEPTED, REJECTED, ERROR_INVALID_CHAR
        }

    // Constructing DFA from inputs
    public DFA(int startState, int[] finalStates, int[][] transitionTable, char[] alphabet) {
        this.startState = startState;
        this.finalStates = finalStates;
        this.transitionTable = transitionTable;
        this.alphabet = alphabet;
        this.status = ProcessingStatus.IDLE;
    }

    public void reset (String input) {
            this.inputString = input;
            this.currentStepIndex = 0;
            this.currentState = startState;
            this.status = ProcessingStatus.RUNNING;
            if (input == null){
                checkFinalState();
            }
    }

    public ProcessingStatus step() {
        if (status != ProcessingStatus.RUNNING) {
            return status;
        }
        if (currentStepIndex >= inputString.length()) {
            checkFinalState();
            return status;
        }
        char currentChar = inputString.charAt(currentStepIndex);
        int charIndex = -1;

        // Tìm cột tương ứng với ký tự trong bảng chữ cái
        for (int j = 0; j < alphabet.length; j++) {
            if (alphabet[j] == currentChar) {
                charIndex = j;
                break;
            }
        }
        if (charIndex == -1) {
            System.out.println("ERROR: char '" + currentChar + "' non-existent in alphabet");
            status = ProcessingStatus.ERROR_INVALID_CHAR;
            return status;
        }
        // Switch state on command
        currentState = transitionTable[currentState][charIndex];
        currentStepIndex++;

        // Return result at end of string
        if (currentStepIndex == inputString.length()) {
            checkFinalState();
        }
        return status;
    }

    public void checkFinalState() {
            for (int fState : finalStates) {
                if (currentState == fState) {
                    status = ProcessingStatus.ACCEPTED;
                    return;
                }
            }
            status = ProcessingStatus.REJECTED;
    }

    // --- Getter functions để for Class UI to illustrate ---
    public int getCurrentState() { return currentState; }
    public ProcessingStatus getStatus() { return status; }
    public String getInputString() { return inputString; }
    public int getCurrentStepIndex() { return currentStepIndex; }
    public char getLastReadChar() {
        if (currentStepIndex > 0 && currentStepIndex <= inputString.length()) {
            return inputString.charAt(currentStepIndex - 1);
        }
        return ' ';
    }
}
