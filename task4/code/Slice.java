public final class Slice {
    private final Integer start;
    private final Integer stop;
    private final int step;

    public Slice(Integer start, Integer stop, int step) {
        if (step == 0) {
            throw new IllegalArgumentException("step cannot be 0");
        }
        this.start = start;
        this.stop = stop;
        this.step = step;
    }

    public static Slice parse(String text) {
        String[] parts = text.trim().split(":", -1);
        if (parts.length < 2 || parts.length > 3) {
            throw new IllegalArgumentException(
                "'" + text + "' must look like start:stop or start:stop:step");
        }
        Integer start = parsePart(parts[0]);
        Integer stop = parsePart(parts[1]);
        Integer step = parts.length == 3 ? parsePart(parts[2]) : null;
        return new Slice(start, stop, step == null ? 1 : step);
    }

    private static Integer parsePart(String part) {
        part = part.trim();
        if (part.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + part + "' is not an integer");
        }
    }


    public int[] indices(int length) {
        int first;
        int last;  
        if (step > 0) {
            first = start == null ? 0 : clamp(start, length, 0, length);
            last = stop == null ? length : clamp(stop, length, 0, length);
        } else {
            first = start == null ? length - 1 : clamp(start, length, -1, length - 1);
            last = stop == null ? -1 : clamp(stop, length, -1, length - 1);
        }

        int distance = step > 0 ? last - first : first - last;
        int magnitude = Math.abs(step);
        int count = distance > 0 ? (distance + magnitude - 1) / magnitude : 0;

        int[] result = new int[count];
        for (int n = 0; n < count; n++) {
            result[n] = first + n * step;
        }
        return result;
    }

    private static int clamp(int value, int length, int low, int high) {
        if (value < 0) {
            value += length;
        }
        return Math.max(low, Math.min(high, value));
    }
}