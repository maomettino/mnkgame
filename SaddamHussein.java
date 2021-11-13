package mnkgame;
import java.util.Arrays;
import java.util.Random;

public class SaddamHussein implements MNKPlayer {
	private Random rand;
	private MNKBoard B;
	private MNKGameState myWin;
	private MNKGameState yourWin;
	private int TIMEOUT;
	private int myM[]; 
	private int myN[];
	private int foeM[];
	private int foeN[];
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
		myM = new int[M];
		myN = new int[N];
		foeM = new int[M];
		foeN = new int[N];
	}
	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		if(MC.length>0) {
			MNKCell cell = MC[MC.length-1];
			foeM[cell.i]++;
			foeN[cell.j]++;
		}
		myM[FC[0].i]++; 
		myN[FC[0].j]++;
		System.out.println("myM "+Arrays.toString(myM));
		System.out.println("myN "+Arrays.toString(myN));
		System.out.println("foeM "+Arrays.toString(foeM));
		System.out.println("foeN "+Arrays.toString(foeN));
		return FC[0];
	} 

	public String playerName() {
		return "SaddamHussein";
	}
}
