package mnkgame;
/**
 * shared parameters between Saddam and alphaBetaPruning
 */

public class P {
    static protected int m,n,k;
    static protected MNKCellState[][] b;
    static protected int turn;
    static protected MNKCellState me;
	static protected MNKCellState foe;
    static protected int myMoves=0, foeMoves=0;
    protected static final int ALPHA = -1;
	protected static final int BETA = 1;
}