package game;

import map.GridMap;
import map.Node;
import timeline.Past;
import timeline.Present;
import timeline.Future;
import java.util.*;
import util.GameSettings;

public class GameEngine {
        // Remove old difficulty string, use GameSettings instead
    private GridMap map;
    private Past past;
    private Present present;
    private Future future;
    
    // Enemies
    private List<Enemy> enemies = new ArrayList<>();
    private List<Projectile> projectiles = new ArrayList<>();
    private List<Particle> particles = new ArrayList<>();
    private List<Collectible> collectibles = new ArrayList<>();
    private List<DamageNumber> damageNumbers = new ArrayList<>();
    private List<Trap> traps = new ArrayList<>();
    
    // Game state
    private int score = 0;
    private int combo = 0;
    private long lastKillTime = 0;
    private int wave = 1;
    private int kills = 0;
    private boolean bossSpawned = false;
    
    // Weather system
    private WeatherType currentWeather = WeatherType.CLEAR;
    private long weatherChangeTime = 0;
    private long lastWeatherEffect = 0;
    
    private Random random = new Random();

    public GameEngine() {
        this.map = new GridMap(25);
        this.past = new Past(map);
        this.present = new Present(map);
        this.future = new Future(map);

        generateMap();
        spawnCollectibles();
        spawnTraps();
		spawnEnemies(getEnemyCountForLevel(wave));
        }

    // Get enemy count based on difficulty and level (wave)
    private int getEnemyCountForLevel(int level) {
        GameSettings.Difficulty difficulty = GameSettings.getDifficulty();
        int baseCount;
        switch (difficulty) {
            case EASY: baseCount = 3; break;
            case HARD: baseCount = 8; break;
            default: baseCount = 5; // MEDIUM or fallback
        }
        // Increase enemy count as level increases
        return baseCount + (level - 1); // +1 enemy per level
    }
    
    private void generateMap() {
        clearMap();
        createBorderWalls();
        List<Room> rooms = generateRooms();
        connectRooms(rooms);
        addRandomObstacles();
    }
    
    private void clearMap() {
        for (int x = 0; x < 25; x++) {
            for (int y = 0; y < 25; y++) {
                map.modifyTile(x, y, true);
            }
        }
    }
    
    private void createBorderWalls() {
        for (int i = 0; i < 25; i++) {
            map.modifyTile(i, 0, false);
            map.modifyTile(i, 24, false);
            map.modifyTile(0, i, false);
            map.modifyTile(24, i, false);
        }
    }
    
    private List<Room> generateRooms() {
        List<Room> rooms = new ArrayList<>();
        int maxRooms = 8 + random.nextInt(5);
        
        for (int i = 0; i < maxRooms; i++) {
            int width = 3 + random.nextInt(4);
            int height = 3 + random.nextInt(4);
            int x = 1 + random.nextInt(25 - width - 2);
            int y = 1 + random.nextInt(25 - height - 2);
            
            Room room = new Room(x, y, width, height);
            
            if (!checkRoomOverlap(room, rooms)) {
                room.carve(map);
                rooms.add(room);
            }
        }
        return rooms;
}
    
    private boolean checkRoomOverlap(Room room, List<Room> existingRooms) {
        for (Room other : existingRooms) {
            if (room.overlaps(other)) {
                return true;
            }
        }
        return false;
    }
    
    private void connectRooms(List<Room> rooms) {
        for (int i = 0; i < rooms.size() - 1; i++) {
            Room roomA = rooms.get(i);
            Room roomB = rooms.get(i + 1);
            
            createCorridor(roomA.getCenterX(), roomA.getCenterY(), 
                          roomB.getCenterX(), roomB.getCenterY());
        }
    }
    
    private void createCorridor(int startX, int startY, int endX, int endY) {
        // Horizontal corridor
        int minX = Math.min(startX, endX);
        int maxX = Math.max(startX, endX);
        for (int x = minX; x <= maxX; x++) {
            map.modifyTile(x, startY, true);
        }
        
        // Vertical corridor
        int minY = Math.min(startY, endY);
        int maxY = Math.max(startY, endY);
        for (int y = minY; y <= maxY; y++) {
            map.modifyTile(endX, y, true);
        }
    }
    
