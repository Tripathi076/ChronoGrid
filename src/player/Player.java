package player;

import map.Node;
import map.GridMap;
import game.GameEngine;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class Player implements Serializable, Runnable {
    private static final long serialVersionUID = 1L;

    private String name;
    private double x, y;
    private double visualX, visualY;
    private GridMap map;
    private String timeline;
    private boolean running = true;
    
    // Stats
    private int health = 100;
    private int maxHealth = 100;
    private int energy = 100;
    private int maxEnergy = 100;
    private int ammo = 50;
    private int maxAmmo = 50;
    private int shield = 0;
    
    // Abilities
    private double moveSpeed = 0.15;
    private int damage = 15;
    private long lastShot = 0;
    private int shotCooldown = 150;
    
    // Buffs
    private long speedBoostEnd = 0;
    private long damageBoostEnd = 0;
    private long invincibilityEnd = 0;
    private long timeSlowEnd = 0;
    
    // Combat
    private double aimAngle = 0;
    private boolean invincible = false;
    private int invincibleTimer = 0;
    private int dashCooldown = 0;
    private int ultimateCooldown = 0;
    private int ultimateCharge = 0;
    
    // Inventory
    private List<Item> inventory = new ArrayList<>();
    private int maxInventorySize = 10;
    private Item equippedWeapon = null;
    private Item equippedArmor = null;
    
    // Visual
    private double targetX, targetY;

    public Player(String name, GridMap map, String timeline) {
        this.name = name;
        this.map = map;
        this.timeline = timeline;
        this.x = 2;
        this.y = 2;
        this.visualX = x;
        this.visualY = y;
    }

    public void move(double dx, double dy) {
        double speed = moveSpeed;
        if (System.currentTimeMillis() < speedBoostEnd) speed *= 1.5;
        
        double newX = x + dx * speed;
        double newY = y + dy * speed;
        
        // Collision check
        Node node = map.getNode((int)newX, (int)newY);
        if (node != null && node.isWalkable()) {
            x = newX;
            y = newY;
        } else {
            // Slide along walls
            Node nodeX = map.getNode((int)newX, (int)y);
            Node nodeY = map.getNode((int)x, (int)newY);
            if (nodeX != null && nodeX.isWalkable()) x = newX;
            if (nodeY != null && nodeY.isWalkable()) y = newY;
        }
        
        // Clamp to map bounds (dynamic based on map size)
        double mapBound = map.getSize() - 0.5;
        x = Math.max(0.5, Math.min(mapBound, x));
        y = Math.max(0.5, Math.min(mapBound, y));
    }

    public void updateVisuals() {
        visualX += (x - visualX) * 0.2;
        visualY += (y - visualY) * 0.2;
        
        if (invincible) {
            invincibleTimer--;
            if (invincibleTimer <= 0) invincible = false;
        }
        
        if (dashCooldown > 0) dashCooldown--;
        if (ultimateCooldown > 0) ultimateCooldown--;
    }
    
    public boolean shoot(GameEngine engine) {
        if (ammo <= 0) return false;
        if (System.currentTimeMillis() - lastShot < shotCooldown) return false;
        
        double dx = Math.cos(aimAngle) * 0.25;
        double dy = Math.sin(aimAngle) * 0.25;
        
        int dmg = damage;
        if (System.currentTimeMillis() < damageBoostEnd) dmg = (int)(dmg * 1.5);
        
        engine.fireProjectile(x, y, dx, dy, true);
        ammo--;
        lastShot = System.currentTimeMillis();
        ultimateCharge = Math.min(100, ultimateCharge + 5);
        return true;
    }
    
    public void dash() {
        if (dashCooldown > 0) return;
        
        double dashDist = 3;
        double dx = Math.cos(aimAngle) * dashDist;
        double dy = Math.sin(aimAngle) * dashDist;
        
        // Dash in steps to avoid going through walls
        for (int i = 0; i < 10; i++) {
            double testX = x + dx * i / 10;
            double testY = y + dy * i / 10;
            Node node = map.getNode((int)testX, (int)testY);
            if (node == null || !node.isWalkable()) break;
            x = testX;
            y = testY;
        }
        
        invincible = true;
        invincibleTimer = 20;
        dashCooldown = 60;
    }
    
    public void useUltimate(GameEngine engine) {
        if (ultimateCharge < 100) return;
        
        // Fire projectiles in all directions
        for (int i = 0; i < 16; i++) {
            double angle = i * Math.PI * 2 / 16;
            engine.fireProjectile(x, y, Math.cos(angle) * 0.2, Math.sin(angle) * 0.2, true);
        }
        
        engine.spawnParticles(x, y, "ultimate", 50);
        ultimateCharge = 0;
        ultimateCooldown = 300;
    }
    
    public void takeDamage(int amount, GameEngine engine) {
        if (invincible || System.currentTimeMillis() < invincibilityEnd) return;
        
        if (shield > 0) {
            int absorbed = Math.min(shield, amount);
            shield -= absorbed;
            amount -= absorbed;
        }
        
        health -= amount;
        if (health <= 0) {
            health = 0;
            running = false;
        }
        
        invincible = true;
        invincibleTimer = 30;
        engine.spawnParticles(x, y, "damage", 10);
    }
    
    public void heal(int amount) {
        health = Math.min(maxHealth, health + amount);
    }
    
    public void addEnergy(int amount) {
        energy = Math.min(maxEnergy, energy + amount);
    }
    
    public void addAmmo(int amount) {
        ammo = Math.min(maxAmmo, ammo + amount);
    }
    
    public void addShield(int amount) {
        shield = Math.min(50, shield + amount);
    }
    
    public void applySpeedBoost() {
        speedBoostEnd = System.currentTimeMillis() + 5000;
    }
    
    public void applyDamageBoost() {
        damageBoostEnd = System.currentTimeMillis() + 5000;
    }
    
    public void applyInvincibility() {
        invincibilityEnd = System.currentTimeMillis() + 3000;
    }
    
    public void applyTimeSlow() {
        timeSlowEnd = System.currentTimeMillis() + 4000;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(50);
                if (energy < maxEnergy) energy++;
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void stop() { running = false; }
    
    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getVisualX() { return visualX; }
    public double getVisualY() { return visualY; }
    public String getName() { return name; }
    public String getTimeline() { return timeline; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getEnergy() { return energy; }
    public int getMaxEnergy() { return maxEnergy; }
    public int getAmmo() { return ammo; }
    public int getMaxAmmo() { return maxAmmo; }
    public int getShield() { return shield; }
    public int getDamage() { return damage; }
    public double getAimAngle() { return aimAngle; }
    public boolean isInvincible() { return invincible; }
    public boolean isActive() { return running; }
    public int getDashCooldown() { return dashCooldown; }
    public int getUltimateCharge() { return ultimateCharge; }
    
    public void setAimAngle(double angle) { this.aimAngle = angle; }
    public void setPosition(double x, double y) { this.x = x; this.y = y; }
    
    // Inventory methods
    public boolean addItem(Item item) {
        if (inventory.size() < maxInventorySize) {
            inventory.add(item);
            return true;
        }
        return false;
    }
    
    public void removeItem(Item item) {
        inventory.remove(item);
    }
    
    public List<Item> getInventory() { return inventory; }
    public Item getEquippedWeapon() { return equippedWeapon; }
    public Item getEquippedArmor() { return equippedArmor; }
    
    public void equipWeapon(Item weapon) {
        if (weapon != null && weapon.type == ItemType.WEAPON) {
            equippedWeapon = weapon;
            damage = 15 + weapon.power;
        }
    }
    
    public void equipArmor(Item armor) {
        if (armor != null && armor.type == ItemType.ARMOR) {
            equippedArmor = armor;
            maxHealth = 100 + armor.power;
            if (health > maxHealth) health = maxHealth;
        }
    }
    
    public static class Item {
        public String name;
        public ItemType type;
        public int power;
        public String description;
        
        public Item(String name, ItemType type, int power, String description) {
            this.name = name;
            this.type = type;
            this.power = power;
            this.description = description;
        }
    }
    
    public enum ItemType {
        WEAPON, ARMOR, CONSUMABLE
    }
}
