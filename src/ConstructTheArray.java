import java.io.*;
import java.math.*;
import java.security.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import static java.lang.Integer.parseInt;

public class ConstructTheArray {
    static final long MOD = 1_000_000_007L;

    public static long countArray(int n, int k, int x) {
        if (n < 3 || k < 2)
            return 0;
        long count1 = k - 1, countX = k - 2;
        while (n-- > 3) {
            long temp = countX * (k - 1) % MOD;
            countX = (count1 + countX * (k - 2)) % MOD;
            count1 = temp;
        }
        if (x == 1) {
            return count1;
        }
        return countX;
    }

    public static void main(String[] args) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(System.getenv("OUTPUT_PATH")));
        Scanner scanner = new Scanner(System.in);
        String[] nkx = scanner.nextLine().split(" ");

        int n = parseInt(nkx[0]);
        int k = parseInt(nkx[1]);
        int x = parseInt(nkx[2]);

        long answer = countArray(n, k, x);

        bufferedWriter.write(String.valueOf(answer));
        bufferedWriter.newLine();

        bufferedWriter.close();

        scanner.close();
    }
}
