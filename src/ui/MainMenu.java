package ui;

import game.GameEngine;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.*;

public class MainMenu {
    private Stage stage;
    private Canvas canvas;
    private GraphicsContext gc;
    private AnimationTimer animator;
    private double time = 0;
    private List<MenuParticle> particles = new ArrayList<>();
    private List<GridLine> gridLines = new ArrayList<>();
    private Random random = new Random();

    // Cyberpunk color palette
    private static final Color NEON_CYAN = Color.web("#00ffff");
    private static final Color NEON_PINK = Color.web("#ff0080");
    private static final Color NEON_PURPLE = Color.web("#8000ff");
    private static final Color NEON_GREEN = Color.web("#00ff80");
    private static final Color NEON_YELLOW = Color.web("#ffff00");
    private static final Color NEON_RED = Color.web("#ff0040");
    private static final Color DARK_BG = Color.web("#0a0a0f");
    private static final Color GLASS_BG = Color.web("#1a1a2e", 0.8);

    public MainMenu(Stage stage) {
        this.stage = stage;
        initParticles();
        initGrid();
    }

    private void initParticles() {
        for (int i = 0; i < 150; i++) {
            particles.add(new MenuParticle());
        }
    }

    private void initGrid() {
        for (int i = 0; i < 50; i++) {
            gridLines.add(new GridLine(true, i * 30));
            gridLines.add(new GridLine(false, i * 30));
        }
    }

    public void show() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #0a0a12;");

        // Background canvas
        canvas = new Canvas(1600, 1000);
        gc = canvas.getGraphicsContext2D();
        canvas.setMouseTransparent(true); // Allow mouse events to pass through to buttons

        // Content layer
        VBox content = createContent();

        root.getChildren().addAll(canvas, content);

        Scene scene = new Scene(root, 1600, 1000);
        scene.setFill(DARK_BG);

        stage.setScene(scene);
        stage.setTitle("CHRONOGRID - Cyberpunk Edition");
        stage.setResizable(false);
        stage.show();

