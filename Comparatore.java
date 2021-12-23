package mnkgame;
import java.util.Comparator;
public class Comparatore implements Comparator<int[]> {

//what we need is the reverse natural order, hence the existence of this comparator
    @Override
    public int compare(int[] m1, int[] m2) {
        if(m1==null || m2==null) {
            System.out.println(m1==null?"m1 null":""+" "+m2==null?"m2 null":"");
            return 0;
        }

        if (m1[2] < m2[2])
            return 1;
        else if (m1[2] > m2[2])
            return -1;
        return 0;
        }
}
