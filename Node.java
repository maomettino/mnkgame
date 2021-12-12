package mnkgame;

public class Node {

    public final int i;
    public final int j;
    public int alpha;
    public int beta;
    public int value;
    public Node bestChild;
    public boolean isSaddam;
    public boolean isLeaf;
    public int[] myLastRegularMove;
    public int[] foeLastRegularMove;
    //constructor for regular nodes
    public Node(int i, int j, int alpha, int beta, int value, int[] myLastRegularMove, int[] foeLastRegularMove, boolean isSaddam) {
        this.i = i;
        this.j = j;
        this.alpha = alpha;
        this.beta = beta;
        this.value = value;
        this.myLastRegularMove = new int[2];
        this.myLastRegularMove[0] = myLastRegularMove[0];
        this.myLastRegularMove[1] = myLastRegularMove[1];
        this.foeLastRegularMove = new int[2];
        this.foeLastRegularMove[0] = foeLastRegularMove[0];
        this.foeLastRegularMove[1] = foeLastRegularMove[1];
        this.isSaddam = isSaddam;
        this.isLeaf = false;
    }

    //contructor for leaf nodes
    public Node(int i, int j, boolean isSaddam, int value) {
        this.i = i;
        this.j = j;
        this.value = value;
        this.isSaddam = isSaddam;
        this.isLeaf = true;
    }

}
