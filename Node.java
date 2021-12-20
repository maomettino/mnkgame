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
    public int iFather;
    public int jFather;
    //constructor for regular nodes
    public Node(int i, int j, int alpha, int beta, int value, int iFather, int jFather, boolean isSaddam) {
        this.i = i;
        this.j = j;
        this.alpha = alpha;
        this.beta = beta;
        this.value = value;
        this.isSaddam = isSaddam;
        this.isLeaf = false;
        this.iFather = iFather;
        this.jFather = jFather;
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
