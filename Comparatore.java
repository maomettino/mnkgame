package mnkgame;
import java.util.Comparator;
public class Comparatore implements Comparator<Move> {

//what we need is the reverse natural order, hence the existence of this comparator
    @Override
    public int compare(Move m1, M m2) {
        if (m1.length < m2.length)
            return 1;
        else if (m1.length > m2.length)
            return -1;
        return 0;
        }
}