    private void addRandomObstacles() {
        for (int i = 0; i < 100; i++) { // Increased from 40 to 100
            int x = 1 + random.nextInt(23);
            int y = 1 + random.nextInt(23);
            if (map.getNode(x, y).isWalkable() && random.nextDouble() < 0.7) { // Increased chance from 0.5 to 0.7
                map.modifyTile(x, y, false); // Pillar or decoration
            }
        }
    }
    
    // Spawn enemies with difficulty and level scaling
    public void spawnEnemies(int count) {
        for (int i = 0; i < count; i++) {
            int x, y;
            do {
                x = random.nextInt(23) + 1;
                y = random.nextInt(23) + 1;
            } while (!map.getNode(x, y).isWalkable());

            EnemyType type = EnemyType.values()[random.nextInt(EnemyType.values().length)];
            int level = wave;
            enemies.add(new Enemy(x, y, type, level));
        }
    }
    
    public void spawnBoss() {
        int level = wave;
        enemies.add(new Enemy(12, 12, EnemyType.BOSS, level));
        bossSpawned = true;
    }
    
    private void spawnTraps() {
        for (int i = 0; i < 8; i++) {
            int x;
            int y;
            do {
                x = random.nextInt(23) + 1;
                y = random.nextInt(23) + 1;
            } while (!map.getNode(x, y).isWalkable());
            
            TrapType type = TrapType.values()[random.nextInt(TrapType.values().length)];
            traps.add(new Trap(x, y, type));
        }
    }
    
    private void spawnCollectibles() {
        for (int i = 0; i < 10; i++) {
            int x;
            int y;
            do {
                x = random.nextInt(23) + 1;
                y = random.nextInt(23) + 1;
            } while (!map.getNode(x, y).isWalkable());
            
            CollectibleType type = CollectibleType.values()[random.nextInt(CollectibleType.values().length)];
            collectibles.add(new Collectible(x, y, type));
        }
    }
    
    public void update(double playerX, double playerY) {
        updateWeather();
        updateEnemies(playerX, playerY);
        updateProjectiles();
        updateParticles();
        updateDamageNumbers();
        updateCombos();
        updateWaveProgression();
    }
    
    private void updateWeather() {
        // Change weather every 30 seconds
        if (System.currentTimeMillis() - weatherChangeTime > 30000) {
            WeatherType[] weathers = WeatherType.values();
            currentWeather = weathers[random.nextInt(weathers.length)];
            weatherChangeTime = System.currentTimeMillis();
        }
        
        // Weather effects
        if (currentWeather != WeatherType.CLEAR && System.currentTimeMillis() - lastWeatherEffect > 1000) {
            applyWeatherEffects();
            lastWeatherEffect = System.currentTimeMillis();
        }
    }
    
    private void applyWeatherEffects() {
        switch (currentWeather) {
            case RAIN:
                // Rain spawns water puddles that can slow movement
                if (random.nextDouble() < 0.1) {
                    int x = random.nextInt(25);
                    int y = random.nextInt(25);
                    if (map.getNode(x, y).isWalkable()) {
                        // Temporary slow effect (would need to implement in player movement)
                    }
                }
                break;
            case STORM:
                // Storm increases enemy speed (multiplier already applied in enemy update)
                break;
            case SNOW:
                // Snow creates slippery ice patches
                if (random.nextDouble() < 0.05) {
                    int x = random.nextInt(25);
                    int y = random.nextInt(25);
                    if (map.getNode(x, y).isWalkable()) {
                        // Ice patch that makes movement unpredictable
                    }
                }
                break;
            default:
                // CLEAR or other weather types
                break;
        }
    }
    
    private void updateEnemies(double playerX, double playerY) {
        List<Enemy> enemiesCopy = new ArrayList<>(enemies);
        for (Enemy e : enemiesCopy) {
            e.update(playerX, playerY, map, this);
        }
    }
    
