package mnkgame;

public class Node {

    public final int i;
    public final int j;
    public int alpha;
    public int beta;
    public int value;
    public final int iParent;
    public final int jParent;
    public Node bestChild;
    public boolean isSaddam;
    public boolean isLeaf;

    //constructor for regular nodes
    public Node(int i, int j, int alpha, int beta, int value, int iParent, int jParent, boolean isSaddam) {
        this.i = i;
        this.j = j;
        this.alpha = alpha;
        this.beta = beta;
        this.value = value;
        this.iParent = iParent;
        this.jParent = jParent;
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
        this.iParent = 0;
        this.jParent = 0;
    }

}
