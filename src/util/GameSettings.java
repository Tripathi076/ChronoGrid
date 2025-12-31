package util;

public class GameSettings {
    public enum Difficulty { EASY, MEDIUM, HARD }
    private static Difficulty selectedDifficulty = Difficulty.MEDIUM;

    public static void setDifficulty(Difficulty difficulty) {
        selectedDifficulty = difficulty;
    }

    public static Difficulty getDifficulty() {
        return selectedDifficulty;
    }
}