    private void updateProjectiles() {
        Iterator<Projectile> projIt = projectiles.iterator();
        while (projIt.hasNext()) {
            Projectile p = projIt.next();
            p.update();
            if (p.isDead()) projIt.remove();
        }
    }
    
    private void updateParticles() {
        Iterator<Particle> partIt = particles.iterator();
        while (partIt.hasNext()) {
            Particle p = partIt.next();
            p.update();
            if (p.isDead()) partIt.remove();
        }
    }
    
    private void updateDamageNumbers() {
        Iterator<DamageNumber> dmgIt = damageNumbers.iterator();
        while (dmgIt.hasNext()) {
            DamageNumber d = dmgIt.next();
            d.update();
            if (d.isDead()) dmgIt.remove();
        }
    }
    
    private void updateCombos() {
        if (System.currentTimeMillis() - lastKillTime > 2000) {
            combo = 0;
        }
    }
    
    private void updateWaveProgression() {
        if (enemies.isEmpty()) {
            wave++;
            if (wave % 5 == 0 && !bossSpawned) {
                spawnBoss();
            } else {
                spawnEnemies(5 + wave * 2);
                spawnCollectibles();
                bossSpawned = false;
            }
        }
    }
    
    public void fireProjectile(double x, double y, double dx, double dy, boolean isPlayer) {
        projectiles.add(new Projectile(x, y, dx, dy, isPlayer));
    }
    
    public void spawnParticles(double x, double y, String type, int count) {
        for (int i = 0; i < count; i++) {
            particles.add(new Particle(x, y, type));
        }
    }
    
    public void addDamageNumber(double x, double y, int damage, boolean crit) {
        damageNumbers.add(new DamageNumber(x, y, damage, crit));
    }
    
    public void addScore(int points) {
        combo++;
        lastKillTime = System.currentTimeMillis();
        score += points * (1 + combo / 5);
        kills++;
    }
    
    public Collectible checkCollectible(double px, double py) {
        Iterator<Collectible> it = collectibles.iterator();
        while (it.hasNext()) {
            Collectible c = it.next();
            double dist = Math.sqrt(Math.pow(c.x - px, 2) + Math.pow(c.y - py, 2));
            if (dist < 0.8) {
                it.remove();
                return c;
            }
        }
        return null;
    }

    public GridMap getMap() { return map; }
    public Past getPast() { return past; }
    public Present getPresent() { return present; }
    public Future getFuture() { return future; }
    public List<Enemy> getEnemies() { return enemies; }
    public List<Projectile> getProjectiles() { return projectiles; }
    public List<Particle> getParticles() { return particles; }
    public List<Collectible> getCollectibles() { return collectibles; }
    public List<DamageNumber> getDamageNumbers() { return damageNumbers; }
    public List<Trap> getTraps() { return traps; }
    public int getScore() { return score; }
    public int getCombo() { return combo; }
    public int getWave() { return wave; }
    public int getKills() { return kills; }
    public WeatherType getCurrentWeather() { return currentWeather; }
    
    // === INNER CLASSES ===
    
    public enum EnemyType {
        CHASER(30, 0.03, 1),
        SHOOTER(20, 0.015, 2),
        TANK(80, 0.01, 3),
        TELEPORTER(25, 0.04, 2),
        INVISIBLE(15, 0.025, 1),
        SUMMONER(40, 0.005, 1),
        BOSS(300, 0.008, 10);
        
        public final int health;
        public final double speed;
        public final int damage;
        
        EnemyType(int health, double speed, int damage) {
            this.health = health;
            this.speed = speed;
            this.damage = damage;
        }
    }
    
    public enum CollectibleType {
        HEALTH, ENERGY, AMMO, SHIELD, SPEED_BOOST, DAMAGE_BOOST, INVINCIBILITY, TIME_SLOW, TELEPORT
    }
    
