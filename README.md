# DFA Visualization Simulator

A Desktop Application built with Pure JavaFX to visualize, design, and simulate Deterministic Finite Automata (DFA). The application includes dynamic graph rendering, validation processing, and step-by-step algorithm visualization.

---

## To Run App Only (For End Users)

If you only want to use the application to simulate DFAs, you do **not** need to manually install Java, Maven, or any development environments.

### 1. Download the Application
You can get the application files using one of the following methods:

* **Option A (Via Git):** Open your terminal or command prompt and run:
    ```bash
    git clone https://github.com/carbonatedFishSauce/java-dfa-simulator.git
    ```

* **Option B (Direct Download):** Go to the project repository link. Click on **Code -> Download ZIP**. Once downloaded, extract the `.zip` file and open the `java-dfa-simulator` folder.

---

### 2. How to Launch
Run the provided executable script for your Operating System:

* **Windows:** Double-click `run-dfa-simulator.bat`.
* **Linux / macOS / Windows WSL:** Open a terminal inside the folder and run the following commands:
    ```bash
    chmod +x run-dfa-simulator.sh
    ./run-dfa-simulator.sh
    ```

> **Note:** If your system does not have Java or Maven installed, the script will prompt you to download portable versions automatically into a hidden `.tools` folder. Just type `Y` and press **Enter**.

---

### 3. How to Use the App

* **Input:** Enter the Alphabet (space-separated) and the Total Number of States.
* **Generate Table:** Click **Generate Transition Table** to build the input matrix.
* **Define Transitions:** Fill in the target state indices (e.g., enter `1` for state q1). Leave empty if the transition does not exist.
* **Finalize Config:** Enter the Start State (e.g., `0` for q0) and Final States (space-separated).
* **Build Graph:** Click **Build DFA Graph** to render the nodes and edges on the canvas. You can drag the canvas to explore.
* **Testing:** Enter a string. Use **Run Full Test** for automatic animation, or **Start Stepping -> Next Step** for manual trace.
* **Importing:** Use **Menu -> Input -> Import from .txt** to load a pre-configured DFA.
---

## TO OPEN/RUN PROJECT (For Developers)
If you wish to view the source code, modify the project, or build it yourself, follow these steps:

**1. Prerequisites:**
* **Java Development Kit (JDK):** Version 21 or higher.
* **Apache Maven:** Version 3.8+ for dependency management.
* An IDE supporting JavaFX/Maven (IntelliJ IDEA, Eclipse, or VS Code with Java Extension Pack).

**2. Setup & Installation:**
1. Clone this repository: `git clone <repository_url>`
2. Open the project folder in your IDE. The IDE should automatically resolve `pom.xml`.
3. To run via CLI manually: `mvn clean javafx:run`
4. To build an executable JAR (requires shade plugin config): `mvn clean package`

---

## Test Case Demo
To quickly test the application without manual typing, you can create a `demo.txt` file anywhere on your computer with the following structure (This DFA accepts binary strings ending in "01"):
```text
a b c
6
1 4 2 5 0 0 4 1 5 2 3 2 4 0 1 0 2 3
0
0
abcabcabcbbcabcabacabcaccc
```
## Security Vulnerabilities
As a local desktop application, standard web-based vulnerabilities (such as SQL Injection or XSS) are not applicable. However, users and developers should be aware of the following local risks:
* **Resource Exhaustion (Denial of Service):** Inputting an astronomically long test string (e.g., millions of characters) and triggering the automatic UI animation can lead to memory heap overflows or permanently freeze the JavaFX Application Thread.
* **Malicious File Parsing:** The application reads local `.txt` configuration files. While `InputDatas.java` handles most `IllegalArgumentException` cases, a specially crafted, excessively large or malformed file could bypass basic validation and crash the buffer reader or consume excessive memory.

## Known Bugs & Limitations
* **Edge Overlapping in Complex Graphs:** For highly complex DFAs (e.g., nearly complete graphs with 8+ states), the purely mathematical circular placement of `QuadCurve` and `CubicCurve` elements may result in overlapping transition lines, making labels difficult to distinguish.
* **Static Node Positioning (No Auto-Layout):** Nodes are arranged in a strict, hardcoded circular pattern based on the total number of states. The application does not currently utilize force-directed graph drawing algorithms (like Spring Layout) for optimal space utilization.
* **Responsive Resizing:** The graph canvas uses a fixed logical center point. Drastically resizing the application window may require the user to manually click and drag the canvas to recenter the graph.
* **Missing Visual Error Highlighting:** If a user inputs a character not defined in the alphabet during manual stepping, the app triggers an alert popup but does not highlight the exact invalid character index within the input text field.

## Future Development
* **Save & Export Functionality:** Enable users to export their constructed DFA graph as a reusable `.txt` configuration file or capture the canvas as a high-resolution `.png` image for documentation purposes.
* **NFA & ε-NFA Support:** Implement backend logic to accept Non-deterministic Finite Automata configurations (including epsilon transitions), visualize them, and automatically convert them to their equivalent minimal DFAs using the Subset Construction algorithm.
* **Interactive Drag & Drop:** Allow users to manually click, drag, and reposition individual nodes around the canvas to resolve overlapping edges and customize the layout interactively.
* **Regex Engine Integration:** Allow users to input a Regular Expression (Regex) directly, and have the application automatically parse, generate, and visualize the corresponding DFA.
