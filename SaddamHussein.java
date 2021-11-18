package mnkgame;

import java.util.Arrays;
import java.util.Random;

import mnkgame.ChainState;
import mnkgame.Direction;
import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.MNKGameState;


public class SaddamHussein implements MNKPlayer {
	private final int MAX_DEPTH = 5; //the max depth that the beta pruning will analyze
	private int[][] direction = { 	
		{-1,0}, //up
		{-1,1},	//up-right
		{0,1},	//right
		{1,1},	//down-right
		{1,0},	//down
		{1,-1},	//down-left
		{0,-1},	//left
		{-1,-1}, //up-left
	};
	private Random rand;
	private MNKBoard B;
	private ChainState chain_state;
	private int m, n, k, chain_length;
	MNKCell knot_cell;
	MNKCell current_best_move;
	int turn;

	/**
	 * Default empty constructor
	 */
	public SaddamHussein() {
	}

	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		// New random seed for each game
		rand = new Random(System.currentTimeMillis());
		B = new MNKBoard(M, N, K);
		m = M;
		n = N;
		k = K;
		chain_length = 0;
		turn = 0;
		chain_state = ChainState.nochain;
	}

	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		// When it's not my first turn
		if (chain_state != ChainState.nochain) { // it's not my first move
			MNKCell foe_cell = MC[MC.length - 1]; // Recover the last move from MC
			B.markCell(foe_cell.i, foe_cell.j); // Save the last move of the opponent in the local MNKBoard
			MNKCell last_cell = MC[MC.length - 2]; // Save my last move in the local MNKBoard
			int lastI = last_cell.i;
			int lastJ = last_cell.j;
			for (int[] dir : direction)
			{
				try {
					if( B.B[lastI + dir[0]][lastJ + dir[1]] == MNKCellState.FREE) {
						MNKGameState gamestate = B.markCell(lastI + dir[0], lastJ + dir[1]);
						if(gamestate==MNKGameState.WINP1 || gamestate==MNKGameState.WINP2) //i win with this move
							return new MNKCell(lastI + dir[0], lastJ + dir[1]);
						else 
							B.unmarkCell();
					}
					else {
					//check if the cell is marked by me, if so
					//keep going through current direction until you come
					//a cross an enemy cell or a free cell
					//in the latter case check the cell ans check if you win
					}
				}
				catch (IndexOutOfBoundsException e) {
					System.out.println("IndexOutOfBoundsException exception caught, skip to next directionò");
					continue;
				}		
			} 
					
			printMatrix(B.B);
			return FC[0];
		}
		// When it's my first turn
		else {
			chain_state = ChainState.newborn;
			if (MC.length > 0) { // if I'm the second player
				MNKCell foe_cell = MC[MC.length - 1]; // Recover the last move from MC
				B.markCell(foe_cell.i, foe_cell.j); // Save the last move in the local MNKBoard
			}
			B.markCell(FC[0].i, FC[0].j); //TODO centrare la prima mossa
			printMatrix(B.B);
			return FC[0];
		}
	}

	private void printMatrix(MNKCellState[][] matrix) {
		turn++;
		System.out.println("turno: " + turn);
		Arrays.stream(matrix).forEach((row) -> {
			System.out.print("[");
			Arrays.stream(row).forEach((el) -> System.out.print(" " + el + " "));
			System.out.println("]");
		});
	}

	public String playerName() {
		return "SaddamHussein";
	}
}
