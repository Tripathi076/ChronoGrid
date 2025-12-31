package ui;

import game.GameEngine;
import game.GameEngine.*;
import map.GridMap;
import map.Node;
import player.Player;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.*;

public class GameView {
    private GameEngine engine;
    private Stage stage;
    private Canvas gameCanvas;
    private GraphicsContext gc;
    private Player player;
    private AnimationTimer gameLoop;
    
    // Screen - Optimized for 1920x1080
    private static final int GAME_WIDTH = 1200;
    private static final int GAME_HEIGHT = 850;
    private static final double TILE_SIZE = 36;
    
    // Camera
    private double cameraX = 0, cameraY = 0;
    
    // Effects
    private double shakeX = 0, shakeY = 0, shakeIntensity = 0;
    private List<ScreenEffect> screenEffects = new ArrayList<>();
    private List<FloatingText> floatingTexts = new ArrayList<>();
    private List<ParticleEffect> particleEffects = new ArrayList<>();
    private List<Notification> notifications = new ArrayList<>();
    
    // Enhanced Features
    private MiniMap miniMap;
    private TimelineShiftIndicator timelineIndicator;
    private ComboSystem comboSystem;
    private PowerUpDisplay powerUpDisplay;
    private KillStreakSystem killStreakSystem;
    private BossHealthBar bossHealthBar;
    private DamageIndicatorSystem damageIndicators;
    
    // Input
    private Set<KeyCode> keys = new HashSet<>();
    private double mouseX, mouseY;
    private boolean mouseDown = false;
    
    // HUD Elements for new design
    private Text levelText, timelineText, waveText, scoreText;
    private Text killsText, fpsText, waveProgressText, gameTimeText;
    private ProgressIndicator healthBar, energyBar, ammoBar;
    private Canvas strategicMapCanvas, threatLevelCanvas;
    private VBox notificationArea;
    private VBox dashCooldownBar, ultimateCooldownBar;
    
    // State
    private String currentTimeline = "PRESENT";
    private double time = 0;
    private long lastShot = 0;
    private boolean gameOver = false;
    private boolean paused = false;
    
    // NEW: Performance & Stats tracking
    private long frameCount = 0;
    private long lastFpsTime = System.nanoTime();
    private int currentFps = 60;
    private int totalKills = 0;
    private int currentWaveKills = 0;
    private int waveKillsRequired = 5;
    private long gameStartTime = System.currentTimeMillis();
    private long lastDashTime = 0;
    private long lastUltimateTime = 0;
    private static final long DASH_COOLDOWN = 2000; // 2 seconds
    private static final long ULTIMATE_COOLDOWN = 15000; // 15 seconds

    // Cyberpunk color palette
    private static final Color NEON_CYAN = Color.web("#00ffff");
    private static final Color NEON_PINK = Color.web("#ff0080");
    private static final Color NEON_PURPLE = Color.web("#8000ff");
    private static final Color NEON_GREEN = Color.web("#00ff80");
    private static final Color NEON_YELLOW = Color.web("#ffff00");
    private static final Color NEON_RED = Color.web("#ff0040");
    private static final Color DARK_BG = Color.web("#0a0a0f");
    private static final Color NEON_ORANGE = Color.web("#ff6600");
    private static final Color NEON_GOLD = Color.web("#ffd700");

