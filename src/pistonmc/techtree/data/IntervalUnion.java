package pistonmc.techtree.data;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import pistonmc.techtree.ModMain;
import pistonmc.techtree.adapter.IDeserializer;
import pistonmc.techtree.adapter.ISerializer;

public class IntervalUnion {
    static class Interval implements Comparable<Interval> {
        /** inclusive start */
        int start;
        /** inclusive end */
        int end;
        public Interval(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public Interval copy() {
            return new Interval(this.start, this.end);
        }
        @Override
        public int compareTo(Interval o) {
            if (o.start > this.end + 1) {
                return -1;
            }
            if (o.end + 1 < this.start) {
                return 1;
            }
            return 0;
        }

        public boolean intersects(Interval other) {
            if (other.start > this.end) {
                return false;
            }
            if (other.end < this.start) {
                return false;
            }
            return true;
        }
        public void writeTo(ISerializer serializer) {
            if (this.start == this.end) {
                serializer.writeBoolean(true);
                serializer.writeInt(this.start);
                return;
            }
            serializer.writeBoolean(false);
            serializer.writeInt(this.start);
            serializer.writeInt(this.end);
        }

        public static Interval readFrom(IDeserializer deserializer) {
            boolean isSingle = deserializer.readBoolean();
            if (isSingle) {
                int value = deserializer.readInt();
                return new Interval(value, value);
            }
            int start = deserializer.readInt();
            int end = deserializer.readInt();
            return new Interval(start, end);
        }

        public String toString() {
            if (this.start == this.end) {
                return Integer.toString(this.start);
            }
            return this.start + "-" + this.end;
        }

    }

    /** 
     * Parses a string into an IntervalUnion.
     *
     * The unions should be separated by commas. Each union can be a single number, or `start-end` where `start` and `end` are numbers, or *
     *
     * Returns null if parse fails
     */
    public static IntervalUnion parse(String s) {
        s = s.trim();
        if (s.equals("*")) {
            IntervalUnion union = new IntervalUnion(null);
            return union;
        }
        String[] parts = s.split(",");
        List<Interval> intervals = new ArrayList<>(parts.length);
        for (String part: parts) {
            int i = part.indexOf('-');
            if (i < 0) {
                try {
                    int value = Integer.parseInt(part.trim());
                    Interval interval = new Interval(value, value);
                    intervals.add(interval);
                } catch (NumberFormatException e) {
                    ModMain.error(e);
                    return null;
                }
                continue;
            } 
            String startStr = part.substring(0, i).trim();
            String endStr = part.substring(i + 1).trim();
            try {
                int start = Integer.parseInt(startStr);
                int end = Integer.parseInt(endStr);
                Interval interval = new Interval(start, end);
                intervals.add(interval);
            } catch (NumberFormatException e) {
                ModMain.error(e);
                return null;
            }
        }
        return new IntervalUnion(intervals);
    }

    /** The intervals that make up the union. null to match anything*/
    private TreeSet<Interval> intervals;


    public IntervalUnion(List<Interval> intervals) {
        if (intervals != null) {
            this.intervals = new TreeSet<>();
            for (Interval interval : intervals) {
                this.union(interval);
            }
        } else {
            this.intervals = null;
        }
    }

    public void union(int start, int end) {
        Interval interval = new Interval(start, end);
        this.union(interval);
    }

    public void union(Interval interval) {
        if (this.intervals == null) {
            return;
        }
        Interval ceiling = this.intervals.ceiling(interval);
        if (ceiling == null) {
            this.intervals.add(interval);
            return;
        }

        if (ceiling.compareTo(interval) != 0) {
            // doesn't intersect or connect
            this.intervals.add(interval);
            return;
        }
        this.intervals.remove(ceiling);
        ceiling.end = Math.max(ceiling.end, interval.end);
        ceiling.start = Math.min(ceiling.start, interval.start);
        this.union(ceiling);
    }

    public void union(IntervalUnion other) {
        if (this.intervals == null) {
            return;
        }
        if (other.intervals == null) {
            this.intervals = null;
            return;
        }
        for (Interval interval : other.intervals) {
            this.union(interval);
        }
    }

    /** Returns any value from the union. Returns 0 if the union is empty */
    public int anyValue() {
        if (this.intervals == null) {
            return 0;
        }
        if (this.intervals.isEmpty()) {
            return 0;
        }

        return this.intervals.first().start;
    }

    public boolean contains(int value) {
        return this.intersects(new Interval(value, value));
    }

    public boolean intersects(int start, int end) {
        return this.intersects(new Interval(start, end));
    }

    public boolean intersects(Interval interval) {
        if (this.intervals == null) {
            return true;
        }
        for (Interval i: this.intervals.subSet(interval, true, interval, true)) {
            if (i.intersects(interval)) {
                return true;
            }
        }
        return false;
    }

    public boolean intersects(IntervalUnion other) {
        if (this.intervals == null) {
            return true;
        }
        if (other.intervals == null) {
            return true;
        }
        for (Interval interval : other.intervals) {
            if (this.intersects(interval)) {
                return true;
            }
        }
        return false;
    }

    public IntervalUnion copy() {
        if (this.intervals == null) {
            IntervalUnion u = new IntervalUnion(null);
            return u;
        }
        IntervalUnion u = new IntervalUnion(new ArrayList<>());
        for (Interval interval : this.intervals) {
            u.union(interval.copy());
        }
        return u;
    }

    public String toString() {
        if (this.intervals == null) {
            return "*";
        }

        StringBuilder sb = new StringBuilder();
        for (Interval interval : this.intervals) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(interval);
        }
        return sb.toString();
    }

    public void writeTo(ISerializer serializer) {
        if (this.intervals == null) {
            serializer.writeBoolean(true);
            return;
        }
        serializer.writeBoolean(false);
        serializer.writeInt(this.intervals.size());
        for (Interval interval : this.intervals) {
            interval.writeTo(serializer);
        }
    }

    public static IntervalUnion readFrom(IDeserializer deserializer) {
        boolean isNull = deserializer.readBoolean();
        if (isNull) {
            return new IntervalUnion(null);
        }
        int size = deserializer.readInt();
        List<Interval> intervals = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            intervals.add(Interval.readFrom(deserializer));
        }
        return new IntervalUnion(intervals);
    }
}
