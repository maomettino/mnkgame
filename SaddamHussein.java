package mnkgame;

import java.util.Arrays;
import java.util.Random;

import mnkgame.ChainState;
import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.MNKGameState;
/*IMPORTANTE:
l'algoritmo che controlla se io e l'avversario vinciamo in una mossa sono implementati
e funzionano ma ci sono due problemi:
1)viene usato B.B che è un campo protetto
2)per poter selezionare mosse a nome dell'avversario bisogna cambiare
CurrentPlayer(che è protetto) e l'unico modo per farlo è marcare prima una cella 
a caso che non sia quella da testare.
L'idea è di rimuovere la classe board e usare al suo posto una matrice locale
e la funzione isWinningCell, che sono ciò che davvero ci serve della board

*/
public class SaddamHussein implements MNKPlayer {
	private final int MAX_DEPTH = 5; // the max depth that the beta pruning will analyze
	private int[][] direction = { 
			{ -1, 0 }, // up
			{ -1, 1 }, // up-right
			{ 0, 1 }, // right
			{ 1, 1 }, // down-right
			{ 1, 0 }, // down
			{ 1, -1 }, // down-left
			{ 0, -1 }, // left
			{ -1, -1 } // up-left
	};
	private Random rand;
	private MNKBoard B;

	private ChainState chain_state;
	private int m, n, k, chain_length;
	MNKCell knot_cell;
	MNKCell current_best_move;
	int turn;
	MNKCellState whoami;

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
		whoami = first ? MNKCellState.P1 : MNKCellState.P2;
	}

	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		turn++;
		System.out.println("turno: " + turn);
		
		// When it's not my first turn
		if (chain_state != ChainState.nochain) { // it's not my first move
			if(FC.length==1)
				return FC[0];	
			MNKCell my_last_cell = MC[MC.length - 2];
			MNKCell foe_cell = MC[MC.length - 1]; 
			//making the local board up-to-date
			B.markCell(my_last_cell.i, my_last_cell.j);
			B.markCell(foe_cell.i, foe_cell.j);
			//printMatrix(B.B);			
			//check if i can win in one move
			/*if (turn >= k) {
				int result[] = find_winning_move(my_last_cell);
				if(result[0] !=-1)
					return new MNKCell(result[0], result[1]);
			}  */
			//check if the foe can win in one move
			int foe_moves = MC.length - turn + 1;
			if(foe_moves >= k-1) {
				System.out.println("checking foe's moves...");
				int result[] = find_winning_move(foe_cell);
				if(result[0] !=-1) {	//i found a winning cell
					System.out.println("the foe can win with "+ Arrays.toString(result)+", gotta stop him!");
					return new MNKCell(result[0], result[1]);
				}
				System.out.println("no danger for now");
			}
			return FC[0];
		}
		// When it's my first turn
		else
		{
			chain_state = ChainState.newborn;
			if (MC.length > 0) { // if I'm the second player
				MNKCell foe_cell = MC[MC.length - 1]; // Recover the last move from MC
				B.markCell(foe_cell.i, foe_cell.j); // Save the last move in the local MNKBoard
			}
			return FC[0];
		}
	}
	//returns the one-turn-winning move if it exists, returns -1,-1 if it doesn't
	private int[] find_winning_move(MNKCell cell) {
		for (int[] dir : direction) {
			//System.out.println("checking direction " + Arrays.toString(dir));
			int result[] = check_direction(cell.i, cell.j, dir,cell.state);
			//System.out.println("result from find winning move " + Arrays.toString(result));
			if(result[0] !=-1)	//i found a winning cell
				return new int[] {result[0], result[1]};
		}
		return new int[] {-1,-1};
	}
	//from the given cell moves in the given direction and returns the winning cell if it exists, [-1,-1 ] otherwise
	private int[] check_direction(int i, int j, int[] dir, MNKCellState cellstate) {
			//the cell is out of the board bound or it's marked by the other player	
			System.out.println("i and j before" + i+j);
			i += dir[0];
			j += dir[1];
			System.out.println("i and j after" + i+j);
			if(i < 0 || i >= m || j < 0 || j >= n || (B.B[i][j] != MNKCellState.FREE && B.B[i][j] != cellstate ) )
				return new int[] {-1,-1};
			else if (B.B[i][j] == MNKCellState.FREE) {	//the cell is free
				MNKGameState gamestate = B.markCell(i, j);
				System.out.println("the gamestate for direction is "+gamestate);
				if (gamestate == MNKGameState.WINP1 || gamestate == MNKGameState.WINP2) // i win with this																		// // move
					return new int[] {i,j};
				else {
					B.unmarkCell();
					return new int[] {-1,-1};
				}
			}
			//recursive case
			
			return check_direction(i, j, dir,cellstate);
	}
	private void printMatrix(MNKCellState[][] matrix) {
		Arrays.stream(matrix).forEach((row) -> {
			System.out.print("[");
			Arrays.stream(row).forEach((el) -> System.out.print(" " + el + " "));
			System.out.println("]");
		});
	}
/*
	private boolean isWinningCell(int i, int j) {
		MNKCellState s = B[i][j];
		int n;

		// Useless pedantic check
		if(s == MNKCellState.FREE) return false;

		// Horizontal check
		n = 1;
		for(int k = 1; j-k >= 0 && B[i][j-k] == s; k++) n++; // backward check
		for(int k = 1; j+k <  N && B[i][j+k] == s; k++) n++; // forward check   
		if(n >= K) return true;

		// Vertical check
		n = 1;
		for(int k = 1; i-k >= 0 && B[i-k][j] == s; k++) n++; // backward check
		for(int k = 1; i+k <  M && B[i+k][j] == s; k++) n++; // forward check
		if(n >= K) return true;
		

		// Diagonal check
		n = 1;
		for(int k = 1; i-k >= 0 && j-k >= 0 && B[i-k][j-k] == s; k++) n++; // backward check
		for(int k = 1; i+k <  M && j+k <  N && B[i+k][j+k] == s; k++) n++; // forward check
		if(n >= K) return true;

		// Anti-diagonal check
		n = 1;
		for(int k = 1; i-k >= 0 && j+k < N  && B[i-k][j+k] == s; k++) n++; // backward check
		for(int k = 1; i+k <  M && j-k >= 0 && B[i+k][j-k] == s; k++) n++; // backward check
		if(n >= K) return true;

		return false;
	}
*/
	public String playerName() {
		return "SaddamHussein";
	}
}