    public GameView(GameEngine engine, Stage stage) {
        this.engine = engine;
        this.stage = stage;
        this.player = new Player("Hero", engine.getMap(), "PRESENT");
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0d1117;");

        // === TOP HUD - Like reference image ===
        HBox topHUD = createTopHUD();
        
        // === CENTER: Game Area ===
        StackPane gameArea = new StackPane();
        gameArea.setStyle("-fx-background-color: transparent;");
        gameArea.setPadding(new Insets(10));
        
        gameCanvas = new Canvas(GAME_WIDTH, GAME_HEIGHT);
        gc = gameCanvas.getGraphicsContext2D();
        
        // Game frame with subtle border
        StackPane canvasHolder = new StackPane();
        canvasHolder.setStyle("-fx-background-color: #0a0f14; -fx-border-color: #1e3a5f; -fx-border-width: 2; -fx-border-radius: 4; -fx-background-radius: 4;");
        canvasHolder.setPadding(new Insets(4));
        canvasHolder.getChildren().add(gameCanvas);
        canvasHolder.setMaxSize(GAME_WIDTH + 10, GAME_HEIGHT + 10);
        
        gameArea.getChildren().add(canvasHolder);
        
        // === RIGHT PANEL - Strategic Map & Threat Level ===
        VBox rightPanel = createRightPanel();
        
        // === BOTTOM HUD (hidden, but create bars for tracking) ===
        HBox bottomHUD = createBottomHUD();
        
        // Notification area
        notificationArea = new VBox(10);
        notificationArea.setAlignment(Pos.TOP_CENTER);
        notificationArea.setPadding(new Insets(20));
        notificationArea.setPickOnBounds(false);
        notificationArea.setMouseTransparent(true);
        
        StackPane centerStack = new StackPane();
        centerStack.getChildren().addAll(gameArea, notificationArea);
        StackPane.setAlignment(notificationArea, Pos.TOP_CENTER);
        
        root.setTop(topHUD);
        root.setCenter(centerStack);
        root.setRight(rightPanel);
        root.setBottom(bottomHUD);

        // Fit to 1920x1080 screen with some margin
        Scene scene = new Scene(root, 1880, 1000);
        scene.setFill(Color.web("#0d1117"));
        
        // Make window maximized/fullscreen friendly
        stage.setMaximized(true);
        
        // Input handlers
        scene.setOnKeyPressed(e -> keys.add(e.getCode()));
        scene.setOnKeyReleased(e -> keys.remove(e.getCode()));
        scene.setOnMouseMoved(e -> updateMouse(e.getSceneX(), e.getSceneY()));
        scene.setOnMouseDragged(e -> updateMouse(e.getSceneX(), e.getSceneY()));
        scene.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) mouseDown = true;
            if (e.getButton() == MouseButton.SECONDARY) doDash();
        });
        scene.setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.PRIMARY) mouseDown = false;
        });
        
        stage.setScene(scene);
        stage.setTitle("CHRONOGRID - Timeline Warfare");
        
        // Initialize enhanced features
        initEnhancedFeatures();
        
        startGameLoop();
    }
    
    private void updateMouse(double sx, double sy) {
        // Convert scene coords to game canvas coords
        double canvasLeft = 50;
        double canvasTop = 80;
        mouseX = sx - canvasLeft;
        mouseY = sy - canvasTop;
        
        // Calculate aim angle
        double playerScreenX = player.getVisualX() * TILE_SIZE - cameraX + TILE_SIZE/2;
        double playerScreenY = player.getVisualY() * TILE_SIZE - cameraY + TILE_SIZE/2;
        player.setAimAngle(Math.atan2(mouseY - playerScreenY, mouseX - playerScreenX));
    }
    
    private HBox createTopHUD() {
        HBox hud = new HBox(15);
        hud.setAlignment(Pos.CENTER_LEFT);
        hud.setPadding(new Insets(15, 20, 15, 20));
        hud.setStyle("-fx-background-color: #0d1117;");
        
        // === LEVEL BOX (Blue border) ===
        VBox levelBox = new VBox(2);
        levelBox.setAlignment(Pos.CENTER);
        levelBox.setPadding(new Insets(8, 20, 8, 20));
        levelBox.setStyle("-fx-background-color: #0a1628; -fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");
        
        Text levelLabel = new Text("LEVEL 1");
        levelLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        levelLabel.setFill(Color.WHITE);
        levelText = levelLabel;
        
        Text waveLabel = new Text("Wave 1");
        waveLabel.setFont(Font.font("Arial", 11));
        waveLabel.setFill(Color.web("#6b7280"));
        waveText = waveLabel;
        
        levelBox.getChildren().addAll(levelLabel, waveLabel);
        
        // === TIMELINE BOX (Cyan border) ===
        VBox timelineBox = new VBox(2);
        timelineBox.setAlignment(Pos.CENTER);
        timelineBox.setPadding(new Insets(8, 15, 8, 15));
        timelineBox.setStyle("-fx-background-color: #0a1628; -fx-border-color: #06b6d4; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");
        
        Text tlLabel = new Text("◄► TIMELINE");
        tlLabel.setFont(Font.font("Arial", 10));
        tlLabel.setFill(Color.web("#06b6d4"));
        
        Text tlValue = new Text("PRESENT");
        tlValue.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        tlValue.setFill(Color.web("#10b981"));
        timelineText = tlValue;
        
        timelineBox.getChildren().addAll(tlLabel, tlValue);
        
        // === SPACER ===
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        
        // === HEALTH BAR (Red) ===
        StackPane healthBarBox = createHUDBar("#dc2626", "#7f1d1d", 200);
        
        // === ENERGY BAR (Blue/Purple) ===
        StackPane energyBarBox = createHUDBar("#6366f1", "#312e81", 200);
        
        // === SCORE BOX (Green) ===
        HBox scoreBox = new HBox(10);
        scoreBox.setAlignment(Pos.CENTER);
        scoreBox.setPadding(new Insets(8, 20, 8, 20));
        scoreBox.setStyle("-fx-background-color: #052e16; -fx-border-color: #22c55e; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");
        
        Text scoreValue = new Text("0");
        scoreValue.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        scoreValue.setFill(Color.web("#22c55e"));
        scoreText = scoreValue;
        
        scoreBox.getChildren().add(scoreValue);
        
        hud.getChildren().addAll(levelBox, timelineBox, spacer1, healthBarBox, energyBarBox, scoreBox);
        return hud;
    }
    
    private StackPane createHUDBar(String fillColor, String bgColor, double width) {
        StackPane barContainer = new StackPane();
        barContainer.setPrefSize(width, 35);
        barContainer.setStyle("-fx-background-color: " + bgColor + "; -fx-border-color: " + fillColor + "; -fx-border-width: 2; -fx-border-radius: 6; -fx-background-radius: 6;");
        
        // Inner fill bar (will be updated in updateHUD)
        Rectangle fill = new Rectangle(width - 10, 20);
        fill.setFill(Color.web(fillColor));
        fill.setArcWidth(4);
        fill.setArcHeight(4);
        
        barContainer.getChildren().add(fill);
        return barContainer;
    }
    
    private HBox createBottomHUD() {
        // Hidden - just for tracking internally
        HBox hud = new HBox();
        hud.setVisible(false);
        hud.setManaged(false);
        return hud;
    }
    
    private VBox createCyberResourceBar(String name, Color accentColor, int max) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(200);
        box.setPadding(new Insets(10));
        
        Text label = new Text(name);
        label.setFont(Font.font("Courier New", FontWeight.BOLD, 10));
        label.setFill(accentColor);
        
        ProgressIndicator bar = new ProgressIndicator(accentColor, 180, 16);
        
        Text valueText = new Text(max + "/" + max);
        valueText.setFont(Font.font("Consolas", 11));
        valueText.setFill(Color.web("#cccccc"));
        
        box.getChildren().addAll(label, bar, valueText);
        return box;
    }
    
    private VBox createRightPanel() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(320);
        panel.setMaxWidth(320);
        panel.setStyle("-fx-background-color: #0d1117;");
        
        // === GAME STATS SECTION ===
        VBox statsSection = new VBox(6);
        statsSection.setStyle("-fx-background-color: #0a1628; -fx-border-color: #1e3a5f; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");
        statsSection.setPadding(new Insets(10));
        
        HBox statsHeader = new HBox(8);
        statsHeader.setAlignment(Pos.CENTER_LEFT);
        Text statsIcon = new Text("📊");
        statsIcon.setFont(Font.font(14));
        Text statsTitle = new Text("GAME STATS");
        statsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        statsTitle.setFill(Color.web("#a855f7"));
        statsHeader.getChildren().addAll(statsIcon, statsTitle);
        
        // Kills display
        HBox killsRow = createStatRow("☠ KILLS:", "0", "#ef4444");
        killsText = (Text) ((HBox)killsRow).getChildren().get(1);
        
        // FPS display
        HBox fpsRow = createStatRow("⚡ FPS:", "60", "#22c55e");
        fpsText = (Text) ((HBox)fpsRow).getChildren().get(1);
        
        // Wave Progress
        HBox waveRow = createStatRow("🌊 WAVE:", "0/5", "#06b6d4");
        waveProgressText = (Text) ((HBox)waveRow).getChildren().get(1);
        
        // Game Time
        HBox timeRow = createStatRow("⏱ TIME:", "00:00", "#fbbf24");
        gameTimeText = (Text) ((HBox)timeRow).getChildren().get(1);
        
        statsSection.getChildren().addAll(statsHeader, killsRow, fpsRow, waveRow, timeRow);
        
        // === COOLDOWNS SECTION ===
        VBox cooldownSection = new VBox(6);
        cooldownSection.setStyle("-fx-background-color: #0a1628; -fx-border-color: #1e3a5f; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");
        cooldownSection.setPadding(new Insets(10));
        
        HBox cdHeader = new HBox(8);
        cdHeader.setAlignment(Pos.CENTER_LEFT);
        Text cdIcon = new Text("⏳");
        cdIcon.setFont(Font.font(14));
        Text cdTitle = new Text("COOLDOWNS");
        cdTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        cdTitle.setFill(Color.web("#06b6d4"));
        cdHeader.getChildren().addAll(cdIcon, cdTitle);
        
        // Dash cooldown bar
        dashCooldownBar = createCooldownBar("DASH [RMB]", "#00ffff");
        
        // Ultimate cooldown bar
        ultimateCooldownBar = createCooldownBar("ULTIMATE [Q]", "#ffd700");
        
        cooldownSection.getChildren().addAll(cdHeader, dashCooldownBar, ultimateCooldownBar);
        
        // === STRATEGIC MAP ===
        VBox mapSection = new VBox(8);
        mapSection.setStyle("-fx-background-color: #0a1628; -fx-border-color: #1e3a5f; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");
        mapSection.setPadding(new Insets(10));
        
        HBox mapHeader = new HBox(8);
        mapHeader.setAlignment(Pos.CENTER_LEFT);
        Text mapIcon = new Text("📡");
        mapIcon.setFont(Font.font(14));
        Text mapTitle = new Text("STRATEGIC MAP");
        mapTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        mapTitle.setFill(Color.web("#06b6d4"));
        mapHeader.getChildren().addAll(mapIcon, mapTitle);
        
        strategicMapCanvas = new Canvas(290, 160);
        drawStrategicMap(strategicMapCanvas.getGraphicsContext2D());
        
        mapSection.getChildren().addAll(mapHeader, strategicMapCanvas);
        
        // === THREAT LEVEL ===
        VBox threatSection = new VBox(8);
        threatSection.setStyle("-fx-background-color: #0a1628; -fx-border-color: #1e3a5f; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");
        threatSection.setPadding(new Insets(10));
        
        HBox threatHeader = new HBox(8);
        threatHeader.setAlignment(Pos.CENTER_LEFT);
        Text threatIcon = new Text("⚠");
        threatIcon.setFont(Font.font(14));
        threatIcon.setFill(Color.web("#ef4444"));
        Text threatTitle = new Text("THREAT LEVEL");
        threatTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        threatTitle.setFill(Color.web("#ef4444"));
        threatHeader.getChildren().addAll(threatIcon, threatTitle);
        
        threatLevelCanvas = new Canvas(290, 120);
        drawThreatLevel(threatLevelCanvas.getGraphicsContext2D());
        
        threatSection.getChildren().addAll(threatHeader, threatLevelCanvas);
        
        panel.getChildren().addAll(statsSection, cooldownSection, mapSection, threatSection);
        return panel;
    }
    
    private HBox createStatRow(String label, String value, String color) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        
        Text labelText = new Text(label + " ");
        labelText.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
        labelText.setFill(Color.web("#888888"));
        
        Text valueText = new Text(value);
        valueText.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
        valueText.setFill(Color.web(color));
        
        row.getChildren().addAll(labelText, valueText);
        return row;
    }
    
    private VBox createCooldownBar(String name, String color) {
        VBox box = new VBox(3);
        
        Text label = new Text(name);
        label.setFont(Font.font("Consolas", 10));
        label.setFill(Color.web("#888888"));
        
        StackPane barBg = new StackPane();
        barBg.setPrefSize(280, 14);
        barBg.setStyle("-fx-background-color: #1a2a35; -fx-background-radius: 6;");
        
        Rectangle fill = new Rectangle(280, 14);
        fill.setFill(Color.web(color));
        fill.setArcWidth(6);
        fill.setArcHeight(6);
        
        barBg.getChildren().add(fill);
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);
        
        box.getChildren().addAll(label, barBg);
        box.setUserData(fill); // Store reference to fill rectangle
        return box;
    }
    
    private void drawStrategicMap(GraphicsContext mgc) {
        double mapWidth = 290, mapHeight = 160;
        
        // Dark background
        mgc.setFill(Color.web("#0f172a"));
        mgc.fillRect(0, 0, mapWidth, mapHeight);
        
        GridMap map = engine.getMap();
        double scale = mapWidth / map.getSize();
        double scaleY = mapHeight / map.getSize();
        
        // Draw walls
        for (int i = 0; i < map.getSize(); i++) {
            for (int j = 0; j < map.getSize(); j++) {
                if (!map.getNode(i, j).isWalkable()) {
                    mgc.setFill(Color.web("#1e3a5f"));
                    mgc.fillRect(i * scale, j * scaleY, scale, scaleY);
                }
            }
        }
        
        // Draw collectibles as colored dots
        for (Collectible c : engine.getCollectibles()) {
            String color;
            switch (c.type) {
                case HEALTH: color = "#ef4444"; break;
                case ENERGY: color = "#fbbf24"; break;
                case AMMO: color = "#06b6d4"; break;
                default: color = "#a855f7";
            }
            mgc.setFill(Color.web(color));
            mgc.fillOval(c.x * scale - 2, c.y * scaleY - 2, 5, 5);
        }
        
        // Draw enemies as colored dots
        for (Enemy e : engine.getEnemies()) {
            String color;
            switch (e.type) {
                case CHASER: color = "#10b981"; break;
                case SHOOTER: color = "#fbbf24"; break;
                case TANK: color = "#6b7280"; break;
                case BOSS: color = "#ef4444"; break;
                default: color = "#f97316";
            }
            mgc.setFill(Color.web(color));
            mgc.fillOval(e.x * scale - 3, e.y * scaleY - 3, 7, 7);
        }
        
        // Draw player (yellow with glow effect)
        double px = player.getX() * scale;
        double py = player.getY() * scaleY;
        mgc.setFill(Color.web("#fbbf24", 0.3));
        mgc.fillOval(px - 8, py - 8, 16, 16);
        mgc.setFill(Color.web("#fbbf24"));
        mgc.fillOval(px - 4, py - 4, 8, 8);
        
        // Border
        mgc.setStroke(Color.web("#1e3a5f"));
        mgc.setLineWidth(1);
        mgc.strokeRect(0, 0, mapWidth, mapHeight);
    }
    
    private void drawThreatLevel(GraphicsContext tgc) {
        double mapWidth = 290, mapHeight = 120;
        
        // Dark background
        tgc.setFill(Color.web("#0f172a"));
        tgc.fillRect(0, 0, mapWidth, mapHeight);
        
        // Create a threat heat map grid
        int gridSize = 12;
        double cellW = mapWidth / gridSize;
        double cellH = mapHeight / gridSize;
        
        GridMap map = engine.getMap();
        double mapScale = (double) map.getSize() / gridSize;
        
        // Calculate threat levels for each cell
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                double centerX = (i + 0.5) * mapScale;
                double centerY = (j + 0.5) * mapScale;
                
                // Count nearby enemies and their threat
                double threat = 0;
                for (Enemy e : engine.getEnemies()) {
                    double dist = Math.sqrt(Math.pow(e.x - centerX, 2) + Math.pow(e.y - centerY, 2));
                    if (dist < 5) {
                        double threatValue = 1.0;
                        if (e.type == EnemyType.BOSS) threatValue = 3.0;
                        else if (e.type == EnemyType.TANK) threatValue = 2.0;
                        else if (e.type == EnemyType.SHOOTER) threatValue = 1.5;
                        threat += threatValue * (5 - dist) / 5;
                    }
                }
                
                // Draw cell with threat color
                if (threat > 0) {
                    Color threatColor;
                    if (threat > 3) threatColor = Color.web("#ef4444"); // Red - high
                    else if (threat > 2) threatColor = Color.web("#f97316"); // Orange
                    else if (threat > 1) threatColor = Color.web("#fbbf24"); // Yellow
                    else if (threat > 0.5) threatColor = Color.web("#06b6d4"); // Cyan - low
                    else threatColor = Color.web("#3b82f6"); // Blue - minimal
                    
                    tgc.setFill(threatColor);
                    tgc.fillRect(i * cellW + 1, j * cellH + 1, cellW - 2, cellH - 2);
                }
            }
        }
        
        // Draw grid lines
        tgc.setStroke(Color.web("#1e3a5f", 0.5));
        tgc.setLineWidth(1);
        for (int i = 0; i <= gridSize; i++) {
            tgc.strokeLine(i * cellW, 0, i * cellW, mapHeight);
            tgc.strokeLine(0, i * cellH, mapWidth, i * cellH);
        }
        
        // Border
        tgc.setStroke(Color.web("#1e3a5f"));
        tgc.setLineWidth(1);
        tgc.strokeRect(0, 0, mapWidth, mapHeight);
    }
    
    private void startGameLoop() {
        gameStartTime = System.currentTimeMillis();
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!gameOver && !paused) {
                    time += 0.016;
                    frameCount++;
                    
                    // Calculate FPS every second
                    if (now - lastFpsTime >= 1_000_000_000) {
                        currentFps = (int) frameCount;
                        frameCount = 0;
                        lastFpsTime = now;
                    }
                    
                    processInput();
                    update();
                    updateEnhancedFeatures();
                    render();
                    renderEnhancedFeatures();
                    updateHUD();
                }
            }
        };
        gameLoop.start();
    }
    
    private void processInput() {
        // Pause toggle
        if (keys.contains(KeyCode.P) || keys.contains(KeyCode.ESCAPE)) {
            keys.remove(KeyCode.P);
            keys.remove(KeyCode.ESCAPE);
            togglePause();
            return;
        }
        
        if (paused) return;
        
        double dx = 0, dy = 0;
        if (keys.contains(KeyCode.W) || keys.contains(KeyCode.UP)) dy = -1;
        if (keys.contains(KeyCode.S) || keys.contains(KeyCode.DOWN)) dy = 1;
        if (keys.contains(KeyCode.A) || keys.contains(KeyCode.LEFT)) dx = -1;
        if (keys.contains(KeyCode.D) || keys.contains(KeyCode.RIGHT)) dx = 1;
        
        if (dx != 0 || dy != 0) {
            double len = Math.sqrt(dx*dx + dy*dy);
            player.move(dx/len, dy/len);
        }
        
        if (keys.contains(KeyCode.DIGIT1)) switchTimeline("PAST");
        if (keys.contains(KeyCode.DIGIT2)) switchTimeline("PRESENT");
        if (keys.contains(KeyCode.DIGIT3)) switchTimeline("FUTURE");
        if (keys.contains(KeyCode.Q)) doUltimate();
        if (keys.contains(KeyCode.R)) quickRestart(); // Quick restart
        if (keys.contains(KeyCode.TAB)) showQuickStats(); // Quick stats toggle
        
        // Auto-fire while holding mouse
        if (mouseDown) {
            doShoot();
        }
    }
    
    private void quickRestart() {
        keys.remove(KeyCode.R);
        gameLoop.stop();
        engine = new GameEngine();
        player = new Player("Hero", engine.getMap(), "PRESENT");
        gameOver = false;
        paused = false;
        totalKills = 0;
        currentWaveKills = 0;
        show();
    }
    
    private boolean showingQuickStats = false;
    
    private void showQuickStats() {
        // Toggle quick stats overlay on TAB
        showingQuickStats = !showingQuickStats;
        keys.remove(KeyCode.TAB);
    }
    
    private void doShoot() {
        long now = System.currentTimeMillis();
        if (now - lastShot < 120) return;
        if (player.shoot(engine)) {
            lastShot = now;
            addShake(3);
        }
    }
    
    private void doDash() {
        long now = System.currentTimeMillis();
        if (now - lastDashTime < DASH_COOLDOWN) return; // On cooldown
        
        player.dash();
        lastDashTime = now;
        addShake(8);
        screenEffects.add(new ScreenEffect("#00ffff", 15));
        addFloatingText("DASH!", GAME_WIDTH/2, GAME_HEIGHT/2 - 50, NEON_CYAN);
    }
    
    private void doUltimate() {
        long now = System.currentTimeMillis();
        if (now - lastUltimateTime < ULTIMATE_COOLDOWN) return; // On cooldown
        if (player.getUltimateCharge() < 100) return;
        
        player.useUltimate(engine);
        lastUltimateTime = now;
        addShake(15);
        screenEffects.add(new ScreenEffect("#ffd700", 20));
        showNotification("ULTIMATE!", "#ffd700");
        
        // Kill all enemies on screen for ultimate effect
        for (Enemy e : new ArrayList<>(engine.getEnemies())) {
            e.takeDamage(999, engine);
            if (e.isDead()) {
                onEnemyKilled(e);
            }
        }
    }
    
    private void onEnemyKilled(Enemy e) {
        totalKills++;
        currentWaveKills++;
        
        // Update kill streak
        if (killStreakSystem != null) {
            killStreakSystem.addKill();
        }
        
        // Add combo
        if (comboSystem != null) {
            comboSystem.addCombo();
        }
        
        // Score based on enemy type
        int scoreGain;
        switch (e.type) {
            case BOSS: scoreGain = 500; break;
            case TANK: scoreGain = 200; break;
            case SHOOTER: scoreGain = 150; break;
            case TELEPORTER: scoreGain = 175; break;
            case SUMMONER: scoreGain = 250; break;
            default: scoreGain = 100;
        }
        engine.addScore(scoreGain);
        
        // Floating score text
        addFloatingText("+" + scoreGain, e.visualX * TILE_SIZE - cameraX, e.visualY * TILE_SIZE - cameraY, NEON_GOLD);
        
        // Check wave completion
        if (currentWaveKills >= waveKillsRequired) {
            onWaveComplete();
        }
    }
    
    private void onWaveComplete() {
        currentWaveKills = 0;
        waveKillsRequired = Math.min(waveKillsRequired + 2, 20);
        
        showNotification("WAVE COMPLETE!", "#22c55e");
        addShake(10);
        screenEffects.add(new ScreenEffect("#22c55e", 15));
        
        // Bonus points
        engine.addScore(engine.getWave() * 100);
        addFloatingText("WAVE BONUS +" + (engine.getWave() * 100), GAME_WIDTH/2, GAME_HEIGHT/2, NEON_GREEN);
        
        // Add power-up for completing wave
        if (powerUpDisplay != null) {
            powerUpDisplay.addPowerUp("Wave Bonus", NEON_GREEN, 180);
        }
    }
    
    private void switchTimeline(String tl) {
        if (currentTimeline.equals(tl)) return;
        currentTimeline = tl;
        
        String color;
        switch (tl) {
            case "PAST": color = "#4a90d9"; engine.getPast().applyChange((int)player.getX(), (int)player.getY()); break;
            case "FUTURE": color = "#a855f7"; engine.getFuture().applyChange((int)player.getX(), (int)player.getY()); break;
            default: color = "#10b981"; engine.getPresent().applyChange((int)player.getX(), (int)player.getY());
        }
        
        screenEffects.add(new ScreenEffect(color, 15));
        addShake(5);
    }
    
    private void addShake(double intensity) {
        shakeIntensity = Math.max(shakeIntensity, intensity);
    }
    
    private void showNotification(String text, String color) {
        Text notif = new Text(text);
        notif.setFont(Font.font("Arial Black", FontWeight.BLACK, 32));
        notif.setFill(Color.web(color));
        
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web(color));
        glow.setRadius(20);
        notif.setEffect(glow);
        
        notificationArea.getChildren().add(notif);
        
        FadeTransition ft = new FadeTransition(Duration.millis(1500), notif);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setDelay(Duration.millis(500));
        ft.setOnFinished(e -> notificationArea.getChildren().remove(notif));
        ft.play();
    }
    
    private void update() {
        player.updateVisuals();
        engine.update(player.getX(), player.getY());
        
        // Collisions
        checkCollisions();
        
        // Collectibles
        Collectible c = engine.checkCollectible(player.getX(), player.getY());
        if (c != null) applyCollectible(c);
        
        // Camera
        double targetCamX = player.getVisualX() * TILE_SIZE - GAME_WIDTH/2 + TILE_SIZE/2;
        double targetCamY = player.getVisualY() * TILE_SIZE - GAME_HEIGHT/2 + TILE_SIZE/2;
        cameraX += (targetCamX - cameraX) * 0.08;
        cameraY += (targetCamY - cameraY) * 0.08;
        
        double maxCam = engine.getMap().getSize() * TILE_SIZE - GAME_WIDTH;
        cameraX = Math.max(0, Math.min(maxCam, cameraX));
        cameraY = Math.max(0, Math.min(maxCam, cameraY));
        
        // Shake decay
        if (shakeIntensity > 0) {
            shakeX = (Math.random() - 0.5) * shakeIntensity * 2;
            shakeY = (Math.random() - 0.5) * shakeIntensity * 2;
            shakeIntensity *= 0.9;
            if (shakeIntensity < 0.5) shakeIntensity = 0;
        } else {
            shakeX = shakeY = 0;
        }
        
        // Effects decay
        screenEffects.removeIf(e -> --e.life <= 0);
        
        // Game over check
        if (player.getHealth() <= 0) {
            gameOver = true;
            showGameOver();
        }
    }
    
    private void checkCollisions() {
        List<Projectile> projs = engine.getProjectiles();
        List<Enemy> enemies = engine.getEnemies();
        
        Iterator<Projectile> pi = projs.iterator();
        while (pi.hasNext()) {
            Projectile p = pi.next();
            
            // Wall collision
            Node node = engine.getMap().getNode((int)p.x, (int)p.y);
            if (node == null || !node.isWalkable()) {
                engine.spawnParticles(p.x, p.y, "spark", 5);
                pi.remove();
                continue;
            }
            
            if (p.isPlayer) {
                Iterator<Enemy> ei = enemies.iterator();
                while (ei.hasNext()) {
                    Enemy e = ei.next();
                    if (dist(p.x, p.y, e.x, e.y) < 0.8) {
                        e.takeDamage(player.getDamage(), engine);
                        engine.spawnParticles(p.x, p.y, "hit", 8);
                        
                        // Add damage indicator
                        if (damageIndicators != null) {
                            damageIndicators.addIndicator(e.visualX * TILE_SIZE - cameraX, 
                                e.visualY * TILE_SIZE - cameraY, player.getDamage(), false);
                        }
                        
                        pi.remove();
                        
                        if (e.isDead()) {
                            onEnemyKilled(e);
                            engine.spawnParticles(e.x, e.y, "explosion", 25);
                            addShake(e.type == EnemyType.BOSS ? 20 : 8);
                            screenEffects.add(new ScreenEffect("#ffffff", 8));
                            ei.remove();
                        }
                        break;
                    }
                }
            } else {
                if (dist(p.x, p.y, player.getX(), player.getY()) < 0.6) {
                    player.takeDamage(5, engine);
                    addShake(5);
                    screenEffects.add(new ScreenEffect("#ef4444", 10));
                    
                    // Add damage indicator for player
                    if (damageIndicators != null) {
                        damageIndicators.addIndicator(GAME_WIDTH/2, GAME_HEIGHT/2, 5, true);
                    }
                    
                    pi.remove();
                }
            }
        }
        
        // Enemy-player collision
        for (Enemy e : enemies) {
            if (dist(e.x, e.y, player.getX(), player.getY()) < 0.7) {
                player.takeDamage(e.type.damage, engine);
                addShake(8);
                screenEffects.add(new ScreenEffect("#ef4444", 12));
                
                // Add damage indicator
                if (damageIndicators != null) {
                    damageIndicators.addIndicator(GAME_WIDTH/2, GAME_HEIGHT/2, e.type.damage, true);
                }
            }
        }
        
        // Trap-player collision
        for (Trap t : engine.getTraps()) {
            if (dist(t.x, t.y, player.getX(), player.getY()) < 0.8) {
                if (System.currentTimeMillis() - t.lastDamage > 1000) { // Damage once per second
                    player.takeDamage(t.type.damage, engine);
                    addShake(3);
                    screenEffects.add(new ScreenEffect("#8b5cf6", 8));
                    t.lastDamage = System.currentTimeMillis();
                }
            }
        }
    }
    
    private double dist(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
    }
    
    private void applyCollectible(Collectible c) {
        String msg = "";
        String color = "#ffffff";
        switch (c.type) {
            case HEALTH: player.heal(30); msg = "+30 HP"; color = "#ef4444"; break;
            case ENERGY: player.addEnergy(50); msg = "+50 ENERGY"; color = "#fbbf24"; break;
            case AMMO: player.addAmmo(20); msg = "+20 AMMO"; color = "#00ffff"; break;
            case SHIELD: player.addShield(25); msg = "+25 SHIELD"; color = "#6b7280"; break;
            case SPEED_BOOST: player.applySpeedBoost(); msg = "SPEED BOOST!"; color = "#10b981"; break;
            case DAMAGE_BOOST: player.applyDamageBoost(); msg = "DAMAGE BOOST!"; color = "#a855f7"; break;
            case INVINCIBILITY: player.applyInvincibility(); msg = "INVINCIBLE!"; color = "#fbbf24"; break;
            case TIME_SLOW: player.applyTimeSlow(); msg = "TIME SLOW!"; color = "#8b5cf6"; break;
            case TELEPORT: 
                // Teleport to random safe location
                int tx, ty;
                do {
                    tx = (int)(Math.random() * 23) + 1;
                    ty = (int)(Math.random() * 23) + 1;
                } while (!engine.getMap().getNode(tx, ty).isWalkable());
                player.setPosition(tx, ty);
                msg = "TELEPORTED!"; 
                color = "#06b6d4"; 
                break;
        }
        showNotification(msg, color);
        engine.spawnParticles(c.x, c.y, "collect", 15);
    }
    
    private void updateHUD() {
        // Update level/wave display
        levelText.setText("LEVEL " + ((engine.getWave() - 1) / 5 + 1));
        waveText.setText("Wave " + engine.getWave());
        
        // Update score
        scoreText.setText(String.valueOf(engine.getScore()));
        
        // Update timeline text
        timelineText.setText(currentTimeline);
        switch (currentTimeline) {
            case "PAST": timelineText.setFill(Color.web("#3b82f6")); break;
            case "FUTURE": timelineText.setFill(Color.web("#a855f7")); break;
            default: timelineText.setFill(Color.web("#10b981"));
        }
        
        // Update kills display
        if (killsText != null) {
            killsText.setText(String.valueOf(totalKills));
        }
        
        // Update FPS display
        if (fpsText != null) {
            fpsText.setText(String.valueOf(currentFps));
            // Color based on FPS performance
            if (currentFps >= 55) fpsText.setFill(Color.web("#22c55e"));
            else if (currentFps >= 30) fpsText.setFill(Color.web("#fbbf24"));
            else fpsText.setFill(Color.web("#ef4444"));
        }
        
        // Update wave progress
        if (waveProgressText != null) {
            waveProgressText.setText(currentWaveKills + "/" + waveKillsRequired);
        }
        
        // Update game time
        if (gameTimeText != null) {
            long elapsedSeconds = (System.currentTimeMillis() - gameStartTime) / 1000;
            int minutes = (int) (elapsedSeconds / 60);
            int seconds = (int) (elapsedSeconds % 60);
            gameTimeText.setText(String.format("%02d:%02d", minutes, seconds));
        }
        
        // Update cooldown bars
        updateCooldownBars();
        
        // Update strategic map and threat level displays
        if (strategicMapCanvas != null) {
            drawStrategicMap(strategicMapCanvas.getGraphicsContext2D());
        }
        if (threatLevelCanvas != null) {
            drawThreatLevel(threatLevelCanvas.getGraphicsContext2D());
        }
    }
    
    private void updateCooldownBars() {
        long now = System.currentTimeMillis();
        
        // Dash cooldown
        if (dashCooldownBar != null) {
            Rectangle fill = (Rectangle) dashCooldownBar.getUserData();
            if (fill != null) {
                long elapsed = now - lastDashTime;
                double progress = Math.min(1.0, (double) elapsed / DASH_COOLDOWN);
                fill.setWidth(280 * progress);
                fill.setFill(progress >= 1.0 ? Color.web("#00ffff") : Color.web("#00ffff", 0.5));
            }
        }
        
        // Ultimate cooldown
        if (ultimateCooldownBar != null) {
            Rectangle fill = (Rectangle) ultimateCooldownBar.getUserData();
            if (fill != null) {
                long elapsed = now - lastUltimateTime;
                double progress = Math.min(1.0, (double) elapsed / ULTIMATE_COOLDOWN);
                double chargeProgress = player.getUltimateCharge() / 100.0;
                fill.setWidth(280 * Math.min(progress, chargeProgress));
                
                // Gold when ready, otherwise dim
                boolean ready = progress >= 1.0 && player.getUltimateCharge() >= 100;
                fill.setFill(ready ? Color.web("#ffd700") : Color.web("#ffd700", 0.4));
            }
        }
    }
    
    private void render() {
        gc.save();
        gc.translate(shakeX, shakeY);
        
        // Background - dark like reference
        gc.setFill(Color.web("#0a0f14"));
        gc.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        
        // Get timeline theme - teal/green color scheme like reference
        Color floorA, floorB, wallMain, wallLight, accent;
        switch (currentTimeline) {
            case "PAST":
                floorA = Color.web("#0d1520"); floorB = Color.web("#0a1218");
                wallMain = Color.web("#1a3545"); wallLight = Color.web("#2a4555");
                accent = Color.web("#3b82f6");
                break;
            case "FUTURE":
                floorA = Color.web("#120d20"); floorB = Color.web("#0f0a18");
                wallMain = Color.web("#351a4c"); wallLight = Color.web("#452a5c");
                accent = Color.web("#a855f7");
                break;
            default: // PRESENT - teal/green theme like reference image
                floorA = Color.web("#0f1a18"); floorB = Color.web("#0c1614");
                wallMain = Color.web("#1a3530"); wallLight = Color.web("#254540");
                accent = Color.web("#22c55e");
        }
        
        GridMap map = engine.getMap();
        
        // Draw tiles
        for (int i = 0; i < map.getSize(); i++) {
            for (int j = 0; j < map.getSize(); j++) {
                double sx = i * TILE_SIZE - cameraX;
                double sy = j * TILE_SIZE - cameraY;
                
                if (sx < -TILE_SIZE || sx > GAME_WIDTH || sy < -TILE_SIZE || sy > GAME_HEIGHT) continue;
                
                Node node = map.getNode(i, j);
                
                if (node.isWalkable()) {
                    gc.setFill((i + j) % 2 == 0 ? floorA : floorB);
                    gc.fillRect(sx, sy, TILE_SIZE, TILE_SIZE);
                } else {
                    // Wall with 3D effect
                    gc.setFill(wallMain);
                    gc.fillRect(sx, sy, TILE_SIZE, TILE_SIZE);
                    gc.setFill(wallLight);
                    gc.fillRect(sx, sy, TILE_SIZE, 5);
                    gc.fillRect(sx, sy, 5, TILE_SIZE);
                    gc.setFill(Color.web("#000", 0.3));
                    gc.fillRect(sx, sy + TILE_SIZE - 5, TILE_SIZE, 5);
                    gc.fillRect(sx + TILE_SIZE - 5, sy, 5, TILE_SIZE);
                }
            }
        }
        
        // Draw collectibles - yellow dots like reference
        for (Collectible c : engine.getCollectibles()) {
            double sx = c.x * TILE_SIZE - cameraX + TILE_SIZE/2;
            double sy = c.y * TILE_SIZE - cameraY + TILE_SIZE/2 + Math.sin(time * 3 + c.bobOffset) * 3;
            
            String color;
            switch (c.type) {
                case HEALTH: color = "#ef4444"; break;
                case ENERGY: color = "#fbbf24"; break;
                case AMMO: color = "#06b6d4"; break;
                case SHIELD: color = "#6b7280"; break;
                case SPEED_BOOST: color = "#22c55e"; break;
                case DAMAGE_BOOST: color = "#a855f7"; break;
                case INVINCIBILITY: color = "#fbbf24"; break;
                case TIME_SLOW: color = "#8b5cf6"; break;
                case TELEPORT: color = "#06b6d4"; break;
                default: color = "#fbbf24"; // Yellow like reference
            }
            
            // Outer glow
            gc.setFill(Color.web(color, 0.25));
            gc.fillOval(sx - 12, sy - 12, 24, 24);
            
            // Inner dot
            gc.setFill(Color.web(color));
            gc.fillOval(sx - 6, sy - 6, 12, 12);
        }
        
        // Draw traps
        for (Trap t : engine.getTraps()) {
            double sx = t.x * TILE_SIZE - cameraX + TILE_SIZE/2;
            double sy = t.y * TILE_SIZE - cameraY + TILE_SIZE/2;
            
            String color;
            switch (t.type) {
                case SPIKES: color = "#ef4444"; break;
                case POISON: color = "#22c55e"; break;
                default: color = "#ef4444";
            }
            
            // Triangle indicator
            gc.setFill(Color.web(color, 0.6));
            gc.fillPolygon(
                new double[]{sx, sx - 8, sx + 8},
                new double[]{sy - 8, sy + 6, sy + 6},
                3
            );
        }
        
        // Draw enemies
        for (Enemy e : engine.getEnemies()) {
            // Skip invisible enemies that are not visible
            if (e.type == EnemyType.INVISIBLE && !e.visible) continue;
            
            double sx = e.visualX * TILE_SIZE - cameraX + TILE_SIZE/2;
            double sy = e.visualY * TILE_SIZE - cameraY + TILE_SIZE/2;
            double size = e.type == EnemyType.BOSS ? 44 : e.type == EnemyType.TANK ? 32 : 24;
            
            // Color based on enemy type - tan/beige for normal, colors for special
            String bodyColor, glowColor;
            switch (e.type) {
                case CHASER: bodyColor = "#d4a574"; glowColor = "#22c55e"; break;   // Tan with green glow
                case SHOOTER: bodyColor = "#e8c090"; glowColor = "#f59e0b"; break;  // Beige with orange glow
                case TANK: bodyColor = "#8b7355"; glowColor = "#6b7280"; break;     // Brown with gray glow
                case TELEPORTER: bodyColor = "#c4b0d4"; glowColor = "#a855f7"; break; // Light purple
                case INVISIBLE: bodyColor = "#9ca3af"; glowColor = "#6b7280"; break;
                case SUMMONER: bodyColor = "#e8a4c4"; glowColor = "#ec4899"; break;  // Pink
                case BOSS: bodyColor = "#ff6b6b"; glowColor = "#ef4444"; break;      // Red
                default: bodyColor = "#d4a574"; glowColor = "#f97316";
            }
            
            // Shadow
            gc.setFill(Color.web("#000", 0.25));
            gc.fillOval(sx - size/2 + 3, sy + size/3, size - 2, size/4);
            
            // Outer glow
            gc.setFill(Color.web(glowColor, 0.2));
            gc.fillOval(sx - size/2 - 4, sy - size/2 - 4, size + 8, size + 8);
            
            // Body with gradient
            RadialGradient enemyGrad = new RadialGradient(0, 0, 0.3, 0.3, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, e.hit ? Color.WHITE : Color.web(bodyColor).brighter()),
                new Stop(0.7, e.hit ? Color.web("#dddddd") : Color.web(bodyColor)),
                new Stop(1, e.hit ? Color.web("#aaaaaa") : Color.web(bodyColor).darker())
            );
            gc.setFill(enemyGrad);
            gc.fillOval(sx - size/2, sy - size/2, size, size);
            
            // Eye
            double eyeX = sx + Math.cos(e.angle) * size/5;
            double eyeY = sy + Math.sin(e.angle) * size/5;
            gc.setFill(Color.WHITE);
            gc.fillOval(eyeX - 4, eyeY - 4, 8, 8);
            gc.setFill(Color.web("#1a1a1a"));
            gc.fillOval(eyeX - 2, eyeY - 2, 4, 4);
            
            // Health bar (only for damaged enemies)
            double hpPct = (double)e.health / e.maxHealth;
            if (hpPct < 1.0) {
                gc.setFill(Color.web("#1a2a25", 0.9));
                gc.fillRoundRect(sx - size/2, sy - size/2 - 10, size, 5, 2, 2);
                gc.setFill(hpPct > 0.5 ? Color.web("#22c55e") : hpPct > 0.25 ? Color.web("#f59e0b") : Color.web("#ef4444"));
                gc.fillRoundRect(sx - size/2, sy - size/2 - 10, size * hpPct, 5, 2, 2);
            }
        }
        
        // Draw projectiles
        for (Projectile p : engine.getProjectiles()) {
            double sx = p.x * TILE_SIZE - cameraX;
            double sy = p.y * TILE_SIZE - cameraY;
            
            // Trail
            gc.setStroke(Color.web(p.isPlayer ? "#22c55e" : "#ef4444", 0.4));
            gc.setLineWidth(2);
            for (int i = 0; i < p.trail.size() - 1; i++) {
                double[] t1 = p.trail.get(i);
                double[] t2 = p.trail.get(i + 1);
                gc.setGlobalAlpha((double)i / p.trail.size() * 0.5);
                gc.strokeLine(t1[0]*TILE_SIZE-cameraX, t1[1]*TILE_SIZE-cameraY, 
                              t2[0]*TILE_SIZE-cameraX, t2[1]*TILE_SIZE-cameraY);
            }
            gc.setGlobalAlpha(1);
            
            // Bullet glow
            gc.setFill(Color.web(p.isPlayer ? "#22c55e" : "#ef4444", 0.3));
            gc.fillOval(sx - 8, sy - 8, 16, 16);
            // Bullet core
            gc.setFill(Color.web(p.isPlayer ? "#22c55e" : "#ef4444"));
            gc.fillOval(sx - 4, sy - 4, 8, 8);
        }
        
        // Draw particles
        for (Particle p : engine.getParticles()) {
            double sx = p.x * TILE_SIZE - cameraX;
            double sy = p.y * TILE_SIZE - cameraY;
            double alpha = p.life / 60.0;
            
            String color;
            switch (p.type) {
                case "hit": color = "#ffd700"; break;
                case "explosion": color = "#ff6b6b"; break;
                case "damage": color = "#ef4444"; break;
                case "collect": color = "#10b981"; break;
                case "ultimate": color = "#ffd700"; break;
                default: color = "#ffffff";
            }
            
            gc.setFill(Color.web(color, alpha));
            gc.fillOval(sx - p.size/2, sy - p.size/2, p.size, p.size);
        }
        
        // Draw damage numbers
        for (DamageNumber d : engine.getDamageNumbers()) {
            double sx = d.x * TILE_SIZE - cameraX;
            double sy = d.y * TILE_SIZE - cameraY;
            double alpha = d.life / 40.0;
            
            gc.setGlobalAlpha(alpha);
            gc.setFont(Font.font("Arial Black", d.crit ? 22 : 16));
            gc.setFill(d.crit ? Color.web("#ffd700") : Color.WHITE);
            gc.fillText((d.crit ? "CRIT! " : "") + d.damage, sx, sy);
            gc.setGlobalAlpha(1);
        }
        
        // Draw player - white/light circle with glow like reference
        double px = player.getVisualX() * TILE_SIZE - cameraX + TILE_SIZE/2;
        double py = player.getVisualY() * TILE_SIZE - cameraY + TILE_SIZE/2;
        
        if (!player.isInvincible() || (int)(time * 20) % 2 == 0) {
            // Shadow
            gc.setFill(Color.web("#000", 0.3));
            gc.fillOval(px - 12 + 3, py + 8, 24, 8);
            
            // Outer glow (cyan/teal)
            gc.setFill(Color.web("#22c55e", 0.15));
            gc.fillOval(px - 30, py - 30, 60, 60);
            gc.setFill(Color.web("#22c55e", 0.25));
            gc.fillOval(px - 22, py - 22, 44, 44);
            
            // Main body - white/light with gradient
            RadialGradient playerGrad = new RadialGradient(0, 0, 0.3, 0.3, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ffffff")),
                new Stop(0.5, Color.web("#e0f0e8")),
                new Stop(1, Color.web("#90c0a0"))
            );
            gc.setFill(playerGrad);
            gc.fillOval(px - 16, py - 16, 32, 32);
            
            // Inner highlight
            gc.setFill(Color.web("#ffffff", 0.7));
            gc.fillOval(px - 10, py - 12, 10, 7);
            
            // Health bar above player
            double barWidth = 36;
            double hpPct = player.getHealth() / 100.0;
            gc.setFill(Color.web("#1a2a25", 0.9));
            gc.fillRoundRect(px - barWidth/2 - 2, py - 32, barWidth + 4, 8, 4, 4);
            gc.setFill(Color.web("#22c55e"));
            gc.fillRoundRect(px - barWidth/2, py - 30, barWidth * hpPct, 4, 2, 2);
            
            // Shield indicator
            if (player.getShield() > 0) {
                gc.setStroke(Color.web("#3b82f6", 0.8));
                gc.setLineWidth(2);
                gc.strokeOval(px - 20, py - 20, 40, 40);
            }
        }
        
        // Aim line
        double aimLen = 60;
        double aimEndX = px + Math.cos(player.getAimAngle()) * aimLen;
        double aimEndY = py + Math.sin(player.getAimAngle()) * aimLen;
        
        gc.setStroke(Color.web("#00ffff", 0.4));
        gc.setLineWidth(2);
        gc.setLineDashes(8, 8);
        gc.strokeLine(px, py, aimEndX, aimEndY);
        gc.setLineDashes(0);
        
        // Crosshair
        gc.setStroke(Color.web("#00ffff", 0.8));
        gc.setLineWidth(2);
        gc.strokeOval(aimEndX - 10, aimEndY - 10, 20, 20);
        gc.strokeLine(aimEndX - 15, aimEndY, aimEndX + 15, aimEndY);
        gc.strokeLine(aimEndX, aimEndY - 15, aimEndX, aimEndY + 15);
        
        gc.restore();
        
        // Screen effects
        for (ScreenEffect e : screenEffects) {
            gc.setFill(Color.web(e.color, e.life / 30.0 * 0.3));
            gc.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        }
        
        // Vignette
        RadialGradient vignette = new RadialGradient(0, 0, 0.5, 0.5, 0.7, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.TRANSPARENT),
            new Stop(1, Color.web("#000", 0.6))
        );
        gc.setFill(vignette);
        gc.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
    }
    
    private void showGameOver() {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.8);");
        
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        
        Text gameOverText = new Text("GAME OVER");
        gameOverText.setFont(Font.font("Arial Black", FontWeight.BLACK, 72));
        gameOverText.setFill(Color.web("#ef4444"));
        
        Text finalScore = new Text("Final Score: " + engine.getScore());
        finalScore.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        finalScore.setFill(Color.WHITE);
        
        Text stats = new Text("Wave: " + engine.getWave() + "  |  Kills: " + engine.getKills());
        stats.setFont(Font.font("Arial", 20));
        stats.setFill(Color.web("#888"));
        
        StackPane retryBtn = createGameOverButton("RETRY", "#00ffff");
        StackPane menuBtn = createGameOverButton("MENU", "#a855f7");
        
        retryBtn.setOnMouseClicked(e -> {
            gameLoop.stop();
            engine = new GameEngine();
            player = new Player("Hero", engine.getMap(), "PRESENT");
            gameOver = false;
            show();
        });
        
        menuBtn.setOnMouseClicked(e -> returnToMenu());
        
        HBox buttons = new HBox(20);
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(retryBtn, menuBtn);
        
        box.getChildren().addAll(gameOverText, finalScore, stats, buttons);
        overlay.getChildren().add(box);
        
        ((StackPane)((BorderPane)stage.getScene().getRoot()).getCenter()).getChildren().add(overlay);
    }
    
    private StackPane createGameOverButton(String text, String color) {
        StackPane btn = new StackPane();
        btn.setPrefSize(200, 50);
        btn.setCursor(javafx.scene.Cursor.HAND);
        
        Rectangle bg = new Rectangle(200, 50);
        bg.setArcWidth(25);
        bg.setArcHeight(25);
        bg.setFill(Color.web(color, 0.2));
        bg.setStroke(Color.web(color));
        bg.setStrokeWidth(2);
        
        Text label = new Text(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        label.setFill(Color.web(color));
        
        btn.getChildren().addAll(bg, label);
        
        btn.setOnMouseEntered(e -> bg.setFill(Color.web(color, 0.4)));
        btn.setOnMouseExited(e -> bg.setFill(Color.web(color, 0.2)));
        
        return btn;
    }
    
    private void returnToMenu() {
        if (gameLoop != null) gameLoop.stop();
        MainMenu menu = new MainMenu(stage);
        menu.show();
    }
    
    // Helper classes
    private static class ScreenEffect {
        String color;
        int life;
        ScreenEffect(String c, int l) { color = c; life = l; }
    }
    
    // Custom progress bar
    public static class ProgressIndicator extends StackPane {
        private Rectangle bg, fill;
        private Color accentColor;
        
        public ProgressIndicator(Color accentColor, double width, double height) {
            this.accentColor = accentColor;
            
            bg = new Rectangle(width, height);
            bg.setArcWidth(height);
            bg.setArcHeight(height);
            bg.setFill(Color.web("#1a1a2a"));
            bg.setStroke(accentColor);
            bg.setStrokeWidth(1);
            bg.setOpacity(0.8);
            
            fill = new Rectangle(width - 4, height - 4);
            fill.setArcWidth(height - 4);
            fill.setArcHeight(height - 4);
            fill.setFill(accentColor);
            fill.setTranslateX(-2);
            
            StackPane fillContainer = new StackPane(fill);
            fillContainer.setAlignment(Pos.CENTER_LEFT);
            fillContainer.setPadding(new Insets(2));
            fillContainer.setMaxWidth(width);
            
            getChildren().addAll(bg, fillContainer);
        }
        
        public void setProgress(double p) {
            p = Math.max(0, Math.min(1, p));
            fill.setWidth((bg.getWidth() - 4) * p);
            
            // Color change based on progress for health bar
            if (accentColor.equals(NEON_RED)) {
                if (p > 0.5) fill.setFill(NEON_GREEN);
                else if (p > 0.25) fill.setFill(NEON_YELLOW);
                else fill.setFill(NEON_RED);
            }
        }
    }

    // === CREATIVE ENHANCEMENT CLASSES ===

    private class FloatingText {
        double x, y, vx, vy, life, maxLife;
        String text;
        Color color;
        double size;
        
        FloatingText(String text, double x, double y, Color color) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.color = color;
            this.size = 20;
            this.maxLife = 120; // 2 seconds at 60fps
            this.life = maxLife;
            this.vx = (Math.random() - 0.5) * 2;
            this.vy = -2 - Math.random() * 2;
        }
        
        void update() {
            x += vx;
            y += vy;
            vy += 0.1; // gravity
            life--;
            size = 20 * (life / maxLife);
        }
        
        boolean isDead() { return life <= 0; }
        
        void render() {
            gc.save();
            gc.setGlobalAlpha(life / maxLife);
            gc.setFill(color);
            gc.setFont(Font.font("Arial Black", FontWeight.BLACK, size));
            gc.fillText(text, x, y);
            
            // Glow effect
            gc.setGlobalAlpha((life / maxLife) * 0.5);
            gc.setFill(Color.web("#ffffff", 0.8));
            gc.fillText(text, x - 1, y - 1);
            gc.restore();
        }
    }

    private class ParticleEffect {
        double x, y, vx, vy, life, maxLife;
        Color color;
        double size;
        
        ParticleEffect(double x, double y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.size = 2 + Math.random() * 4;
            this.maxLife = 60 + Math.random() * 60;
            this.life = maxLife;
            double angle = Math.random() * Math.PI * 2;
            double speed = 1 + Math.random() * 3;
            this.vx = Math.cos(angle) * speed;
            this.vy = Math.sin(angle) * speed;
        }
        
        void update() {
            x += vx;
            y += vy;
            vx *= 0.98; // friction
            vy *= 0.98;
            life--;
        }
        
        boolean isDead() { return life <= 0; }
        
        void render() {
            gc.save();
            gc.setGlobalAlpha(life / maxLife);
            gc.setFill(color);
            gc.fillOval(x - size/2, y - size/2, size, size);
            gc.restore();
        }
    }

    private class Notification {
        String title, message;
        Color color;
        double life, maxLife;
        double yOffset;
        
        Notification(String title, String message, Color color) {
            this.title = title;
            this.message = message;
            this.color = color;
            this.maxLife = 300; // 5 seconds
            this.life = maxLife;
            this.yOffset = -50;
        }
        
        void update() {
            life--;
            if (life > maxLife - 30) {
                yOffset = -50 + (maxLife - life) * 1.67; // slide in
            } else if (life < 30) {
                yOffset = -50 + life * 1.67; // slide out
            }
        }
        
        boolean isDead() { return life <= 0; }
        
        void render() {
            double alpha = Math.min(Math.min(life / 30.0, (maxLife - life) / 30.0), 1.0);
            gc.save();
            gc.setGlobalAlpha(alpha);
            
            // Background
            gc.setFill(Color.web("#1a1a2e", 0.9));
            gc.fillRoundRect(50, yOffset, 500, 80, 10, 10);
            gc.setStroke(color);
            gc.setLineWidth(2);
            gc.strokeRoundRect(50, yOffset, 500, 80, 10, 10);
            
            // Title
            gc.setFill(color);
            gc.setFont(Font.font("Arial Black", FontWeight.BLACK, 18));
            gc.fillText(title, 70, yOffset + 25);
            
            // Message
            gc.setFill(Color.web("#cccccc"));
            gc.setFont(Font.font("Arial", 14));
            gc.fillText(message, 70, yOffset + 50);
            
            gc.restore();
        }
    }

    private class MiniMap {
        private Canvas mapCanvas;
        private double mapSize = 150;
        
        MiniMap() {
            mapCanvas = new Canvas(mapSize, mapSize);
        }
        
        void update() {
            GraphicsContext mgc = mapCanvas.getGraphicsContext2D();
            mgc.clearRect(0, 0, mapSize, mapSize);
            
            // Background
            mgc.setFill(Color.web("#1a1a2e", 0.8));
            mgc.fillRect(0, 0, mapSize, mapSize);
            mgc.setStroke(Color.web("#00ffff", 0.5));
            mgc.strokeRect(0, 0, mapSize, mapSize);
            
            // Draw map (simplified representation)
            // This would normally show the actual game map
            mgc.setFill(NEON_CYAN);
            mgc.fillOval(mapSize/2 - 3, mapSize/2 - 3, 6, 6);
        }
        
        void render() {
            gc.drawImage(mapCanvas.snapshot(null, null), GAME_WIDTH - mapSize - 20, 20);
        }
    }

    private class TimelineShiftIndicator {
        private double shiftProgress = 0;
        private String currentTimeline = "PRESENT";
        private boolean isShifting = false;
        
        void startShift(String newTimeline) {
            isShifting = true;
            shiftProgress = 0;
            currentTimeline = newTimeline;
            
            // Add screen effect
            addScreenEffect(new Color(0, 1, 1, 0.3), 60);
        }
        
        void update() {
            if (isShifting) {
                shiftProgress += 0.02;
                if (shiftProgress >= 1) {
                    isShifting = false;
                    shiftProgress = 0;
                }
            }
        }
        
        void render() {
            if (isShifting) {
                double alpha = Math.sin(shiftProgress * Math.PI) * 0.5;
                gc.save();
                gc.setGlobalAlpha(alpha);
                gc.setFill(Color.web("#00ffff"));
                gc.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
                gc.restore();
            }
            
            // Timeline indicator
            gc.save();
            gc.setFill(Color.web("#00ffff", 0.8));
            gc.setFont(Font.font("Courier New", FontWeight.BOLD, 16));
            gc.fillText("TIMELINE: " + currentTimeline, GAME_WIDTH - 200, GAME_HEIGHT - 50);
            gc.restore();
        }
    }

    private class ComboSystem {
        private int currentCombo = 0;
        private int maxCombo = 0;
        private long lastComboTime = 0;
        private double comboScale = 1.0;
        
        void addCombo() {
            currentCombo++;
            maxCombo = Math.max(maxCombo, currentCombo);
            lastComboTime = System.currentTimeMillis();
            comboScale = 1.5;
            
            // Add particles for combo
            for (int i = 0; i < 10; i++) {
                particleEffects.add(new ParticleEffect(GAME_WIDTH/2, GAME_HEIGHT/2, NEON_PURPLE));
            }
        }
        
        void update() {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastComboTime > 3000) { // 3 seconds
                currentCombo = 0;
            }
            
            comboScale = Math.max(1.0, comboScale - 0.05);
        }
        
        void render() {
            if (currentCombo > 1) {
                gc.save();
                gc.setGlobalAlpha(0.8);
                gc.setFill(NEON_PURPLE);
                gc.setFont(Font.font("Arial Black", FontWeight.BLACK, 24 * comboScale));
                String comboText = currentCombo + "x COMBO!";
                gc.fillText(comboText, GAME_WIDTH/2 - 100, GAME_HEIGHT/2);
                
                // Glow
                gc.setGlobalAlpha(0.4);
                gc.setFill(Color.WHITE);
                gc.fillText(comboText, GAME_WIDTH/2 - 99, GAME_HEIGHT/2 - 1);
                gc.restore();
            }
        }
        
        int getCurrentCombo() { return currentCombo; }
    }

    private class PowerUpDisplay {
        private List<PowerUp> activePowerUps = new ArrayList<>();
        
        void addPowerUp(String name, Color color, int duration) {
            activePowerUps.add(new PowerUp(name, color, duration));
        }
        
        void update() {
            activePowerUps.removeIf(p -> p.update());
        }
        
        void render() {
            double y = 100;
            for (PowerUp powerUp : activePowerUps) {
                powerUp.render(20, y);
                y += 40;
            }
        }
        
        private class PowerUp {
            String name;
            Color color;
            int duration, maxDuration;
            
            PowerUp(String name, Color color, int duration) {
                this.name = name;
                this.color = color;
                this.duration = duration;
                this.maxDuration = duration;
            }
            
            boolean update() {
                duration--;
                return duration <= 0;
            }
            
            void render(double x, double y) {
                double progress = (double)duration / maxDuration;
                
                // Background
                gc.setFill(Color.web("#1a1a2e", 0.8));
                gc.fillRoundRect(x, y, 200, 30, 5, 5);
                
                // Progress bar
                gc.setFill(color);
                gc.fillRoundRect(x + 2, y + 2, (200 - 4) * progress, 26, 3, 3);
                
                // Text
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                gc.fillText(name.toUpperCase(), x + 10, y + 20);
            }
        }
    }

    // === ENHANCED METHODS ===

    private void addFloatingText(String text, double x, double y, Color color) {
        floatingTexts.add(new FloatingText(text, x, y, color));
    }

    private void addParticleEffect(double x, double y, Color color) {
        particleEffects.add(new ParticleEffect(x, y, color));
    }

    private void addNotification(String title, String message, Color color) {
        notifications.add(new Notification(title, message, color));
    }

    private void addScreenEffect(Color color, int duration) {
        // Use timeline shift effect instead
        if (timelineIndicator != null) {
            timelineIndicator.startShift("PAST");
        }
    }

    // Enhanced update method
    private void updateEnhancedFeatures() {
        // Update floating texts
        floatingTexts.removeIf(FloatingText::isDead);
        floatingTexts.forEach(FloatingText::update);
        
        // Update particles
        particleEffects.removeIf(ParticleEffect::isDead);
        particleEffects.forEach(ParticleEffect::update);
        
        // Update notifications
        notifications.removeIf(Notification::isDead);
        notifications.forEach(Notification::update);
        
        // Update enhanced systems
        if (miniMap != null) miniMap.update();
        if (timelineIndicator != null) timelineIndicator.update();
        if (comboSystem != null) comboSystem.update();
        if (powerUpDisplay != null) powerUpDisplay.update();
        if (killStreakSystem != null) killStreakSystem.update();
        if (bossHealthBar != null) bossHealthBar.update();
        if (damageIndicators != null) damageIndicators.update();
    }

    // Enhanced render method
    private void renderEnhancedFeatures() {
        // Render particles
        particleEffects.forEach(ParticleEffect::render);
        
        // Render floating texts
        floatingTexts.forEach(FloatingText::render);
        
        // Render notifications
        notifications.forEach(Notification::render);
        
        // Render enhanced systems
        if (miniMap != null) miniMap.render();
        if (timelineIndicator != null) timelineIndicator.render();
        if (comboSystem != null) comboSystem.render();
        if (powerUpDisplay != null) powerUpDisplay.render();
        if (killStreakSystem != null) killStreakSystem.render();
        if (bossHealthBar != null) bossHealthBar.render();
        if (damageIndicators != null) damageIndicators.render();
    }

    // Initialize enhanced features
    private void initEnhancedFeatures() {
        miniMap = new MiniMap();
        timelineIndicator = new TimelineShiftIndicator();
        comboSystem = new ComboSystem();
        powerUpDisplay = new PowerUpDisplay();
        killStreakSystem = new KillStreakSystem();
        bossHealthBar = new BossHealthBar();
        damageIndicators = new DamageIndicatorSystem();
        
        // Add welcome notification
        addNotification("Welcome to ChronoGrid!", "WASD=Move, Mouse=Aim, Click=Shoot, Q=Ultimate, RMB=Dash", NEON_CYAN);
    }
    
    // === NEW FEATURE CLASSES ===
    
    private class KillStreakSystem {
        private int currentStreak = 0;
        private int bestStreak = 0;
        private long lastKillTime = 0;
        private static final long STREAK_TIMEOUT = 3000; // 3 seconds
        private double displayScale = 1.0;
        private String currentTitle = "";
        
        void addKill() {
            long now = System.currentTimeMillis();
            if (now - lastKillTime < STREAK_TIMEOUT) {
                currentStreak++;
            } else {
                currentStreak = 1;
            }
            lastKillTime = now;
            bestStreak = Math.max(bestStreak, currentStreak);
            displayScale = 1.5;
            
            // Trigger streak bonuses
            if (currentStreak == 3) {
                currentTitle = "TRIPLE KILL!";
                showNotification("TRIPLE KILL!", "#f59e0b");
                engine.addScore(50);
            } else if (currentStreak == 5) {
                currentTitle = "KILLING SPREE!";
                showNotification("KILLING SPREE!", "#ef4444");
                engine.addScore(100);
            } else if (currentStreak == 10) {
                currentTitle = "UNSTOPPABLE!";
                showNotification("UNSTOPPABLE!", "#a855f7");
                engine.addScore(250);
                addShake(15);
            } else if (currentStreak == 15) {
                currentTitle = "GODLIKE!";
                showNotification("GODLIKE!", "#ffd700");
                engine.addScore(500);
                addShake(20);
                screenEffects.add(new ScreenEffect("#ffd700", 30));
            }
        }
        
        void update() {
            long now = System.currentTimeMillis();
            if (now - lastKillTime > STREAK_TIMEOUT && currentStreak > 0) {
                currentStreak = 0;
                currentTitle = "";
            }
            displayScale = Math.max(1.0, displayScale - 0.03);
        }
        
        void render() {
            if (currentStreak >= 3) {
                gc.save();
                
                // Streak counter at top center
                String streakText = currentStreak + "x STREAK";
                gc.setFont(Font.font("Arial Black", FontWeight.BLACK, 24 * displayScale));
                
                // Glow effect
                gc.setFill(Color.web("#ffd700", 0.3));
                gc.fillText(streakText, GAME_WIDTH/2 - 70, 60);
                
                gc.setFill(NEON_GOLD);
                gc.fillText(streakText, GAME_WIDTH/2 - 72, 58);
                
                // Title below
                if (!currentTitle.isEmpty()) {
                    gc.setFont(Font.font("Arial Black", 16));
                    gc.setFill(NEON_RED);
                    gc.fillText(currentTitle, GAME_WIDTH/2 - 60, 82);
                }
                
                gc.restore();
            }
        }
    }
    
    private class BossHealthBar {
        private Enemy currentBoss = null;
        private double displayHealth = 0;
        
        void update() {
            // Find active boss
            currentBoss = null;
            for (Enemy e : engine.getEnemies()) {
                if (e.type == EnemyType.BOSS) {
                    currentBoss = e;
                    break;
                }
            }
            
            if (currentBoss != null) {
                double targetHealth = (double) currentBoss.health / currentBoss.maxHealth;
                displayHealth += (targetHealth - displayHealth) * 0.1;
            }
        }
        
        void render() {
            if (currentBoss != null && !currentBoss.isDead()) {
                gc.save();
                
                double barWidth = 400;
                double barHeight = 20;
                double barX = (GAME_WIDTH - barWidth) / 2;
                double barY = GAME_HEIGHT - 60;
                
                // Background
                gc.setFill(Color.web("#1a1a2e", 0.9));
                gc.fillRoundRect(barX - 5, barY - 25, barWidth + 10, barHeight + 35, 10, 10);
                gc.setStroke(Color.web("#ef4444"));
                gc.setLineWidth(2);
                gc.strokeRoundRect(barX - 5, barY - 25, barWidth + 10, barHeight + 35, 10, 10);
                
                // Boss name
                gc.setFont(Font.font("Arial Black", FontWeight.BLACK, 14));
                gc.setFill(Color.web("#ef4444"));
                gc.fillText("⚔ BOSS ⚔", barX + barWidth/2 - 40, barY - 8);
                
                // Health bar background
                gc.setFill(Color.web("#2a0a0a"));
                gc.fillRoundRect(barX, barY, barWidth, barHeight, 5, 5);
                
                // Health bar fill with gradient
                if (displayHealth > 0) {
                    LinearGradient healthGrad = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#dc2626")),
                        new Stop(0.5, Color.web("#ef4444")),
                        new Stop(1, Color.web("#f87171"))
                    );
                    gc.setFill(healthGrad);
                    gc.fillRoundRect(barX, barY, barWidth * displayHealth, barHeight, 5, 5);
                }
                
                // Health percentage text
                gc.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
                gc.setFill(Color.WHITE);
                String healthText = (int)(displayHealth * 100) + "%";
                gc.fillText(healthText, barX + barWidth/2 - 15, barY + 15);
                
                gc.restore();
            }
        }
    }
    
    private class DamageIndicatorSystem {
        private List<DamageIndicator> indicators = new ArrayList<>();
        
        void addIndicator(double x, double y, int damage, boolean isPlayerDamage) {
            indicators.add(new DamageIndicator(x, y, damage, isPlayerDamage));
        }
        
        void update() {
            indicators.removeIf(DamageIndicator::update);
        }
        
        void render() {
            for (DamageIndicator di : indicators) {
                di.render();
            }
        }
        
        private class DamageIndicator {
            double x, y, vx, vy;
            int damage;
            boolean isPlayerDamage;
            double life = 60;
            double scale = 1.5;
            
            DamageIndicator(double x, double y, int damage, boolean isPlayerDamage) {
                this.x = x + (Math.random() - 0.5) * 40;
                this.y = y - 20;
                this.damage = damage;
                this.isPlayerDamage = isPlayerDamage;
                this.vx = (Math.random() - 0.5) * 2;
                this.vy = -3 - Math.random() * 2;
            }
            
            boolean update() {
                x += vx;
                y += vy;
                vy += 0.1;
                life--;
                scale = Math.max(1.0, scale - 0.02);
                return life <= 0;
            }
            
            void render() {
                gc.save();
                gc.setGlobalAlpha(life / 60.0);
                
                String text = "-" + damage;
                Color color = isPlayerDamage ? NEON_RED : NEON_ORANGE;
                
                // Shadow
                gc.setFill(Color.web("#000000", 0.5));
                gc.setFont(Font.font("Arial Black", FontWeight.BLACK, 16 * scale));
                gc.fillText(text, x + 2, y + 2);
                
                // Main text
                gc.setFill(color);
                gc.fillText(text, x, y);
                
                gc.restore();
            }
        }
    }
    
    // === PAUSE MENU ===
    
    private void togglePause() {
        paused = !paused;
        if (paused) {
            showPauseMenu();
        } else {
            hidePauseMenu();
        }
    }
    
    private StackPane pauseOverlay;
    
    private void showPauseMenu() {
        pauseOverlay = new StackPane();
        pauseOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        
        VBox menu = new VBox(20);
        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(40));
        menu.setStyle("-fx-background-color: #1a1a2e; -fx-border-color: #00ffff; -fx-border-width: 2; -fx-border-radius: 15; -fx-background-radius: 15;");
        
        Text title = new Text("PAUSED");
        title.setFont(Font.font("Arial Black", FontWeight.BLACK, 48));
        title.setFill(NEON_CYAN);
        
        StackPane resumeBtn = createGameOverButton("RESUME", "#00ffff");
        StackPane restartBtn = createGameOverButton("RESTART", "#22c55e");
        StackPane menuBtn = createGameOverButton("MAIN MENU", "#a855f7");
        
        resumeBtn.setOnMouseClicked(e -> togglePause());
        restartBtn.setOnMouseClicked(e -> {
            hidePauseMenu();
            gameLoop.stop();
            engine = new GameEngine();
            player = new Player("Hero", engine.getMap(), "PRESENT");
            gameOver = false;
            paused = false;
            totalKills = 0;
            currentWaveKills = 0;
            show();
        });
        menuBtn.setOnMouseClicked(e -> {
            hidePauseMenu();
            returnToMenu();
        });
        
        // Stats display
        VBox stats = new VBox(5);
        stats.setAlignment(Pos.CENTER);
        stats.setPadding(new Insets(20, 0, 0, 0));
        
        Text statsTitle = new Text("━━ STATS ━━");
        statsTitle.setFont(Font.font("Consolas", 14));
        statsTitle.setFill(Color.web("#666666"));
        
        Text killsStat = new Text("Kills: " + totalKills);
        killsStat.setFont(Font.font("Consolas", 16));
        killsStat.setFill(Color.WHITE);
        
        Text scoreStat = new Text("Score: " + engine.getScore());
        scoreStat.setFont(Font.font("Consolas", 16));
        scoreStat.setFill(NEON_GREEN);
        
        Text waveStat = new Text("Wave: " + engine.getWave());
        waveStat.setFont(Font.font("Consolas", 16));
        waveStat.setFill(NEON_CYAN);
        
        long elapsed = (System.currentTimeMillis() - gameStartTime) / 1000;
        Text timeStat = new Text("Time: " + String.format("%02d:%02d", elapsed / 60, elapsed % 60));
        timeStat.setFont(Font.font("Consolas", 16));
        timeStat.setFill(NEON_YELLOW);
        
        stats.getChildren().addAll(statsTitle, killsStat, scoreStat, waveStat, timeStat);
        
        menu.getChildren().addAll(title, resumeBtn, restartBtn, menuBtn, stats);
        pauseOverlay.getChildren().add(menu);
        
        ((StackPane)((BorderPane)stage.getScene().getRoot()).getCenter()).getChildren().add(pauseOverlay);
    }
    
    private void hidePauseMenu() {
        if (pauseOverlay != null) {
            ((StackPane)((BorderPane)stage.getScene().getRoot()).getCenter()).getChildren().remove(pauseOverlay);
            pauseOverlay = null;
        }
    }
}