    public static class Enemy {
        private double x;
        private double y;
        private double visualX;
        private double visualY;
        private int health;
        private int maxHealth;
        private EnemyType type;
        private double angle = 0;
        private long lastShot = 0;
        private boolean hit = false;
        private int hitTimer = 0;
        private long lastTeleport = 0;
        private long lastSummon = 0;
        private boolean visible = true;
        private long lastVisibilityChange = 0;
        private int bossPhase = 1;

        private int scaledDamage = 0;
        private double scaledSpeed = 0;

        // New constructor: applies difficulty and level scaling
        public Enemy(int x, int y, EnemyType type, int level) {
            this.x = x;
            this.y = y;
            this.visualX = x;
            this.visualY = y;
            this.type = type;
            applyDifficultyAndLevelScaling(level);
        }
        // For compatibility (if needed)
        public Enemy(int x, int y, EnemyType type) {
            this(x, y, type, 1);
        }

        // Applies difficulty and level scaling to enemy stats
        private void applyDifficultyAndLevelScaling(int level) {
            GameSettings.Difficulty difficulty = GameSettings.getDifficulty();
            // Base stats from type
            int baseHealth = type.health;
            int baseDamage = type.damage;
            double baseSpeed = type.speed;

            // Difficulty multipliers
            double healthMult, damageMult, speedMult;
            switch (difficulty) {
                case EASY:
                    healthMult = 0.8; damageMult = 0.8; speedMult = 0.9; break;
                case HARD:
                    healthMult = 1.3; damageMult = 1.3; speedMult = 1.15; break;
                default:
                    healthMult = 1.0; damageMult = 1.0; speedMult = 1.0; break;
            }

            // Level-based scaling (scales up per level)
            double levelHealthMult = 1.0 + (level - 1) * 0.15; // 15% more health per level
            double levelDamageMult = 1.0 + (level - 1) * 0.10; // 10% more damage per level
            double levelSpeedMult = 1.0 + (level - 1) * 0.05;  // 5% more speed per level

            this.maxHealth = (int)(baseHealth * healthMult * levelHealthMult);
            this.health = this.maxHealth;
            this.scaledDamage = (int)Math.max(1, baseDamage * damageMult * levelDamageMult);
            this.scaledSpeed = baseSpeed * speedMult * levelSpeedMult;
        }

        public int getDamage() {
            return scaledDamage;
        }
        public double getSpeed() {
            return scaledSpeed;
        }
        
        // Getters
        public double getX() { return x; }
        public double getY() { return y; }
        public double getVisualX() { return visualX; }
        public double getVisualY() { return visualY; }
        public int getHealth() { return health; }
        public int getMaxHealth() { return maxHealth; }
        public EnemyType getType() { return type; }
        public double getAngle() { return angle; }
        public boolean isHit() { return hit; }
        public boolean isVisible() { return visible; }
        public int getBossPhase() { return bossPhase; }
        
        public void takeDamage(int amount) {
            health -= amount;
            hit = true;
            hitTimer = 10;
        }
        
        public void update(double px, double py, GridMap map, GameEngine engine) {
            double dx = px - x;
            double dy = py - y;
            double dist = Math.sqrt(dx*dx + dy*dy);
            angle = Math.atan2(dy, dx);
            
            updateHitState();
            handleSpecialBehaviors(px, py, map, engine, dist);
            handleMovement(dx, dy, dist, engine, map);
            updateVisualPosition();
            handleCombatBehaviors(dx, dy, dist, engine);
        }
        
        private void updateHitState() {
            if (hit) {
                hitTimer--;
                if (hitTimer <= 0) hit = false;
            }
        }
        
        private void handleSpecialBehaviors(double px, double py, GridMap map, GameEngine engine, double dist) {
            switch (type) {
                case TELEPORTER:
                    handleTeleportBehavior(px, py, map, engine, dist);
                    break;
                case INVISIBLE:
                    handleInvisibleBehavior(dist);
                    break;
                case SUMMONER:
                    handleSummonBehavior(map, engine, dist);
                    break;
                default:
                    // CHASER, SHOOTER, TANK, BOSS use standard movement
                    break;
            }
        }
        
