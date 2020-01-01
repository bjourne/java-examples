import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class Sorting {
    public static <T> void mergeSort(T[] a,
                                     Comparator<? super T> c,
                                     boolean ascending) {
        Comparator<? super T> actualComparator = ascending ? c : c.reversed();
        mergeSort(a, 0, a.length, actualComparator);
    }
    public static <T> void mergeSort(T[] a,
                                     int fromIndex, int toIndex,
                                     Comparator<? super T> c) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(c);
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException(
                "fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
        }
        if (fromIndex < 0) {
            throw new ArrayIndexOutOfBoundsException(fromIndex);
        }
        if (toIndex > a.length) {
            throw new ArrayIndexOutOfBoundsException(toIndex);
        }
        T[] buf = Arrays.copyOf(a, a.length);
        mergeSortInternal(a, fromIndex, toIndex, c, buf);
    }
    private static <T> void mergeSortInternal(T[] a,
                                              int fromIndex, int toIndex,
                                              Comparator<? super T> c,
                                              T[] buf) {
        if (toIndex - fromIndex <= 1)
            return;
        int mid = fromIndex + (toIndex - fromIndex) / 2;
        mergeSortInternal(a, fromIndex, mid, c, buf);
        mergeSortInternal(a, mid, toIndex, c, buf);
        merge(a, fromIndex, mid, toIndex, c, buf);
    }
    private static <T> void merge(T[] a,
                                  int fromIndex, int mid, int toIndex,
                                  Comparator<? super T> c,
                                  T[] buf) {
        System.arraycopy(a, fromIndex, buf, fromIndex, toIndex - fromIndex);
        int lPos = fromIndex;
        int rPos = mid;
        int lEnd = mid;
        int rEnd = toIndex;
        int pos = fromIndex;
        while (lPos < lEnd && rPos < rEnd) {
            if (c.compare(buf[lPos], buf[rPos]) <= 0) {
                a[pos++] = buf[lPos++];
            } else {
                a[pos++] = buf[rPos++];
            }
        }
        while (lPos < lEnd) {
            a[pos++] = buf[lPos++];
        }
        while (rPos < rEnd) {
            a[pos++] = buf[rPos++];
        }
    }
}
