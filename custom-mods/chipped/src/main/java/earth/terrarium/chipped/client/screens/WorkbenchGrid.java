package earth.terrarium.chipped.client.screens;

public final class WorkbenchGrid {
    public static final int COLUMNS = 9;
    public static final int VISIBLE_ROWS = 6;
    public static final int CELL_SIZE = 18;
    public static final int TOP = 41;
    public static final int CLIP_TOP = 40;
    public static final int CLIP_BOTTOM = 149;

    private WorkbenchGrid() {}

    public static int rows(int resultCount) {
        return Math.max(VISIBLE_ROWS, (resultCount + COLUMNS - 1) / COLUMNS);
    }

    public static int index(int column, int row) {
        return column + row * COLUMNS;
    }

    public static double clampScroll(double amount, int resultCount) {
        return Math.clamp(amount, 0, (rows(resultCount) - VISIBLE_ROWS) * CELL_SIZE);
    }

    public static int gridY(int top, double scrollAmount) {
        return top + TOP - (int) scrollAmount;
    }

    public static boolean containsY(double mouseY, int minY, int maxY) {
        return mouseY >= minY && mouseY < maxY;
    }
}
