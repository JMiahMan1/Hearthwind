package earth.terrarium.chipped.client.screens;

public final class WorkbenchInteractionTests {
    private static int assertions;

    public static void main(String[] args) {
        gridMath();
        System.out.println("Workbench interaction assertions passed: " + assertions);
    }

    private static void gridMath() {
        check(WorkbenchGrid.COLUMNS == 9, "columns");
        check(WorkbenchGrid.VISIBLE_ROWS == 6, "visible rows");
        check(WorkbenchGrid.CELL_SIZE == 18, "cell size");
        check(WorkbenchGrid.TOP == 41, "grid top");
        check(WorkbenchGrid.CLIP_TOP == 40, "clip top");
        check(WorkbenchGrid.CLIP_BOTTOM == 149, "clip bottom");
        check(WorkbenchGrid.CLIP_BOTTOM - WorkbenchGrid.CLIP_TOP == 109, "clip height");
        for (int count : new int[]{0, 1, 9, 53, 54, 55, 63, 64, 137}) {
            int rows = Math.max(6, (count + 8) / 9);
            check(WorkbenchGrid.rows(count) == rows, "rows " + count);
            double maximum = Math.max(0, (rows - 6) * 18);
            for (double amount : new double[]{-10, 0, 8, 17.9, 18, 27, 10000, 0}) {
                double scroll = WorkbenchGrid.clampScroll(amount, count);
                check(scroll == Math.clamp(amount, 0, maximum), "scroll clamp " + count);
                for (int top : new int[]{0, 37}) {
                    check(WorkbenchGrid.gridY(top, scroll) == top + 41 - (int) scroll, "grid y");
                    for (int column = 0; column < 9; column++) {
                        for (int row = 0; row < rows; row++) {
                            check(WorkbenchGrid.index(column, row) == column + row * 9, "index map");
                        }
                    }
                    for (int y = top + 39; y <= top + 150; y++) {
                        check(WorkbenchGrid.containsY(y, top + 40, top + 149) == (y >= top + 40 && y < top + 149), "clip window");
                    }
                }
            }
        }
    }

    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) throw new AssertionError(message);
    }
}
