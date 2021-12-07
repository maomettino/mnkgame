package mnkgame;

public class Move extends MNKCell {
    //length of the chain(if it exists) that passes through this move and the last move
    public int length;
    public boolean win;

    public Move(int i, int j) {
        super(i, j);
        this.length = 0;
        win = false;
    }

    public Move(int i, int j ,int length) {
        super(i, j);
        this.length = length;
        win = false;
    }

    public Move(int i, int j , boolean win) {
        super(i, j);
        this.length = 0;
        this.win = win;
    }
}
