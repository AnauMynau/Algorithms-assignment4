package smart.scheduling.common.util;

public final class Preconditions {
    public static int checkIndex(int idx, int n) {
        if (idx < 0 || idx >= n) throw new IndexOutOfBoundsException("index=" + idx + " n=" + n);
        return idx;
    }
    public static <T> T checkNotNull(T x, String msg) {
        if (x == null) throw new IllegalArgumentException(msg);
        return x;
    }
    public static void check(boolean cond, String msg) {
        if (!cond) throw new IllegalArgumentException(msg);
    }
}