        private void handleTeleportBehavior(double px, double py, GridMap map, GameEngine engine, double dist) {
            if (System.currentTimeMillis() - lastTeleport > 3000 && dist < 8) {
                int attempts = 0;
                Node teleportNode = null;
                do {
                    x = engine.random.nextInt(23) + 1.0;
                    y = engine.random.nextInt(23) + 1.0;
                    attempts++;
                    teleportNode = map.getNode((int)x, (int)y);
                } while ((teleportNode == null || !teleportNode.isWalkable() || 
                        Math.sqrt(Math.pow(x - px, 2) + Math.pow(y - py, 2)) < 5) && attempts < 20);
                lastTeleport = System.currentTimeMillis();
            }
        }
        
        private void handleInvisibleBehavior(double dist) {
            if (dist < 3) {
                visible = true;
            } else if (System.currentTimeMillis() - lastVisibilityChange > 2000) {
                visible = !visible;
                lastVisibilityChange = System.currentTimeMillis();
            }
        }
        
        private void handleSummonBehavior(GridMap map, GameEngine engine, double dist) {
            if (System.currentTimeMillis() - lastSummon > 5000 && dist < 10) {
                for (int i = 0; i < 2; i++) {
                    int sx = (int)x + engine.random.nextInt(3) - 1;
                    int sy = (int)y + engine.random.nextInt(3) - 1;
                    Node summonNode = map.getNode(sx, sy);
                    if (summonNode != null && summonNode.isWalkable()) {
                        engine.enemies.add(new Enemy(sx, sy, EnemyType.CHASER));
                    }
                }
                lastSummon = System.currentTimeMillis();
            }
        }
        
        private void handleMovement(double dx, double dy, double dist, GameEngine engine, GridMap map) {
            if ((type != EnemyType.TELEPORTER || System.currentTimeMillis() - lastTeleport > 500) && dist > 1) {
                double speed = type.speed * engine.currentWeather.enemySpeedMultiplier;
                double newX = x + (dx / dist) * speed;
                double newY = y + (dy / dist) * speed;
                
                Node node = map.getNode((int)newX, (int)newY);
                if (node != null && node.isWalkable()) {
                    x = newX;
                    y = newY;
                }
            }
        }
        
        private void updateVisualPosition() {
            visualX += (x - visualX) * 0.15;
            visualY += (y - visualY) * 0.15;
        }
        
        private void handleCombatBehaviors(double dx, double dy, double dist, GameEngine engine) {
            if (type == EnemyType.BOSS) {
                handleBossCombat(dx, dy, dist, engine);
            } else if (type == EnemyType.SHOOTER && System.currentTimeMillis() - lastShot > 1500) {
                engine.fireProjectile(x, y, dx/dist * 0.15, dy/dist * 0.15, false);
                lastShot = System.currentTimeMillis();
            }
        }
        
        private void handleBossCombat(double dx, double dy, double dist, GameEngine engine) {
            updateBossPhase(engine);
            executeBossPhaseAttack(dx, dy, dist, engine);
        }
        
        private void updateBossPhase(GameEngine engine) {
            int newPhase = 1;
            if (health < maxHealth * 0.7) newPhase = 2;
            if (health < maxHealth * 0.4) newPhase = 3;
            if (health < maxHealth * 0.2) newPhase = 4;
            
            if (newPhase != bossPhase) {
                bossPhase = newPhase;
                for (int i = 0; i < bossPhase * 3; i++) {
                    engine.spawnParticles(x + (Math.random() - 0.5) * 2, y + (Math.random() - 0.5) * 2, "explosion", 10);
                }
            }
        }
        
        private void executeBossPhaseAttack(double dx, double dy, double dist, GameEngine engine) {
            switch (bossPhase) {
                case 1:
                    bossPhase1Attack(dx, dy, dist, engine);
                    break;
                case 2:
                    bossPhase2Attack(dx, dy, dist, engine);
                    break;
                case 3:
                    bossPhase3Attack(engine);
                    break;
                case 4:
                    bossPhase4Attack(dx, dy, dist, engine);
                    break;
                default:
                    break;
            }
        }
        
