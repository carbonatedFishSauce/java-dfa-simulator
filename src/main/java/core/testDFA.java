package core;

import java.util.Scanner;

public class testDFA {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        DFA dfa = InputDatas.buildDFAFromConsole(sc);

        while (true) {
            String inputStr = InputDatas.getTestString(sc);
            if (inputStr.equalsIgnoreCase("exit")) break;

            dfa.reset(inputStr);
            System.out.println("[START] Initial state: q" + dfa.getCurrentState());
            System.out.println("(Press Enter to step forward)");

            while (dfa.getStatus() == DFA.ProcessingStatus.RUNNING) {
                sc.nextLine(); // Simulate "Next Step" button click

                int oldState = dfa.getCurrentState();
                char readChar = (dfa.getCurrentStepIndex() < inputStr.length())
                        ? inputStr.charAt(dfa.getCurrentStepIndex()) : ' ';

                dfa.step();

                if (dfa.getStatus() != DFA.ProcessingStatus.ERROR_INVALID_CHAR) {
                    System.out.printf("Step: q%d --(%c)--> q%d%n", oldState, readChar, dfa.getCurrentState());
                }
            }

            System.out.println("Result: " + dfa.getStatus());
        }
        sc.close();
    }
}