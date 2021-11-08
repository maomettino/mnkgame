package mnkgame;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SaddamHussein implements MNKPlayer {
	private Random rand;
	private MNKBoard B;
	private MNKGameState myWin;
	private MNKGameState yourWin;
	private int TIMEOUT;
	private List<Integer> myM = new ArrayList<Integer>();
	private List<Integer> myN = new ArrayList<Integer>();
	private List<Integer> foeM = new ArrayList<Integer>();
	private List<Integer> foeN = new ArrayList<Integer>();
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
		int i;
		for(i=0;i< M ; i++) {
			myM.add(0);
			foeM.add(0);
		}
		for(i=0;i< M ; i++) {
			myN.add(0);
			foeN.add(0);
		}
	}
	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		if(MC.length>0) {
			MNKCell cell = MC[MC.length-1];
			foeM.set(cell.i,foeM.get(cell.i)+1);
			foeN.set(cell.j,foeN.get(cell.j)+1);
		}
		myM.set(FC[0].i,myM.get(FC[0].i)+1); 
		myN.set(FC[0].j,myN.get(FC[0].j)+1);
		System.out.println("myM "+myM);
		System.out.println("myN "+myN);
		System.out.println("foeM "+foeM);
		System.out.println("foeN "+foeN);
		return FC[0];
	} 

	public String playerName() {
		return "SaddamHussein";
	}
}
