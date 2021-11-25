package mnkgame;

public class Node extends P{

    public final int i;
    public final int j;
    public int alpha;
    public int beta;
    public int value;

    public Node(int i, int j, int alpha, int beta, boolean isSaddam) {
        this.i = i;
        this.j = j;
        this.alpha = alpha;
        this.beta = beta;
        this.value = isSaddam?P.ALPHA:P.BETA;
    }

    //contructor for leaf nodes
    public Node(int i, int j, int value) {
        this.i = i;
        this.j = j;
        this.value = value;
    }

}