        private void bossPhase1Attack(double dx, double dy, double dist, GameEngine engine) {
            if (System.currentTimeMillis() - lastShot > 800) {
                engine.fireProjectile(x, y, dx/dist * 0.15, dy/dist * 0.15, false);
                lastShot = System.currentTimeMillis();
            }
        }
        
        private void bossPhase2Attack(double dx, double dy, double dist, GameEngine engine) {
            if (System.currentTimeMillis() - lastShot > 600) {
                engine.fireProjectile(x, y, dx/dist * 0.15, dy/dist * 0.15, false);
                lastShot = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - lastSummon > 4000) {
                for (int i = 0; i < 2; i++) {
                    int sx = (int)x + engine.random.nextInt(5) - 2;
                    int sy = (int)y + engine.random.nextInt(5) - 2;
                    Node spawnNode = engine.getMap().getNode(sx, sy);
                    if (spawnNode != null && spawnNode.isWalkable()) {
                        engine.enemies.add(new Enemy(sx, sy, EnemyType.TELEPORTER));
                    }
                }
                lastSummon = System.currentTimeMillis();
            }
        }
        
        private void bossPhase3Attack(GameEngine engine) {
            if (System.currentTimeMillis() - lastShot > 300) {
                for (int i = 0; i < 8; i++) {
                    double fireAngle = i * Math.PI / 4;
                    engine.fireProjectile(x, y, Math.cos(fireAngle) * 0.1, Math.sin(fireAngle) * 0.1, false);
                }
                lastShot = System.currentTimeMillis();
            }
        }
        
        private void bossPhase4Attack(double dx, double dy, double dist, GameEngine engine) {
            if (System.currentTimeMillis() - lastShot > 200) {
                engine.fireProjectile(x, y, dx/dist * 0.2, dy/dist * 0.2, false);
                lastShot = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - lastSummon > 2000) {
                for (int i = 0; i < 3; i++) {
                    int sx = (int)x + engine.random.nextInt(7) - 3;
                    int sy = (int)y + engine.random.nextInt(7) - 3;
                    Node spawnNode = engine.getMap().getNode(sx, sy);
                    if (spawnNode != null && spawnNode.isWalkable()) {
                        EnemyType[] types = {EnemyType.CHASER, EnemyType.SHOOTER, EnemyType.TELEPORTER};
                        engine.enemies.add(new Enemy(sx, sy, types[engine.random.nextInt(types.length)]));
                    }
                }
                lastSummon = System.currentTimeMillis();
            }
        }
        
        public void takeDamage(int dmg, GameEngine engine) {
            health -= dmg;
            hit = true;
            hitTimer = 10;
            engine.addDamageNumber(x, y - 0.5, dmg, dmg > 20);
            engine.spawnParticles(x, y, "hit", 5);
        }
        
        public boolean isDead() { return health <= 0; }
    }
    
    public static class Projectile {
        private double x;
        private double y;
        private double dx;
        private double dy;
        private boolean isPlayer;
        private int life = 100;
        private List<double[]> trail = new ArrayList<>();
        
        public Projectile(double x, double y, double dx, double dy, boolean isPlayer) {
            this.x = x; 
            this.y = y; 
            this.dx = dx; 
            this.dy = dy; 
            this.isPlayer = isPlayer;
        }
        
        public void update() {
            trail.add(new double[]{x, y});
            if (trail.size() > 10) trail.remove(0);
            x += dx;
            y += dy;
            life--;
        }
        
        // Getters
        public double getX() { return x; }
        public double getY() { return y; }
        public boolean isPlayer() { return isPlayer; }
        public List<double[]> getTrail() { return trail; }
        
        public boolean isDead() { return life <= 0 || x < 0 || x > 25 || y < 0 || y > 25; }
    }
    
