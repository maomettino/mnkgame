package mnkgame;

import java.util.Random;

public class SaddamHussein implements MNKPlayer {
	private Random rand;
	private MNKBoard B;
	private MNKGameState myWin;
	private MNKGameState yourWin;
	private int TIMEOUT;

	/**
	 * Default empty constructor
	 */
	public SaddamHussein() {
	}


	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		// New random seed for each game
		rand    = new Random(System.currentTimeMillis()); 
		B       = new MNKBoard(M,N,K);
		myWin   = first ? MNKGameState.WINP1 : MNKGameState.WINP2; 
		yourWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1;
		TIMEOUT = timeout_in_secs;
	}
	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		return FC[0];
	} 

	public String playerName() {
		return "SaddamHussein";
	}
}
