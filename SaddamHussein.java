package mnkgame;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class SaddamHussein implements MNKPlayer {
	//TODO: handle time with timer, threads or whatever
	//TODO: look-up and perhaps use junit for unit testing
	//TODO: reycle previously computed stuff in beta-pruning if possible
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
	private  MNKCellState[][]    B;
	private ChainState chain_state;
	private int m, n, k, chain_length;
	MNKCell knot_cell;
	MNKCell current_best_move;
	int turn;
	MNKCellState me;
	MNKCellState foe;
	//beta-pruning parameters
	final int BETA = 1000;
	final int ALPHA = -1000;

	/**
	 * Default empty constructor
	 */
	public SaddamHussein() {
	}

	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		// New random seed for each game
		rand = new Random(System.currentTimeMillis());
		B  = new MNKCellState[M][N];
		for(int i = 0; i < M; i++)
			for(int j = 0; j < N; j++)
				B[i][j] = MNKCellState.FREE;
		m = M;
		n = N;
		k = K;
		chain_length = 0;
		turn = 0;
		chain_state = ChainState.nochain;
		me = first ? MNKCellState.P1 : MNKCellState.P2;
		foe = first ? MNKCellState.P2 : MNKCellState.P1;
	}

	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		turn++;
		System.out.println("turno: " + turn);		
		// When it's not my first turn
		if (chain_state != ChainState.nochain) { 
			if(FC.length==1)
				return FC[0];	
			MNKCell my_last_cell = MC[MC.length - 2];
			MNKCell foe_cell = MC[MC.length - 1]; 
			//making the local board up-to-date
			B[my_last_cell.i][my_last_cell.j] = me;
			B[foe_cell.i][foe_cell.j] = foe;		
			//check if i can win in one move
			if (turn >= k) {
				int result[] = find_winning_move(my_last_cell);
				if(result[0] !=-1)
					return new MNKCell(result[0], result[1]);
			}  
			//check if the foe can win in one move
			int foe_moves = MC.length - turn + 1;
			if(foe_moves >= k-1) {
				System.out.println("checking foe's moves...");
				int result[] = find_winning_move(foe_cell);
				if(result[0] !=-1) {	//i found a winning cell
					return new MNKCell(result[0], result[1]);
				}
				System.out.println("no danger for now");
			}
		}
		// When it's my first turn
		else
		{
			chain_state = ChainState.newborn;
			if (MC.length > 0) { // if I'm the second player
				MNKCell foe_cell = MC[MC.length - 1]; // Recover the last move from MC
				B[foe_cell.i][foe_cell.j] = foe;				
			}	
		}
		//here goes the beta-pruning algorithm
		//return beta_pruning(order_moves(FC),true,ALPHA,BETA,0,0);
		//order_moves(FC);
		return FC[0];
	}

	private ArrayList<MNKCell> order_moves( MNKCell[] FC) {
		List<MNKCell> list = Arrays.asList(FC); 
		ArrayList<MNKCell> moves = new ArrayList<MNKCell>(list);
		//System.out.println(moves.get(0).state);
		//order moves according to heuristic
		return moves;
	}
//beta_pruning should return MNKCell or int?
	private int beta_pruning(ArrayList<MNKCell> moves, Boolean maximizingPlayer,int alpha, int beta, int nodeIndex, int depth ) {
		//the moves array is already ordered, so we pick up the first move,
		//then the second and so on inside the for
		//i have another terminating condition which occurs when the move
		//of the current node ends the game somehow, to do so 
		//i must keep track of all the moves made in a root-leaf path in a copy
		//of the local board
		MNKCell c = moves.get(0);
		if( isWinningCell(c.i,c.j,me) ) {
			//maximum positive/negative value if i win/lose from root player pov
			return 0;
		}

		// Terminating condition. i.e
        // leaf node is reached
        if (depth == MAX_DEPTH) {
			//heuristic value for the node get_heuristic_value(moves[0])
			return 0;
		}
            

        if (maximizingPlayer) {
            int best = ALPHA;

            // Recur for left and
            // right children
			//this will be more likely a foreach through moves
            for (int i = 0; i < moves.size(); i++) {
				//remove the marked move from moves before the recursive call
                int val = 0;//beta_pruning(depth + 1, nodeIndex * 2 + i, false, moves, alpha, beta);
                best = Math.max(best, val);
                alpha = Math.max(alpha, best);

                // Alpha Beta Pruning
                if (beta <= alpha)
                    break;
            }
            return best;
        } else {
            int best = BETA;

            // Recur for left and
            // right children
			
            for (int i = 0; i < 2; i++) {

                int val = 0;//beta_pruning(depth + 1, nodeIndex * 2 + i, true, moves, alpha, beta);
                best = Math.min(best, val);
                beta = Math.min(beta, best);

                // Alpha Beta Pruning
                if (beta <= alpha)
                    break;
            }
            return best;
        }		
	}

	//returns the one-turn-winning move if it exists, returns -1,-1 if it doesn't
	private int[] find_winning_move(MNKCell cell) {
		for (int[] dir : direction) {
			int result[] = check_direction(cell.i, cell.j, dir,cell.state);
			if(result[0] !=-1)	//i found a winning cell
				return new int[] {result[0], result[1]};
		}
		return new int[] {-1,-1};
	}

	//from the given cell moves in the given direction and returns the winning cell if it exists, [-1,-1 ] otherwise
	private int[] check_direction(int i, int j, int[] dir, MNKCellState cellstate) {
			//the cell is out of the board bound or it's marked by the other player	
			i += dir[0];
			j += dir[1];
			if(i < 0 || i >= m || j < 0 || j >= n || (B[i][j] != MNKCellState.FREE && B[i][j] != cellstate ) )
				return new int[] {-1,-1};
			else if (B[i][j] == MNKCellState.FREE) {	//the cell is free
				Boolean win = isWinningCell(i, j, cellstate);
				if(win)
					return new int[] {i,j};
				else {
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

	//marks the given cell for the given player and checks if he wins, but does it really work?
	private boolean isWinningCell(int i, int j, MNKCellState player) {
		//can't check a cell if it's already marked
		if(B[i][j] != MNKCellState.FREE) return false;
		int c;
		// Horizontal check
		c = 1;
		for(int k = 1; j-k >= 0 && B[i][j-k] == player; k++) c++; // backward check
		for(int k = 1; j+k <  n && B[i][j+k] == player; k++) c++; // forward check   
		if(c >= k) return true;

		// Vertical check
		c = 1;
		for(int k = 1; i-k >= 0 && B[i-k][j] == player; k++) c++; // backward check
		for(int k = 1; i+k <  m && B[i+k][j] == player; k++) c++; // forward check
		if(c >= k) return true;
		

		// Diagonal check
		c = 1;
		for(int k = 1; i-k >= 0 && j-k >= 0 && B[i-k][j-k] == player; k++) c++; // backward check
		for(int k = 1; i+k <  m && j+k <  n && B[i+k][j+k] == player; k++) c++; // forward check
		if(c >= k) return true;

		// Anti-diagonal check
		c = 1;
		for(int k = 1; i-k >= 0 && j+k < n  && B[i-k][j+k] == player; k++) c++; // backward check
		for(int k = 1; i+k <  m && j-k >= 0 && B[i+k][j-k] == player; k++) c++; // backward check
		if(c >= k) return true;

		return false;
	}

	public String playerName() {
		return "SaddamHussein";
	}
}