    public static class Particle {
        private double x;
        private double y;
        private double dx;
        private double dy;
        private int life;
        private String type;
        private double size;
        
        private static Random rand = new Random();
        
        public Particle(double x, double y, String type) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.dx = (rand.nextDouble() - 0.5) * 0.2;
            this.dy = (rand.nextDouble() - 0.5) * 0.2;
            this.life = 30 + rand.nextInt(30);
            this.size = 2 + rand.nextDouble() * 4;
        }
        
        public void update() {
            x += dx;
            y += dy;
            dy += 0.005; // gravity
            life--;
            size *= 0.95;
        }
        
        // Getters
        public double getX() { return x; }
        public double getY() { return y; }
        public String getType() { return type; }
        public double getSize() { return size; }
        public int getLife() { return life; }
        
        public boolean isDead() { return life <= 0; }
    }
    
    public static class Collectible {
        private double x;
        private double y;
        private CollectibleType type;
        private double bobOffset = 0;
        
        public Collectible(double x, double y, CollectibleType type) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.bobOffset = Math.random() * Math.PI * 2;
        }
        
        // Getters
        public double getX() { return x; }
        public double getY() { return y; }
        public CollectibleType getType() { return type; }
        public double getBobOffset() { return bobOffset; }
        public void updateBobOffset(double delta) { bobOffset += delta; }
    }
    
    public static class DamageNumber {
        private double x;
        private double y;
        private int damage;
        private boolean crit;
        private int life = 40;
        private double dy = -0.05;
        
        public DamageNumber(double x, double y, int damage, boolean crit) {
            this.x = x;
            this.y = y;
            this.damage = damage;
            this.crit = crit;
        }
        
        public void update() {
            y += dy;
            dy *= 0.95;
            life--;
        }
        
        // Getters
        public double getX() { return x; }
        public double getY() { return y; }
        public int getDamage() { return damage; }
        public boolean isCrit() { return crit; }
        public int getLife() { return life; }
        
        public boolean isDead() { return life <= 0; }
    }
    
    public static class Trap {
        private double x;
        private double y;
        private TrapType type;
        private long lastDamage = 0;
        
        public Trap(double x, double y, TrapType type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }
        
        // Getters and setters
        public double getX() { return x; }
        public double getY() { return y; }
        public TrapType getType() { return type; }
        public long getLastDamage() { return lastDamage; }
        public void setLastDamage(long lastDamage) { this.lastDamage = lastDamage; }
    }
    
    public enum TrapType {
        SPIKES(5), // Damage per second
        POISON(3);
        
        public final int damage;
        
        TrapType(int damage) {
            this.damage = damage;
        }
    }
    
    public enum WeatherType {
        CLEAR(1.0, 1.0, "Clear skies"),
        RAIN(0.8, 1.2, "Rain reduces visibility"),
        STORM(0.6, 1.5, "Storm increases enemy speed"),
        FOG(0.9, 0.8, "Fog slows movement"),
        SNOW(0.7, 0.9, "Snow creates slippery surfaces");
        
        public final double visibility;
        public final double enemySpeedMultiplier;
        public final String description;
        
        WeatherType(double visibility, double enemySpeedMultiplier, String description) {
            this.visibility = visibility;
            this.enemySpeedMultiplier = enemySpeedMultiplier;
            this.description = description;
        }
    }
    
    public static class Room {
        private int x;
        private int y;
        private int width;
        private int height;
        private int centerX;
        private int centerY;
        
        public Room(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.centerX = x + width / 2;
            this.centerY = y + height / 2;
        }
        
        // Getters
        public int getCenterX() { return centerX; }
        public int getCenterY() { return centerY; }
        
        public boolean overlaps(Room other) {
            return !(x + width < other.x || other.x + other.width < x ||
                     y + height < other.y || other.y + other.height < y);
        }
        
        public void carve(GridMap map) {
            for (int dx = 0; dx < width; dx++) {
                for (int dy = 0; dy < height; dy++) {
                    map.modifyTile(x + dx, y + dy, true);
                }
            }
        }
    }
}
