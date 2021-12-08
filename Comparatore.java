package mnkgame;
import java.util.Comparator;
public class Comparatore implements Comparator<Move> {

//what we need is the reverse natural order, hence the existence of this comparator
    @Override
    public int compare(int[] m1, int[] m2) {
        if (m1[2] < m2[2])
            return 1;
        else if (m1[2] > m2[2])
            return -1;
        return 0;
        }
}
