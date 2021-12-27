package mnkgame;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Stack;

public class SaddamHussein implements MNKPlayer {
	// TODO: handle time with timer, threads or whatever
	// TODO: look-up and perhaps use junit for unit testing
	// TODO: reycle previously computed stuff in beta-pruning if possible
	private AlphaBetaPruning abp;
	private int turn;
	private MNKCellState[][] board;
	private MNKCellState saddam;
	private MNKCellState foe;
	private Stack<int[]> saddamHistory;
	private Stack<int[]> foeHistory;
	/**
	 * Default empty constructor
	 */
	public SaddamHussein() {
	}

	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		turn = 0;
		abp = new AlphaBetaPruning(M,N,K,first,timeout_in_secs);
		saddam = first ? MNKCellState.P1 : MNKCellState.P2;
		foe = first ? MNKCellState.P2 : MNKCellState.P1;
		board = new MNKCellState[M][N];
		for (int i = 0; i < M; i++)
			for (int j = 0; j < N; j++)
				board[i][j] = MNKCellState.FREE;
		saddamHistory = new Stack<int[]>();
		foeHistory = new Stack<int[]>();
	}

	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		turn++;
		System.out.println("turno: " + turn);
		if (turn > 1) {
			if (FC.length == 1)
				return FC[0];
			MNKCell saddamLastCell = MC[MC.length - 2];
			MNKCell foeLastCell = MC[MC.length - 1];
			board[saddamLastCell.i][saddamLastCell.j] = saddam;
			board[foeLastCell.i][foeLastCell.j] = foe;
			saddamHistory.push(new int[] {saddamLastCell.i, saddamLastCell.j});
			foeHistory.push(new int[] {foeLastCell.i, foeLastCell.j});
			MNKCell cell = abp.clandestino(saddamLastCell, foeLastCell, board, saddamHistory, foeHistory);
			//abp.test();
			//return FC[0];
			return cell.i==-1?FC[0]:cell;
		}
		// When it's my first turn
		else {
			//abp.test();
			if (MC.length > 0) { // if I'm the second player
				MNKCell foeCell = MC[MC.length - 1]; // Recover the last move from MC
				board[foeCell.i][foeCell.j]=foe;
				foeHistory.push(new int[] {foeCell.i, foeCell.j});
				abp.signFoeMove(foeCell);
			}
			//choose the first move for saddam
			//return FC[0];
			return FC[0];
		}

	}

	public String playerName() {
		return "SaddamHussein";
	}
}
