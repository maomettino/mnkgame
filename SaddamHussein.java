package mnkgame;
import java.util.Arrays;
import java.util.Random;

public class SaddamHussein implements MNKPlayer {
	private Random rand;
	private MNKBoard B;
	private Direction direction;
	private ChainState chain_state;
	private int m,n,k, chain_length;
	MNKCell knot_cell;
	int turn;
	/**
	 * Default empty constructor
	 */
	public SaddamHussein() {
	}


	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		// New random seed for each game
		rand    = new Random(System.currentTimeMillis()); 
		B       = new MNKBoard(M,N,K);
		m=M;
		n=N;
		k=K;
		chain_length = 0;
		turn=0;
	}
	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		if(MC.length>0) {
			MNKCell c = MC[MC.length-1]; // Recover the last move from MC
			B.markCell(c.i,c.j);         // Save the last move in the local MNKBoard
		}
		B.markCell(FC[0].i,FC[0].j);
		printMatrix(B.B);
		return FC[0];
	} 

	private void printMatrix(MNKCellState[][] matrix) {
		turn++;
		System.out.println("turno: "+turn);
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
