package game;

import map.GridMap;
import map.Node;
import timeline.Past;
import timeline.Present;
import timeline.Future;
import java.util.*;

public class GameEngine {
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
        spawnEnemies(5);
    }
    
    private void generateMap() {
        // Clear map
        for (int x = 0; x < 25; x++) {
            for (int y = 0; y < 25; y++) {
                map.modifyTile(x, y, true);
            }
        }
        
        // Create border walls
        for (int i = 0; i < 25; i++) {
            map.modifyTile(i, 0, false);
            map.modifyTile(i, 24, false);
            map.modifyTile(0, i, false);
            map.modifyTile(24, i, false);
        }
        
        // Generate procedural rooms
        List<Room> rooms = new ArrayList<>();
        int maxRooms = 8 + random.nextInt(5);
        
        for (int i = 0; i < maxRooms; i++) {
            int width = 3 + random.nextInt(4);
            int height = 3 + random.nextInt(4);
            int x = 1 + random.nextInt(25 - width - 2);
            int y = 1 + random.nextInt(25 - height - 2);
            
            Room room = new Room(x, y, width, height);
            
            // Check if room overlaps with existing rooms
            boolean overlaps = false;
            for (Room other : rooms) {
                if (room.overlaps(other)) {
                    overlaps = true;
                    break;
                }
            }
            
            if (!overlaps) {
                room.carve(map);
                rooms.add(room);
            }
        }
        
        // Connect rooms with corridors
        for (int i = 0; i < rooms.size() - 1; i++) {
            Room roomA = rooms.get(i);
            Room roomB = rooms.get(i + 1);
            
            int startX = roomA.centerX;
            int startY = roomA.centerY;
            int endX = roomB.centerX;
            int endY = roomB.centerY;
            
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
        
        // Add some random obstacles and decorations
        for (int i = 0; i < 15; i++) {
            int x = 1 + random.nextInt(23);
            int y = 1 + random.nextInt(23);
            if (map.getNode(x, y).isWalkable() && random.nextDouble() < 0.3) {
                map.modifyTile(x, y, false); // Pillar or decoration
            }
        }
    }
    
    public void spawnEnemies(int count) {
        for (int i = 0; i < count; i++) {
            int x, y;
            do {
                x = random.nextInt(23) + 1;
                y = random.nextInt(23) + 1;
            } while (!map.getNode(x, y).isWalkable());
            
            EnemyType type = EnemyType.values()[random.nextInt(EnemyType.values().length)];
            enemies.add(new Enemy(x, y, type));
        }
    }
    
    public void spawnBoss() {
        enemies.add(new Enemy(12, 12, EnemyType.BOSS));
        bossSpawned = true;
    }
    
    private void spawnTraps() {
        for (int i = 0; i < 8; i++) {
            int x, y;
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
            int x, y;
            do {
                x = random.nextInt(23) + 1;
                y = random.nextInt(23) + 1;
            } while (!map.getNode(x, y).isWalkable());
            
            CollectibleType type = CollectibleType.values()[random.nextInt(CollectibleType.values().length)];
            collectibles.add(new Collectible(x, y, type));
        }
    }
    
    public void update(double playerX, double playerY) {
        // Update weather
        if (System.currentTimeMillis() - weatherChangeTime > 30000) { // Change weather every 30 seconds
            WeatherType[] weathers = WeatherType.values();
            currentWeather = weathers[random.nextInt(weathers.length)];
            weatherChangeTime = System.currentTimeMillis();
        }
        
        // Weather effects
        if (currentWeather != WeatherType.CLEAR && System.currentTimeMillis() - lastWeatherEffect > 1000) {
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
                    // Storm increases enemy speed
                    for (Enemy e : enemies) {
                        // Speed multiplier already applied in enemy update
                    }
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
            }
            lastWeatherEffect = System.currentTimeMillis();
        }
        
        // Update enemies AI
        List<Enemy> enemiesCopy = new ArrayList<>(enemies);
        for (Enemy e : enemiesCopy) {
            e.update(playerX, playerY, map, this);
        }
        
        // Update projectiles
        Iterator<Projectile> projIt = projectiles.iterator();
        while (projIt.hasNext()) {
            Projectile p = projIt.next();
            p.update();
            if (p.isDead()) projIt.remove();
        }
        
        // Update particles
        Iterator<Particle> partIt = particles.iterator();
        while (partIt.hasNext()) {
            Particle p = partIt.next();
            p.update();
            if (p.isDead()) partIt.remove();
        }
        
        // Update damage numbers
        Iterator<DamageNumber> dmgIt = damageNumbers.iterator();
        while (dmgIt.hasNext()) {
            DamageNumber d = dmgIt.next();
            d.update();
            if (d.isDead()) dmgIt.remove();
        }
        
        // Combo decay
        if (System.currentTimeMillis() - lastKillTime > 2000) {
            combo = 0;
        }
        
        // Wave progression
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
        public double x, y;
        public double visualX, visualY;
        public int health, maxHealth;
        public EnemyType type;
        public double angle = 0;
        public long lastShot = 0;
        public boolean hit = false;
        public int hitTimer = 0;
        public long lastTeleport = 0;
        public long lastSummon = 0;
        public boolean visible = true;
        public long lastVisibilityChange = 0;
        public int bossPhase = 1;
        
        public Enemy(int x, int y, EnemyType type) {
            this.x = x;
            this.y = y;
            this.visualX = x;
            this.visualY = y;
            this.type = type;
            this.health = type.health;
            this.maxHealth = type.health;
        }
        
        public void update(double px, double py, GridMap map, GameEngine engine) {
            double dx = px - x;
            double dy = py - y;
            double dist = Math.sqrt(dx*dx + dy*dy);
            angle = Math.atan2(dy, dx);
            
            if (hit) {
                hitTimer--;
                if (hitTimer <= 0) hit = false;
            }
            
            // Special enemy behaviors
            switch (type) {
                case TELEPORTER:
                    if (System.currentTimeMillis() - lastTeleport > 3000 && dist < 8) {
                        // Teleport to random location away from player
                        int attempts = 0;
                        Node teleportNode = null;
                        do {
                            x = engine.random.nextInt(23) + 1;
                            y = engine.random.nextInt(23) + 1;
                            attempts++;
                            teleportNode = map.getNode((int)x, (int)y);
                        } while ((teleportNode == null || !teleportNode.isWalkable() || 
                                Math.sqrt(Math.pow(x - px, 2) + Math.pow(y - py, 2)) < 5) && attempts < 20);
                        lastTeleport = System.currentTimeMillis();
                    }
                    break;
                    
                case INVISIBLE:
                    if (dist < 3) {
                        visible = true;
                    } else if (System.currentTimeMillis() - lastVisibilityChange > 2000) {
                        visible = !visible;
                        lastVisibilityChange = System.currentTimeMillis();
                    }
                    break;
                    
                case SUMMONER:
                    if (System.currentTimeMillis() - lastSummon > 5000 && dist < 10) {
                        // Summon minions
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
                    break;
            }
            
            // Movement (skip for teleporter when teleporting)
            if (type != EnemyType.TELEPORTER || System.currentTimeMillis() - lastTeleport > 500) {
                if (dist > 1) {
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
            
            // Smooth visual position
            visualX += (x - visualX) * 0.15;
            visualY += (y - visualY) * 0.15;
            
            // Boss AI with phases
            if (type == EnemyType.BOSS) {
                // Update boss phase based on health
                int newPhase = 1;
                if (health < maxHealth * 0.7) newPhase = 2;
                if (health < maxHealth * 0.4) newPhase = 3;
                if (health < maxHealth * 0.2) newPhase = 4;
                
                if (newPhase != bossPhase) {
                    bossPhase = newPhase;
                    // Phase change effects
                    for (int i = 0; i < bossPhase * 3; i++) {
                        engine.spawnParticles(x + (Math.random() - 0.5) * 2, y + (Math.random() - 0.5) * 2, "explosion", 10);
                    }
                }
                
                // Phase-specific behaviors
                switch (bossPhase) {
                    case 1: // Basic shooting
                        if (System.currentTimeMillis() - lastShot > 800) {
                            engine.fireProjectile(x, y, dx/dist * 0.15, dy/dist * 0.15, false);
                            lastShot = System.currentTimeMillis();
                        }
                        break;
                    case 2: // Faster shooting + summons
                        if (System.currentTimeMillis() - lastShot > 600) {
                            engine.fireProjectile(x, y, dx/dist * 0.15, dy/dist * 0.15, false);
                            lastShot = System.currentTimeMillis();
                        }
                        if (System.currentTimeMillis() - lastSummon > 4000) {
                            for (int i = 0; i < 2; i++) {
                                int sx = (int)x + engine.random.nextInt(5) - 2;
                                int sy = (int)y + engine.random.nextInt(5) - 2;
                                Node spawnNode = map.getNode(sx, sy);
                                if (spawnNode != null && spawnNode.isWalkable()) {
                                    engine.enemies.add(new Enemy(sx, sy, EnemyType.TELEPORTER));
                                }
                            }
                            lastSummon = System.currentTimeMillis();
                        }
                        break;
                    case 3: // Rapid fire + area damage
                        if (System.currentTimeMillis() - lastShot > 300) {
                            // Fire in multiple directions
                            for (int i = 0; i < 8; i++) {
                                double angle = i * Math.PI / 4;
                                engine.fireProjectile(x, y, Math.cos(angle) * 0.1, Math.sin(angle) * 0.1, false);
                            }
                            lastShot = System.currentTimeMillis();
                        }
                        break;
                    case 4: // Desperate mode - very fast, spawns many enemies
                        if (System.currentTimeMillis() - lastShot > 200) {
                            engine.fireProjectile(x, y, dx/dist * 0.2, dy/dist * 0.2, false);
                            lastShot = System.currentTimeMillis();
                        }
                        if (System.currentTimeMillis() - lastSummon > 2000) {
                            for (int i = 0; i < 3; i++) {
                                int sx = (int)x + engine.random.nextInt(7) - 3;
                                int sy = (int)y + engine.random.nextInt(7) - 3;
                                Node spawnNode = map.getNode(sx, sy);
                                if (spawnNode != null && spawnNode.isWalkable()) {
                                    EnemyType[] types = {EnemyType.CHASER, EnemyType.SHOOTER, EnemyType.TELEPORTER};
                                    engine.enemies.add(new Enemy(sx, sy, types[engine.random.nextInt(types.length)]));
                                }
                            }
                            lastSummon = System.currentTimeMillis();
                        }
                        break;
                }
            } else if (type == EnemyType.SHOOTER) {
                if (System.currentTimeMillis() - lastShot > 1500) {
                    engine.fireProjectile(x, y, dx/dist * 0.15, dy/dist * 0.15, false);
                    lastShot = System.currentTimeMillis();
                }
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
        public double x, y, dx, dy;
        public boolean isPlayer;
        public int life = 100;
        public List<double[]> trail = new ArrayList<>();
        
        public Projectile(double x, double y, double dx, double dy, boolean isPlayer) {
            this.x = x; this.y = y; this.dx = dx; this.dy = dy; this.isPlayer = isPlayer;
        }
        
        public void update() {
            trail.add(new double[]{x, y});
            if (trail.size() > 10) trail.remove(0);
            x += dx;
            y += dy;
            life--;
        }
        
        public boolean isDead() { return life <= 0 || x < 0 || x > 25 || y < 0 || y > 25; }
    }
    
    public static class Particle {
        public double x, y, dx, dy;
        public int life;
        public String type;
        public double size;
        
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
        
        public boolean isDead() { return life <= 0; }
    }
    
    public static class Collectible {
        public double x, y;
        public CollectibleType type;
        public double bobOffset = 0;
        
        public Collectible(double x, double y, CollectibleType type) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.bobOffset = Math.random() * Math.PI * 2;
        }
    }
    
    public static class DamageNumber {
        public double x, y;
        public int damage;
        public boolean crit;
        public int life = 40;
        public double dy = -0.05;
        
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
        
        public boolean isDead() { return life <= 0; }
    }
    
    public static class Trap {
        public double x, y;
        public TrapType type;
        public long lastDamage = 0;
        
        public Trap(double x, double y, TrapType type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }
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
        public int x, y, width, height;
        public int centerX, centerY;
        
        public Room(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.centerX = x + width / 2;
            this.centerY = y + height / 2;
        }
        
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