        startAnimation();
    }

    private VBox createContent() {
        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(60));
        content.setMaxWidth(800); // Limit width to prevent stretching

        // Title with holographic effect
        StackPane titlePane = new StackPane();

        // Outer glow
        Text titleGlow = new Text("CHRONOGRID");
        titleGlow.setFont(Font.font("Arial Black", FontWeight.BLACK, 140));
        titleGlow.setFill(NEON_CYAN);
        GaussianBlur glowBlur = new GaussianBlur(50);
        titleGlow.setEffect(glowBlur);

        // Main title
        Text title = new Text("CHRONOGRID");
        title.setFont(Font.font("Arial Black", FontWeight.BLACK, 120));
        title.setFill(NEON_CYAN);
        DropShadow titleShadow = new DropShadow(20, 0, 0, NEON_PURPLE);
        title.setEffect(titleShadow);

        // Subtitle
        Text subtitle = new Text("MULTI-TIMELINE CYBER STRATEGY");
        subtitle.setFont(Font.font("Courier New", FontWeight.BOLD, 24));
        subtitle.setFill(NEON_PINK);
        subtitle.setEffect(new Glow(0.8));

        titlePane.getChildren().addAll(titleGlow, title);
        titlePane.setTranslateY(-50);

        // Menu buttons with glass morphism
        VBox menuButtons = createMenuButtons();

        content.getChildren().addAll(titlePane, subtitle, menuButtons);

        return content;
    }

    private VBox createMenuButtons() {
        VBox buttons = new VBox(20);
        buttons.setAlignment(Pos.CENTER);

        String[] buttonTexts = {"START GAME", "SETTINGS", "ACHIEVEMENTS", "EXIT"};
        Color[] buttonColors = {NEON_CYAN, NEON_GREEN, NEON_PURPLE, NEON_PINK};

        for (int i = 0; i < buttonTexts.length; i++) {
            StackPane button = createCyberButton(buttonTexts[i], buttonColors[i]);
            
            // Add click handlers
            final int index = i;
            button.setOnMouseClicked(e -> {
                switch (index) {
                    case 0: // START GAME
                        startGame();
                        break;
                    case 1: // SETTINGS
                        showSettings();
                        break;
                    case 2: // ACHIEVEMENTS
                        showAchievements();
                        break;
                    case 3: // EXIT
                        if (animator != null) animator.stop();
                        stage.close();
                        break;
                }
            });
            
            buttons.getChildren().add(button);
        }

        return buttons;
    }

    private StackPane createCyberButton(String text, Color accentColor) {
        StackPane button = new StackPane();
        button.setPrefSize(300, 60);

        // Glass background
        Rectangle bg = new Rectangle(300, 60);
        bg.setFill(Color.web("#ffffff", 0.1));
        bg.setStroke(accentColor);
        bg.setStrokeWidth(2);
        bg.setArcWidth(15);
        bg.setArcHeight(15);

        // Inner glow
        Rectangle innerGlow = new Rectangle(290, 50);
        innerGlow.setFill(Color.TRANSPARENT);
        innerGlow.setStroke(accentColor);
        innerGlow.setStrokeWidth(1);
        innerGlow.setArcWidth(12);
        innerGlow.setArcHeight(12);
        innerGlow.setEffect(new Glow(0.5));

        // Button text
        Text buttonText = new Text(text);
        buttonText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        buttonText.setFill(accentColor);

        // Hover effects
        button.setOnMouseEntered(e -> {
            bg.setFill(Color.web("#ffffff", 0.2));
            buttonText.setEffect(new Glow(0.8));
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), button);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
        });

        button.setOnMouseExited(e -> {
            bg.setFill(Color.web("#ffffff", 0.1));
            buttonText.setEffect(null);
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), button);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });

        button.getChildren().addAll(bg, innerGlow, buttonText);
        return button;
    }

    private Rectangle createGradientLine(double width, boolean leftToRight) {
        Rectangle line = new Rectangle(width, 2);
        LinearGradient gradient;
        if (leftToRight) {
            gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT),
                new Stop(1, Color.web("#00ffff"))
            );
        } else {
            gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#00ffff")),
                new Stop(1, Color.TRANSPARENT)
            );
        }
        line.setFill(gradient);
        return line;
    }

    private StackPane createMenuButton(String text, String color, boolean isPrimary) {
        StackPane btn = new StackPane();
        btn.setPrefSize(400, isPrimary ? 70 : 55);
        btn.setCursor(javafx.scene.Cursor.HAND);

        Rectangle bg = new Rectangle(400, isPrimary ? 70 : 55);
        bg.setArcWidth(isPrimary ? 35 : 27);
        bg.setArcHeight(isPrimary ? 35 : 27);
        bg.setFill(Color.web(color, 0.1));
        bg.setStroke(Color.web(color, 0.8));
        bg.setStrokeWidth(isPrimary ? 3 : 2);

        Text label = new Text(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, isPrimary ? 24 : 18));
        label.setFill(Color.web(color));

        if (isPrimary) {
            DropShadow glow = new DropShadow();
            glow.setColor(Color.web(color, 0.6));
            glow.setRadius(25);
            bg.setEffect(glow);
        }

        btn.getChildren().addAll(bg, label);

        // Hover effects
        btn.setOnMouseEntered(e -> {
            bg.setFill(Color.web(color, 0.25));
            bg.setStrokeWidth(isPrimary ? 4 : 3);
            ScaleTransition st = new ScaleTransition(Duration.millis(100), btn);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });

        btn.setOnMouseExited(e -> {
            bg.setFill(Color.web(color, 0.1));
            bg.setStrokeWidth(isPrimary ? 3 : 2);
            ScaleTransition st = new ScaleTransition(Duration.millis(100), btn);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        return btn;
    }

    private void startAnimation() {
        animator = new AnimationTimer() {
            @Override
            public void handle(long now) {
                time += 0.02;
                render();
            }
        };
        animator.start();
    }

    private void render() {
        // Background gradient
        LinearGradient bg = new LinearGradient(0, 0, 0.5, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#000814")),
            new Stop(0.5, Color.web("#001d3d")),
            new Stop(1, Color.web("#000814"))
        );
        gc.setFill(bg);
        gc.fillRect(0, 0, 1600, 1000);

        // Grid
        gc.setStroke(Color.web("#00ffff", 0.05));
        gc.setLineWidth(1);
        double offset = (time * 30) % 50;
        for (int i = -1; i < 35; i++) {
            gc.strokeLine(i * 50 + offset, 0, i * 50 + offset, 1000);
        }
        for (int i = -1; i < 25; i++) {
            gc.strokeLine(0, i * 50 + offset, 1600, i * 50 + offset);
        }

        // Particles
        for (MenuParticle p : particles) {
            p.update();
            gc.setFill(Color.web(p.color, p.alpha));
            gc.fillOval(p.x - p.size/2, p.y - p.size/2, p.size, p.size);
            
            if (p.size > 2) {
                gc.setFill(Color.web(p.color, p.alpha * 0.3));
                gc.fillOval(p.x - p.size, p.y - p.size, p.size * 2, p.size * 2);
            }
        }

        // Horizontal scan line
        double scanY = (time * 100) % 1200 - 100;
        gc.setFill(Color.web("#00ffff", 0.1));
        gc.fillRect(0, scanY, 1600, 3);
        gc.setFill(Color.web("#00ffff", 0.02));
        gc.fillRect(0, scanY - 20, 1600, 40);
    }

    private void startGame() {
        if (animator != null) animator.stop();
        GameEngine engine = new GameEngine();
        GameView view = new GameView(engine, stage);
        view.show();
    }

    private void showSettings() {
        // Create settings panel
        VBox settingsPanel = new VBox(20);
        settingsPanel.setAlignment(Pos.CENTER);
        settingsPanel.setPadding(new Insets(40));
        settingsPanel.setStyle("-fx-background-color: rgba(26, 26, 46, 0.95); -fx-background-radius: 20; -fx-border-color: rgba(0, 255, 255, 0.5); -fx-border-width: 2; -fx-border-radius: 20;");
        settingsPanel.setMaxWidth(500);
        settingsPanel.setMaxHeight(600);
        settingsPanel.setPrefWidth(Math.min(500, stage.getWidth() * 0.6));
        settingsPanel.setPrefHeight(Math.min(600, stage.getHeight() * 0.8));

        // Title
        Text title = new Text("SETTINGS");
        title.setFont(Font.font("Arial Black", FontWeight.BLACK, 36));
        title.setFill(NEON_CYAN);
        title.setEffect(new DropShadow(10, NEON_PURPLE));

        // Settings options
        VBox options = new VBox(15);
        options.setAlignment(Pos.CENTER_LEFT);

        // Sound toggle
        HBox soundRow = createSettingRow("SOUND EFFECTS", true);
        HBox musicRow = createSettingRow("BACKGROUND MUSIC", true);
        HBox particlesRow = createSettingRow("PARTICLE EFFECTS", true);

        // Difficulty selector
        HBox difficultyRow = new HBox(20);
        difficultyRow.setAlignment(Pos.CENTER_LEFT);
        Text diffLabel = new Text("DIFFICULTY:");
        diffLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 16));
        diffLabel.setFill(NEON_GREEN);

        HBox diffButtons = new HBox(10);
        String[] difficulties = {"EASY", "NORMAL", "HARD", "EXTREME"};
        Color[] diffColors = {NEON_GREEN, NEON_CYAN, NEON_YELLOW, NEON_RED};

        for (int i = 0; i < difficulties.length; i++) {
            StackPane diffBtn = createSmallButton(difficulties[i], diffColors[i]);
            final int diffIndex = i;
            diffBtn.setOnMouseClicked(e -> {
                // TODO: Save difficulty setting
                System.out.println("Difficulty set to: " + difficulties[diffIndex]);
            });
            diffButtons.getChildren().add(diffBtn);
        }

        difficultyRow.getChildren().addAll(diffLabel, diffButtons);

        options.getChildren().addAll(soundRow, musicRow, particlesRow, difficultyRow);

        // Back button
        StackPane backButton = createCyberButton("BACK TO MENU", NEON_PINK);
        backButton.setOnMouseClicked(e -> {
            // Return to main menu by recreating the scene
            show();
        });

        settingsPanel.getChildren().addAll(title, options, backButton);

        // Show settings - center the panel properly
        StackPane root = (StackPane) stage.getScene().getRoot();
        root.getChildren().clear();
        root.getChildren().addAll(canvas, settingsPanel);
        StackPane.setAlignment(settingsPanel, Pos.CENTER);
    }

    private void showAchievements() {
        // Create achievements panel
        VBox achievementsPanel = new VBox(20);
        achievementsPanel.setAlignment(Pos.TOP_CENTER);
        achievementsPanel.setPadding(new Insets(40));
        achievementsPanel.setStyle("-fx-background-color: rgba(26, 26, 46, 0.95); -fx-background-radius: 20; -fx-border-color: rgba(0, 255, 255, 0.5); -fx-border-width: 2; -fx-border-radius: 20;");
        achievementsPanel.setMaxWidth(700);
        achievementsPanel.setMaxHeight(600);
        achievementsPanel.setPrefWidth(Math.min(700, stage.getWidth() * 0.8));
        achievementsPanel.setPrefHeight(Math.min(600, stage.getHeight() * 0.8));

        // Title
        Text title = new Text("ACHIEVEMENTS");
        title.setFont(Font.font("Arial Black", FontWeight.BLACK, 36));
        title.setFill(NEON_PURPLE);
        title.setEffect(new DropShadow(10, NEON_CYAN));

        // Achievement list
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);

        VBox achievementList = new VBox(10);
        achievementList.setAlignment(Pos.TOP_CENTER);
        achievementList.setPadding(new Insets(20));

        // Sample achievements
        String[][] achievements = {
            {"FIRST BLOOD", "Defeat your first enemy", "true"},
            {"TIMELINE MASTER", "Complete 10 timeline shifts", "false"},
            {"SPEED RUNNER", "Complete a level in under 2 minutes", "false"},
            {"BOSS SLAYER", "Defeat 5 multi-phase bosses", "true"},
            {"SURVIVOR", "Survive for 10 minutes in extreme mode", "false"},
            {"COLLECTOR", "Gather 100 timeline artifacts", "true"},
            {"STRATEGIST", "Win without taking damage", "false"},
            {"EXPLORER", "Discover all map areas", "false"}
        };

        for (String[] achievement : achievements) {
            achievementList.getChildren().add(createAchievementItem(achievement[0], achievement[1], Boolean.parseBoolean(achievement[2])));
        }

        scrollPane.setContent(achievementList);

        // Back button
        StackPane backButton = createCyberButton("BACK TO MENU", NEON_PINK);
        backButton.setOnMouseClicked(e -> {
            // Return to main menu by recreating the scene
            show();
        });

        achievementsPanel.getChildren().addAll(title, scrollPane, backButton);

        // Show achievements - center the panel properly
        StackPane root = (StackPane) stage.getScene().getRoot();
        root.getChildren().clear();
        root.getChildren().addAll(canvas, achievementsPanel);
        StackPane.setAlignment(achievementsPanel, Pos.CENTER);
    }

    private HBox createSettingRow(String label, boolean enabled) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);

        Text labelText = new Text(label);
        labelText.setFont(Font.font("Courier New", FontWeight.BOLD, 16));
        labelText.setFill(NEON_CYAN);

        HBox toggleButtons = new HBox(5);
        StackPane onBtn = createSmallButton("ON", enabled ? NEON_GREEN : Color.GRAY);
        StackPane offBtn = createSmallButton("OFF", !enabled ? NEON_RED : Color.GRAY);

        onBtn.setOnMouseClicked(e -> {
            onBtn.getChildren().get(1).setStyle("-fx-fill: " + toHexString(NEON_GREEN));
            offBtn.getChildren().get(1).setStyle("-fx-fill: gray");
            // TODO: Save setting
        });

        offBtn.setOnMouseClicked(e -> {
            offBtn.getChildren().get(1).setStyle("-fx-fill: " + toHexString(NEON_RED));
            onBtn.getChildren().get(1).setStyle("-fx-fill: gray");
            // TODO: Save setting
        });

        toggleButtons.getChildren().addAll(onBtn, offBtn);
        row.getChildren().addAll(labelText, toggleButtons);

        return row;
    }

    private StackPane createSmallButton(String text, Color color) {
        StackPane button = new StackPane();
        button.setPrefSize(80, 30);

        Rectangle bg = new Rectangle(80, 30);
        bg.setFill(Color.web("#ffffff", 0.1));
        bg.setStroke(color);
        bg.setStrokeWidth(1);
        bg.setArcWidth(8);
        bg.setArcHeight(8);

        Text buttonText = new Text(text);
        buttonText.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        buttonText.setFill(color);

        button.setOnMouseEntered(e -> {
            bg.setFill(Color.web("#ffffff", 0.2));
            buttonText.setEffect(new Glow(0.5));
        });

        button.setOnMouseExited(e -> {
            bg.setFill(Color.web("#ffffff", 0.1));
            buttonText.setEffect(null);
        });

        button.getChildren().addAll(bg, buttonText);
        return button;
    }

    private HBox createAchievementItem(String name, String description, boolean unlocked) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(15));
        item.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-background-radius: 10;");

        // Achievement icon/status
        StackPane icon = new StackPane();
        Rectangle iconBg = new Rectangle(40, 40);
        iconBg.setArcWidth(8);
        iconBg.setArcHeight(8);
        iconBg.setFill(unlocked ? NEON_GREEN : Color.web("#333"));
        iconBg.setStroke(unlocked ? NEON_CYAN : Color.web("#666"));
        iconBg.setStrokeWidth(2);

        Text iconText = new Text(unlocked ? "✓" : "?");
        iconText.setFont(Font.font("Arial Black", FontWeight.BLACK, 20));
        iconText.setFill(unlocked ? Color.WHITE : Color.web("#888"));

        icon.getChildren().addAll(iconBg, iconText);

        // Achievement details
        VBox details = new VBox(5);
        details.setAlignment(Pos.CENTER_LEFT);

        Text nameText = new Text(name);
        nameText.setFont(Font.font("Arial Black", FontWeight.BLACK, 16));
        nameText.setFill(unlocked ? NEON_CYAN : Color.web("#666"));

        Text descText = new Text(description);
        descText.setFont(Font.font("Courier New", 12));
        descText.setFill(unlocked ? Color.web("#ccc") : Color.web("#888"));

        details.getChildren().addAll(nameText, descText);

        item.getChildren().addAll(icon, details);
        return item;
    }

    private class MenuParticle {
        double x, y, vx, vy, size, alpha;
        String color;

        MenuParticle() {
            reset();
            y = random.nextDouble() * 1000;
        }

        void reset() {
            x = random.nextDouble() * 1600;
            y = -20;
            vx = (random.nextDouble() - 0.5) * 0.5;
            vy = random.nextDouble() * 2 + 0.5;
            size = random.nextDouble() * 4 + 1;
            alpha = random.nextDouble() * 0.5 + 0.2;
            String[] colors = {"#00ffff", "#0088ff", "#00ff88", "#ffffff"};
            color = colors[random.nextInt(colors.length)];
        }

        void update() {
            x += vx;
            y += vy;
            if (y > 1020) reset();
        }
    }

    private class GridLine {
        boolean horizontal;
        double pos;

        GridLine(boolean h, double p) {
            horizontal = h;
            pos = p;
        }
    }

    private String toHexString(Color color) {
        return String.format("#%02x%02x%02x",
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255));
    }
}
