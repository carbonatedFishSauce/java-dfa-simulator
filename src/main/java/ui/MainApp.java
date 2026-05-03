package ui;

import core.InputDatas;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;

public class MainApp extends Application {

    // --- UI COMPONENTS ---
    private TextField txtAlphabet;
    private TextField txtNumStates;
    private TextField txtStartState;
    private TextField txtFinalStates;
    private TextField txtTestString;
    private Button btnGenTable;
    private Button btnBuild;
    private Button btnTest;

    // New buttons for Step-by-Step testing
    private Button btnStartStep;
    private Button btnNextStep;

    private TextField[][] transitionCells;
    private String[] alphabets;
    private String startState;
    private List<String> finalStates;

    // --- MANUAL STEPPING STATE VARIABLES ---
    private String stepCurrentState;
    private int stepIndex;
    private String stepInputStr;
    private String activeEdgeId;

    // Graph Drawing Area
    private Pane graphPane;
    private Map<String, StackPane> uiNodes = new HashMap<>();
    private Map<String, List<Shape>> uiEdges = new HashMap<>();

    // Colors
    private final Color NORMAL_COLOR = Color.web("#e0e0e0");
    private final Color ACTIVE_COLOR = Color.web("#ffeb3b");
    private final Color STROKE_COLOR = Color.web("#6666ff");
    private final Color ACTIVE_STROKE = Color.web("#ff9800");

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });

        // --- 1. MENU BAR SETUP ---
        MenuBar menuBar = new MenuBar();
        Menu menuInput = new Menu("Input");
        MenuItem itemImport = new MenuItem("Import from .txt...");

        itemImport.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select DFA Configuration File (.txt)");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                importDataFromFile(file);
            }
        });

        menuInput.getItems().add(itemImport);
        menuBar.getMenus().add(menuInput);

        // --- 2. LEFT INPUT PANEL ---
        VBox inputPanel = new VBox(10);
        inputPanel.setPadding(new Insets(15));
        inputPanel.setPrefWidth(350);

        txtAlphabet = new TextField();
        txtNumStates = new TextField();
        btnGenTable = new Button("Generate Transition Table");
        btnGenTable.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        VBox tableContainer = new VBox(5);
        ScrollPane scrollPane = new ScrollPane(tableContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(180);

        txtStartState = new TextField();
        txtFinalStates = new TextField();
        txtTestString = new TextField();

        btnBuild = new Button("Build DFA Graph");
        btnTest = new Button("Run Full Test");
        btnStartStep = new Button("Start Stepping");
        btnNextStep = new Button("Next Step \u23ED"); // Fast forward unicode symbol

        // Initially disable test buttons until graph is built
        btnTest.setDisable(true);
        btnStartStep.setDisable(true);
        btnNextStep.setDisable(true);

        // Wrap testing buttons in HBoxes for better layout
        HBox buildBox = new HBox(10, btnBuild);
        HBox testBox = new HBox(10, btnTest, btnStartStep, btnNextStep);

        // --- 3. RIGHT GRAPH PANEL ---
        graphPane = new Pane();
        graphPane.setStyle("-fx-background-color: #fdfdfd; -fx-border-color: #cccccc; -fx-border-width: 0 0 0 1;");

        final double[] dragDelta = new double[2];
        graphPane.setOnMousePressed(e -> {
            dragDelta[0] = graphPane.getLayoutX() - e.getSceneX();
            dragDelta[1] = graphPane.getLayoutY() - e.getSceneY();
        });
        graphPane.setOnMouseDragged(e -> {
            graphPane.setLayoutX(e.getSceneX() + dragDelta[0]);
            graphPane.setLayoutY(e.getSceneY() + dragDelta[1]);
        });

        // --- 4. BUTTON EVENTS ---
        btnGenTable.setOnAction(e -> {
            try {
                alphabets = txtAlphabet.getText().trim().split("\\s+");
                int numStates = InputDatas.validateNumStates(txtNumStates.getText());

                tableContainer.getChildren().clear();
                GridPane grid = new GridPane();
                grid.setHgap(8); grid.setVgap(8);

                grid.add(new Label("State \\ Σ"), 0, 0);
                for (int j = 0; j < alphabets.length; j++) {
                    grid.add(new Label("   '" + alphabets[j] + "'"), j + 1, 0);
                }

                transitionCells = new TextField[numStates][alphabets.length];
                for (int i = 0; i < numStates; i++) {
                    grid.add(new Label("  q" + i), 0, i + 1);
                    for (int j = 0; j < alphabets.length; j++) {
                        TextField cell = new TextField();
                        cell.setPrefWidth(45);
                        transitionCells[i][j] = cell;
                        grid.add(cell, j + 1, i + 1);
                    }
                }
                tableContainer.getChildren().add(grid);
            } catch (IllegalArgumentException ex) {
                showAlert("Input Error", ex.getMessage(), Alert.AlertType.ERROR);
            } catch (Exception ex) {
                showAlert("Error", "An unexpected error occurred generating the table.", Alert.AlertType.ERROR);
            }
        });

        btnBuild.setOnAction(e -> {
            if (transitionCells == null) return;
            int numStates = transitionCells.length;

            try {
                for (int i = 0; i < numStates; i++) {
                    for (int j = 0; j < alphabets.length; j++) {
                        InputDatas.validateTransitionInput(transitionCells[i][j].getText(), numStates, alphabets[j], i);
                    }
                }
            } catch (IllegalArgumentException ex) {
                showAlert("Table Configuration Error", ex.getMessage(), Alert.AlertType.ERROR);
                return;
            }

            graphPane.getChildren().clear();
            uiNodes.clear();
            uiEdges.clear();

            startState = "q" + txtStartState.getText().trim();
            finalStates = Arrays.asList(txtFinalStates.getText().trim().split("\\s+"));
            for(int i=0; i<finalStates.size(); i++) finalStates.set(i, "q" + finalStates.get(i));

            double centerX = graphPane.getWidth() / 2;
            if (centerX < 350) centerX = 350;
            double centerY = 350;
            double radius = 120 + (numStates * 35);

            Map<String, double[]> nodePositions = new HashMap<>();

            for (int i = 0; i < numStates; i++) {
                String stateId = "q" + i;
                double angle = 2 * Math.PI * i / numStates - Math.PI / 2;
                double x = centerX + radius * Math.cos(angle);
                double y = centerY + radius * Math.sin(angle);
                nodePositions.put(stateId, new double[]{x, y});

                StackPane nodePane = createNodeUI(stateId, finalStates.contains(stateId));
                nodePane.setLayoutX(x - 25);
                nodePane.setLayoutY(y - 25);
                uiNodes.put(stateId, nodePane);
            }

            Map<String, String> edgeLabels = new HashMap<>();
            for (int i = 0; i < numStates; i++) {
                String from = "q" + i;
                for (int j = 0; j < alphabets.length; j++) {
                    String targetIndex = transitionCells[i][j].getText().trim();
                    if (targetIndex.isEmpty()) continue;

                    String to = "q" + targetIndex;
                    String edgeKey = from + "-" + to;
                    edgeLabels.put(edgeKey, edgeLabels.getOrDefault(edgeKey, "") + (edgeLabels.containsKey(edgeKey) ? ", " : "") + alphabets[j]);
                }
            }

            for (String edgeKey : edgeLabels.keySet()) {
                String[] parts = edgeKey.split("-");
                String from = parts[0];
                String to = parts[1];
                String labelStr = edgeLabels.get(edgeKey);

                double[] pos1 = nodePositions.get(from);
                double[] pos2 = nodePositions.get(to);

                List<Shape> edgeShapes = drawEdge(pos1[0], pos1[1], pos2[0], pos2[1], from.equals(to), labelStr);
                uiEdges.put(edgeKey, edgeShapes);
                graphPane.getChildren().addAll(edgeShapes);
            }

            graphPane.getChildren().addAll(uiNodes.values());

            // Enable testing tools after successful build
            btnTest.setDisable(false);
            btnStartStep.setDisable(false);
            btnNextStep.setDisable(true);

            graphPane.setLayoutX(0); graphPane.setLayoutY(0);
        });

        // --- FULL TEST ANIMATION ---
        btnTest.setOnAction(e -> {
            String inputStr = txtTestString.getText().trim();
            if (inputStr.isEmpty()) {
                showAlert("Empty Input", "Please enter a string to test.", Alert.AlertType.WARNING);
                return;
            }

            // Lock UI controls during animation
            btnTest.setDisable(true);
            btnStartStep.setDisable(true);
            btnNextStep.setDisable(true);
            btnBuild.setDisable(true);

            long animationDelay = calculateAnimationDelay(inputStr.length());

            new Thread(() -> {
                try {
                    resetGraphColors();
                    String currentState = startState;
                    highlightNode(currentState, true);
                    Thread.sleep(animationDelay);

                    for (char c : inputStr.toCharArray()) {
                        String charStr = String.valueOf(c);
                        int charIndex = Arrays.asList(alphabets).indexOf(charStr);

                        if (charIndex == -1) {
                            Platform.runLater(() -> {
                                showAlert("Test Error", "Character '" + c + "' is not in the Alphabet!", Alert.AlertType.ERROR);
                                resetControlButtons();
                            });
                            return;
                        }

                        int rowIndex = Integer.parseInt(currentState.replace("q", ""));
                        String targetText = transitionCells[rowIndex][charIndex].getText().trim();

                        if (targetText.isEmpty()) {
                            String errState = currentState;
                            Platform.runLater(() -> {
                                showAlert("DFA Error", "Transition undefined for " + errState + " with input '" + c + "'", Alert.AlertType.ERROR);
                                resetControlButtons();
                            });
                            return;
                        }

                        String nextState = "q" + targetText;
                        String edgeId = currentState + "-" + nextState;

                        highlightNode(currentState, false);
                        highlightEdge(edgeId, true);
                        highlightNode(nextState, true);

                        currentState = nextState;
                        Thread.sleep(animationDelay);
                        highlightEdge(edgeId, false);
                    }

                    String finalStateReached = currentState;
                    Platform.runLater(() -> {
                        resetControlButtons();
                        if (finalStates.contains(finalStateReached)) {
                            showAlert("Result", "VALID String! (Ended at " + finalStateReached + " which is a Final State)", Alert.AlertType.INFORMATION);
                        } else {
                            showAlert("Result", "REJECTED String! (Ended at " + finalStateReached + " which is not a Final State)", Alert.AlertType.WARNING);
                        }
                    });

                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        // --- STEP-BY-STEP LOGIC: INITIALIZE ---
        btnStartStep.setOnAction(e -> {
            stepInputStr = txtTestString.getText().trim();
            if (stepInputStr.isEmpty()) {
                showAlert("Empty Input", "Please enter a string to test step-by-step.", Alert.AlertType.WARNING);
                return;
            }

            stepIndex = 0;
            stepCurrentState = startState;
            activeEdgeId = null;

            resetGraphColors();
            highlightNode(stepCurrentState, true);

            // Adjust button states for stepping mode
            btnBuild.setDisable(true);
            btnTest.setDisable(true);
            btnStartStep.setDisable(true);
            btnNextStep.setDisable(false);
        });

        // --- STEP-BY-STEP LOGIC: NEXT STEP EXECUTION ---
        btnNextStep.setOnAction(e -> {
            // Unhighlight the edge used in the previous step
            if (activeEdgeId != null) {
                highlightEdge(activeEdgeId, false);
                activeEdgeId = null;
            }

            // Check if string is fully processed
            if (stepIndex >= stepInputStr.length()) {
                resetControlButtons();
                if (finalStates.contains(stepCurrentState)) {
                    showAlert("Result", "VALID String! (Ended at " + stepCurrentState + " which is a Final State)", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Result", "REJECTED String! (Ended at " + stepCurrentState + " which is not a Final State)", Alert.AlertType.WARNING);
                }
                return;
            }

            // Process next character
            char c = stepInputStr.charAt(stepIndex);
            String charStr = String.valueOf(c);
            int charIndex = Arrays.asList(alphabets).indexOf(charStr);

            if (charIndex == -1) {
                showAlert("Test Error", "Character '" + c + "' is not in the Alphabet!", Alert.AlertType.ERROR);
                resetControlButtons();
                return;
            }

            int rowIndex = Integer.parseInt(stepCurrentState.replace("q", ""));
            String targetText = transitionCells[rowIndex][charIndex].getText().trim();

            if (targetText.isEmpty()) {
                showAlert("DFA Error", "Transition undefined for " + stepCurrentState + " with input '" + c + "'", Alert.AlertType.ERROR);
                resetControlButtons();
                return;
            }

            String nextState = "q" + targetText;
            activeEdgeId = stepCurrentState + "-" + nextState;

            // Transition UI updates
            highlightNode(stepCurrentState, false);
            highlightEdge(activeEdgeId, true);
            highlightNode(nextState, true);

            stepCurrentState = nextState;
            stepIndex++;
        });

        // --- OVERALL LAYOUT ---
        inputPanel.getChildren().addAll(
                new Label("1. Alphabet (Σ) separated by spaces:"), txtAlphabet,
                new Label("2. Total Number of States (|Q|):"), txtNumStates,
                btnGenTable,
                new Label("3. Transition Table (Enter target state):"), scrollPane,
                new Label("4. Start State (q0):"), txtStartState,
                new Label("5. Final States (F) separated by spaces:"), txtFinalStates,
                new Separator(),
                new Label("6. Input String to test:"), txtTestString,
                buildBox,
                new Separator(),
                new Label("Testing Controls:"),
                testBox
        );

        Pane graphContainer = new Pane(graphPane);
        graphContainer.setClip(new Rectangle(2000, 2000));

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(inputPanel, graphContainer);
        splitPane.setDividerPositions(0.35);

        VBox rootLayout = new VBox(menuBar, splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        Scene scene = new Scene(rootLayout, 1100, 700);
        primaryStage.setTitle("DFA Visualization Simulator (Pure JavaFX)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --- MATHEMATICAL DYNAMIC DELAY FUNCTION ---
    private long calculateAnimationDelay(int length) {
        if (length <= 0) return 1000;

        // Base time target is roughly 10,000ms (10 seconds) total for the whole string
        long delay = 10000 / length;

        // Cap the maximum delay so short strings aren't painstakingly slow
        if (delay > 1000) return 1000;

        // Cap the minimum delay so long strings don't become an unreadable blur
        if (delay < 150) return 150;

        return delay;
    }

    // --- HELPER METHOD TO UNLOCK UI CONTROLS ---
    private void resetControlButtons() {
        btnBuild.setDisable(false);
        btnTest.setDisable(false);
        btnStartStep.setDisable(false);
        btnNextStep.setDisable(true);
    }

    // --- IMPORT DATA FROM TXT VIA InputDatas ---
    private void importDataFromFile(File file) {
        try {
            InputDatas data = InputDatas.parseFromFile(file);

            txtAlphabet.setText(data.getAlphabets());
            txtNumStates.setText(data.getNumStates());

            btnGenTable.fire();

            String[] tokens = data.getTransitionTokens();
            int numStates = Integer.parseInt(data.getNumStates());
            String[] alphas = data.getAlphabets().split("\\s+");

            int tokenIndex = 0;
            for (int i = 0; i < numStates; i++) {
                for (int j = 0; j < alphas.length; j++) {
                    if (tokenIndex < tokens.length) {
                        transitionCells[i][j].setText(tokens[tokenIndex]);
                        tokenIndex++;
                    }
                }
            }

            txtStartState.setText(data.getStartState());
            txtFinalStates.setText(data.getFinalStates());
            txtTestString.setText(data.getTestString());

            btnBuild.fire();

        } catch (Exception e) {
            showAlert("File Read Error", "Cannot read file. Please check the format.\nDetails: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // --- GRAPH DRAWING AND ANIMATION METHODS ---
    private StackPane createNodeUI(String label, boolean isFinal) {
        StackPane pane = new StackPane();
        pane.setPrefSize(50, 50);

        Circle baseCircle = new Circle(25, NORMAL_COLOR);
        baseCircle.setStroke(STROKE_COLOR);
        baseCircle.setStrokeWidth(2);
        pane.getChildren().add(baseCircle);

        if (isFinal) {
            Circle innerCircle = new Circle(19, Color.TRANSPARENT);
            innerCircle.setStroke(STROKE_COLOR);
            innerCircle.setStrokeWidth(2);
            pane.getChildren().add(innerCircle);
        }

        Text text = new Text(label);
        text.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        pane.getChildren().add(text);

        return pane;
    }

    private List<Shape> drawEdge(double x1, double y1, double x2, double y2, boolean isLoop, String label) {
        List<Shape> shapes = new ArrayList<>();

        if (isLoop) {
            CubicCurve curve = new CubicCurve();
            curve.setStartX(x1 - 10); curve.setStartY(y1 - 25);
            curve.setControlX1(x1 - 50); curve.setControlY1(y1 - 100);
            curve.setControlX2(x1 + 50); curve.setControlY2(y1 - 100);
            curve.setEndX(x1 + 10); curve.setEndY(y1 - 25);
            curve.setFill(Color.TRANSPARENT);
            curve.setStroke(STROKE_COLOR);
            curve.setStrokeWidth(2);
            shapes.add(curve);

            double dirX = curve.getEndX() - curve.getControlX2();
            double dirY = curve.getEndY() - curve.getControlY2();
            Polygon arrowHead = createArrowHead(curve.getEndX(), curve.getEndY(), dirX, dirY);
            shapes.add(arrowHead);

            Text text = new Text(x1 - 10, y1 - 85, label);
            text.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            text.setFill(Color.BLACK);
            shapes.add(text);
        } else {
            QuadCurve curve = new QuadCurve();
            double midX = (x1 + x2) / 2;
            double midY = (y1 + y2) / 2;

            double vx = x2 - x1;
            double vy = y2 - y1;
            double dist = Math.sqrt(vx * vx + vy * vy);

            double nx = vy / dist;
            double ny = -vx / dist;

            double curveOffset = dist * 0.2;
            double cx = midX + nx * curveOffset;
            double cy = midY + ny * curveOffset;

            curve.setControlX(cx);
            curve.setControlY(cy);

            double nodeRadius = 28.0;

            double dx1 = cx - x1;
            double dy1 = cy - y1;
            double len1 = Math.sqrt(dx1 * dx1 + dy1 * dy1);
            double startX = x1 + (dx1 / len1) * nodeRadius;
            double startY = y1 + (dy1 / len1) * nodeRadius;

            double dx2 = x2 - cx;
            double dy2 = y2 - cy;
            double len2 = Math.sqrt(dx2 * dx2 + dy2 * dy2);
            double endX = x2 - (dx2 / len2) * nodeRadius;
            double endY = y2 - (dy2 / len2) * nodeRadius;

            curve.setStartX(startX);
            curve.setStartY(startY);
            curve.setEndX(endX);
            curve.setEndY(endY);

            curve.setFill(Color.TRANSPARENT);
            curve.setStroke(STROKE_COLOR);
            curve.setStrokeWidth(2);
            shapes.add(curve);

            double dirX = endX - cx;
            double dirY = endY - cy;
            Polygon arrowHead = createArrowHead(endX, endY, dirX, dirY);
            shapes.add(arrowHead);

            double textX = midX + nx * (curveOffset + 15);
            double textY = midY + ny * (curveOffset + 15);

            Text text = new Text(textX - 5, textY + 5, label);
            text.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            text.setFill(Color.BLACK);
            shapes.add(text);
        }
        return shapes;
    }

    private Polygon createArrowHead(double endX, double endY, double dirX, double dirY) {
        double arrowLen = 10.0;
        double arrowWidth = 7.0;

        double vLen = Math.sqrt(dirX * dirX + dirY * dirY);
        double ux = dirX / vLen;
        double uy = dirY / vLen;
        double wx = -uy;
        double wy = ux;

        double baseCenterX = endX - arrowLen * ux;
        double baseCenterY = endY - arrowLen * uy;

        double p1x = baseCenterX + (arrowWidth / 2.0) * wx;
        double p1y = baseCenterY + (arrowWidth / 2.0) * wy;
        double p2x = baseCenterX - (arrowWidth / 2.0) * wx;
        double p2y = baseCenterY - (arrowWidth / 2.0) * wy;

        Polygon triangle = new Polygon(p1x, p1y, p2x, p2y, endX, endY);
        triangle.setStroke(STROKE_COLOR);
        triangle.setStrokeWidth(1.0);
        triangle.setFill(STROKE_COLOR);
        return triangle;
    }

    private void highlightNode(String stateId, boolean isActive) {
        Platform.runLater(() -> {
            StackPane nodePane = uiNodes.get(stateId);
            if (nodePane != null) {
                Circle base = (Circle) nodePane.getChildren().get(0);
                base.setFill(isActive ? ACTIVE_COLOR : NORMAL_COLOR);
                base.setStroke(isActive ? ACTIVE_STROKE : STROKE_COLOR);
            }
        });
    }

    private void highlightEdge(String edgeId, boolean isActive) {
        Platform.runLater(() -> {
            List<Shape> shapes = uiEdges.get(edgeId);
            if (shapes != null) {
                for (Shape shape : shapes) {
                    if (shape instanceof Text) {
                        ((Text) shape).setFill(isActive ? Color.RED : Color.BLACK);
                        continue;
                    }
                    shape.setStroke(isActive ? ACTIVE_STROKE : STROKE_COLOR);
                    if (shape instanceof Polygon) {
                        shape.setFill(isActive ? ACTIVE_STROKE : STROKE_COLOR);
                    }
                }
            }
        });
    }

    private void resetGraphColors() {
        Platform.runLater(() -> {
            for (String node : uiNodes.keySet()) highlightNode(node, false);
            for (String edge : uiEdges.keySet()) highlightEdge(edge, false);
        });
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}