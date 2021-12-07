package mnkgame;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class SaddamHussein implements MNKPlayer {
	// TODO: handle time with timer, threads or whatever
	// TODO: look-up and perhaps use junit for unit testing
	// TODO: reycle previously computed stuff in beta-pruning if possible
	private AlphaBetaPruning abp;
	private int turn;
	/**
	 * Default empty constructor
	 */
	public SaddamHussein() {
	}

	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		turn = 0;
		abp = new AlphaBetaPruning(M,N,K,first,timeout_in_secs);
	}

	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		turn++;
		System.out.println("turno: " + turn);
		if (turn > 1) {
			if (FC.length == 1)
				return FC[0];
			MNKCell saddamLastCell = MC[MC.length - 2];
			MNKCell foeLastCell = MC[MC.length - 1];
			abp.getMove(saddamLastCell, foeLastCell);
			return FC[0];
		}
		// When it's my first turn
		else {
			if (MC.length > 0) { // if I'm the second player
				MNKCell foeCell = MC[MC.length - 1]; // Recover the last move from MC
				abp.signFoeMove(foeCell);
			}
			//choose the first move for saddam
			return FC[0];
		}

	}

	public String playerName() {
		return "SaddamHussein";
	}
}
