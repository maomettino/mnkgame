package mnkgame;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class SaddamHussein extends P implements MNKPlayer  {
	// TODO: handle time with timer, threads or whatever
	// TODO: look-up and perhaps use junit for unit testing
	// TODO: reycle previously computed stuff in beta-pruning if possible
	private ChainState chainState;
	private AlphaBetaPruning abp;

	/**
	 * Default empty constructor
	 */
	public SaddamHussein() {
	}

	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		P.b = new MNKCellState[M][N];
		for (int i = 0; i < M; i++)
			for (int j = 0; j < N; j++)
				P.b[i][j] = MNKCellState.FREE;
		P.m = M;
		P.n = N;
		P.k = K;
		P.turn = 0;
		chainState = ChainState.nochain;
		P.me = first ? MNKCellState.P1 : MNKCellState.P2;
		P.foe = first ? MNKCellState.P2 : MNKCellState.P1;
		abp = new AlphaBetaPruning();
	}

	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		P.turn++;
		System.out.println("turno: " + P.turn);
		// When it's not my first turn
		if (chainState != ChainState.nochain) {
			if (FC.length == 1)
				return FC[0];
			MNKCell myLastCell = MC[MC.length - 2];
			MNKCell foeCell = MC[MC.length - 1];
			// making the local board up-to-date
			P.b[myLastCell.i][myLastCell.j] = P.me;
			P.b[foeCell.i][foeCell.j] = P.foe;
		}
		// When it's my first turn
		else {
			chainState = ChainState.newborn;
			if (MC.length > 0) { // if I'm the second player
				MNKCell foeCell = MC[MC.length - 1]; // Recover the last move from MC
				P.b[foeCell.i][foeCell.j] = P.foe;
			}
		}
		// here goes the beta-pruning algorithm
		return FC[0];
	}

	public String playerName() {
		return "SaddamHussein";
	}
}
