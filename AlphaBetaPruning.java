package mnkgame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.Node;

/* TODO list:
	-fix the only known bug of this algorithm, which is:
		-the algorithm always finds a winning move for the foe if it exists but it
		doesn't always choose it, even when it must do so(there are no winning moves for the current player
		so we must make sure that the foe does not win in the next turn),
		sometimes this happens with the winning move for the current player as well
	-consider Integer as the element of the set instead of MNKCell, it may be lighter
	-consider Board class instead of b matrix, it may be more comfortable and perhaps more efficient
	
	-the research space of the algorithm may be reduced by generating the foe-adjacent moves only
	if necessary, i.e. no (immediate)winning move was found, can we reduce any further?
	-check around finds a winning move in a relatively efficient way, but if there is no
	 winning move then it's a useless effort, can we use it somehow?
	-the generated moves should be ordered to increase the cut-off probability, we may give
	 priority to the moves adjacent to the player and the foe last move or something like that

*/
public class AlphaBetaPruning {
	private final int m, n, k;
	private final int MAX_DEPTH = 4;
	private final int WIN = 5000;
	private final int DEFEAT = -5000;
	private final int ALPHA = -10000;
	private final int BETA = 10000;
	private final int TIMEOUT;
	private int currentDepth;
	private MNKCellState saddam;
	private MNKCellState foe;
	private MNKCellState[][] b;
	private boolean timeouted;
	private Set<MNKCell> saddamAdjacentCells;
	private Set<MNKCell> foeAdjacentCells;
	private Set<MNKCell> saddamAdjacentCellsCopy;
	private Set<MNKCell> foeAdjacentCellsCopy;
	private int[][] direction = new int[][] { { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0, 1 }, { 1, 1 }, { 1, 0 }, { 1, -1 },
			{ 0, -1 } };
	private int[] bestNode;

	public AlphaBetaPruning(int m, int n, int k, boolean first, int timeout_in_secs) {
		currentDepth = -1;
		TIMEOUT = timeout_in_secs;
		this.m = m;
		this.n = n;
		this.k = k;
		saddam = first ? MNKCellState.P1 : MNKCellState.P2;
		foe = first ? MNKCellState.P2 : MNKCellState.P1;
		b = new MNKCellState[m][n];
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				b[i][j] = MNKCellState.FREE;
		timeouted = false;
		saddamAdjacentCells = new HashSet<MNKCell>();
		foeAdjacentCells = new HashSet<MNKCell>();
		saddamAdjacentCellsCopy = new HashSet<MNKCell>();
		foeAdjacentCellsCopy = new HashSet<MNKCell>();
	}

	public void signFoeMove(MNKCell foeCell) {
		b[foeCell.i][foeCell.j] = foe;
		updateAdjacentCells(foeAdjacentCells, foeCell);
	}

	public MNKCell getMove(MNKCell saddamLastCell, MNKCell foeLastCell, MNKCellState[][] board) {
		currentDepth = -1;
		saddamLastCell = new MNKCell(saddamLastCell.i, saddamLastCell.j);
		foeLastCell = new MNKCell(foeLastCell.i, foeLastCell.j);
		updateAdjacentCells(saddamAdjacentCellsCopy, saddamLastCell, foeLastCell);
		updateAdjacentCells(foeAdjacentCellsCopy, foeLastCell, saddamLastCell);
		if (!timeouted) {
			b[saddamLastCell.i][saddamLastCell.j] = saddam;
			b[foeLastCell.i][foeLastCell.j] = foe;
			updateAdjacentCells(saddamAdjacentCells, saddamLastCell, foeLastCell);
			updateAdjacentCells(foeAdjacentCells, foeLastCell, saddamLastCell);
		} else {
			for (int i = 0; i < m; i++)
				for (int j = 0; j < n; j++)
					b[i][j] = board[i][j];
			timeouted = false;
			saddamAdjacentCells.clear();
			saddamAdjacentCells.addAll(saddamAdjacentCellsCopy);
			foeAdjacentCells.clear();
			foeAdjacentCells.addAll(foeAdjacentCellsCopy);
		}
		bestNode = null;
		int[] father = new int [] {foeLastCell.i, foeLastCell.j, ALPHA};
		alphaBetaPruning(father,ALPHA,BETA);
		if (bestNode != null) {
			System.out.println("cell selected by beta-pruning: " + bestNode[0] + " " + bestNode[1]+" with value "+bestNode[2]);
			return new MNKCell(bestNode[0], bestNode[1]);
		}
		System.out.println("no cell was found by beta-pruning: ");
		return new MNKCell(-1, -1);
	}

	private int[] alphaBetaPruning(int[] father, int alpha, int beta) {
		currentDepth++;
		boolean almostMaxDepth = (currentDepth==MAX_DEPTH-1);
		boolean isSaddam = (currentDepth%2==0)?false:true;
		if(currentDepth==0)
			System.out.println("current father " + father[0] + " " + father[1] + " at depth " + currentDepth);
		boolean removed = false;
		Set<MNKCell> saveState = new HashSet<MNKCell>();
		saveState.addAll(isSaddam ? saddamAdjacentCells : foeAdjacentCells);
		updateAdjacentCells((isSaddam ? saddamAdjacentCells : foeAdjacentCells),
				new MNKCell(father[0], father[1]));
		if ((isSaddam ? foeAdjacentCells : saddamAdjacentCells).remove(new MNKCell(father[0], father[1]))) {
			removed = true;
		}

		int[][] children = getNodes(father, isSaddam);
		if (children.length == 0) {
			System.out.println("i: " + father[0] + " j: " + father[1] + " has no children");
			return new int[] {father[0], father[1], 0};
		}
		if(currentDepth==0) {
			System.out.println("children:");
			for (int[] child : children) {
				System.out.println("i: " + child[0] + " j: " + child[1]);
			}
		}
		
		int foeValue = 0;
		if (almostMaxDepth) {
			foeValue = getHeuristicValue(father[0], father[1], isSaddam);
		}
		for (int[] child : children) {
			if (isWinningCell(child[0], child[1], !isSaddam ? saddam : foe)) {
				child = new int[] { child[0], child[1], !isSaddam ? WIN : DEFEAT };
				if(currentDepth==1)
				System.out.println("the child " + child[0] + " " + child[1] + " is winning for "
						+ (!isSaddam ? "saddam" : "foe")+" with father "+father[0]+" "+father[1]);
			} else {

				if (almostMaxDepth) {
					child = new int[] {child[0], child[1],(getHeuristicValue(child[0], child[1], !isSaddam)-foeValue)};
				} else {
					b[child[0]][child[1]] = !isSaddam ? saddam : foe;
					child = alphaBetaPruning(child,alpha,beta);
					b[child[0]][child[1]] = MNKCellState.FREE;
				}
			}
			if (!isSaddam) {
				if (currentDepth == 0 && child[2] > father[2]) {
					bestNode = child;
				}
				father[2] = Math.max(father[2], child[2]);
				alpha = Math.max(alpha, father[2]);
			} else {
				father[2] = Math.min(father[2], child[2]);
				beta = Math.min(beta, father[2]);
			}
			//System.out.println("value "+father[2] );
			if (beta <= alpha) { 
				
				System.out.println("cut off");
				break;
			}
		}

		if (isSaddam) {
			saddamAdjacentCells = saveState;
		}

		else {
			foeAdjacentCells = saveState;
		}
		if (removed) {
			(isSaddam ? foeAdjacentCells : saddamAdjacentCells).add(new MNKCell(father[0], father[1]));
		}
		if(currentDepth==1)
			System.out.println("cell "+father[0]+" "+father[1]+" has updated value "+father[2]);
		currentDepth--;
		return father;
	}

	private int[][] getNodes(int[] father, boolean isSaddam) {
		Set<MNKCell> set = new HashSet<MNKCell>();
		set.addAll(saddamAdjacentCells);
		set.addAll(foeAdjacentCells);
		int[][] children = new int[set.size()][3];
		int i = 0;
		if ((currentDepth==MAX_DEPTH-1)) {
			int otherPlayerValue = getHeuristicValue(father[0], father[1], isSaddam);
			for (MNKCell cell : set) {
				children[i] = new int[] {cell.i, cell.j,(getHeuristicValue(cell.i, cell.j, !isSaddam)-otherPlayerValue)};
				i++;
			}
		} else {
			for (MNKCell cell : set) {
				children[i] = new int[] {cell.i, cell.j, isSaddam ? ALPHA : BETA };
				i++;
			}
		}
		return children;
	}

	private void updateAdjacentCells(Set<MNKCell> set, MNKCell myCell, MNKCell foeCell) {
			set.addAll(getCellAdjacents(myCell));
			set.remove(myCell);
			set.remove(foeCell);
	}

	private void updateAdjacentCells(Set<MNKCell> set, MNKCell myCell) {
			set.addAll(getCellAdjacents(myCell));
			set.remove(myCell);
	}

	private boolean isWinningCell(int i, int j, MNKCellState s) {
		int n;
		// Horizontal check
		n = 1;
		// backward check
		for (int k = 1; j - k >= 0 && b[i][j - k] == s; k++) {
			// System.out.println("checking cell i: " +i+" j: " +(j-k));
			n++;
		}
		for (int k = 1; j + k < this.n && b[i][j + k] == s; k++)
			n++; 
		// forward check
		if (n >= this.k)
			return true;

		// Vertical check
		n = 1;
		for (int k = 1; i - k >= 0 && b[i - k][j] == s; k++)
			n++; // backward check
		for (int k = 1; i + k < m && b[i + k][j] == s; k++)
			n++; // forward check
		if (n >= this.k)
			return true;

		// Diagonal check
		n = 1;
		for (int k = 1; i - k >= 0 && j - k >= 0 && b[i - k][j - k] == s; k++)
			n++; // backward check
		for (int k = 1; i + k < m && j + k < this.n && b[i + k][j + k] == s; k++)
			n++; // forward check
		if (n >= this.k)
			return true;

		// Anti-diagonal check
		n = 1;
		for (int k = 1; i - k >= 0 && j + k < this.n && b[i - k][j + k] == s; k++)
			n++; // backward check
		for (int k = 1; i + k < m && j - k >= 0 && b[i + k][j - k] == s; k++)
			n++; // backward check
		if (n >= this.k)
			return true;

		return false;
	}

	private Set<MNKCell> getCellAdjacents(MNKCell cell) {
		Set<MNKCell> set = new HashSet<MNKCell>();
		for (int[] d : direction) {
			int i = cell.i + d[0];
			int j = cell.j + d[1];
			if (i < n && j < n && i >= 0 && j >= 0 && b[i][j] == MNKCellState.FREE) {
				set.add(new MNKCell(i, j));
			}
		}
		return set;
	}

	private int getHeuristicValue(int i, int j, boolean isSaddam) {
		MNKCellState player = isSaddam ? saddam : foe;
		int lengthH = 1, lengthV = 1, lengthD = 1, lengthAD = 1;
		int extraH = 0, extraV = 0, extraD = 0, extraAD = 0;
		int backCount, forwardCount;
		// Horizontal
		for (backCount = 1; j - backCount >= 0 && b[i][j - backCount] == player; backCount++)
			lengthH++;
		for (forwardCount = 1; j + forwardCount < n && b[i][j + forwardCount] == player; forwardCount++)
			lengthH++;
		for (; j - backCount >= 0 && b[i][j - backCount] == MNKCellState.FREE && lengthH + extraH < k; backCount++)
			extraH++;
		for (; j + forwardCount < n && b[i][j + forwardCount] == MNKCellState.FREE
				&& lengthH + extraH < k; forwardCount++)
			extraH++;

		// Vertical
		for (backCount = 1; i - backCount >= 0 && b[i - backCount][j] == player; backCount++)
			lengthV++;

		for (forwardCount = 1; i + forwardCount < n && b[i + forwardCount][j] == player; forwardCount++)
			lengthV++;

		for (; i - backCount >= 0 && b[i - backCount][j] == MNKCellState.FREE && lengthV + extraV < k; backCount++)
			extraV++;
		for (; i + forwardCount < n && b[i + forwardCount][j] == MNKCellState.FREE
				&& lengthV + extraV < k; forwardCount++)
			extraV++;

		// Diagonal
		for (backCount = 1; j - backCount >= 0 && i - backCount >= 0
				&& b[i - backCount][j - backCount] == player; backCount++)
			lengthD++;

		for (forwardCount = 1; j + forwardCount < n && i + forwardCount < n
				&& b[i + forwardCount][j + forwardCount] == player; forwardCount++)
			lengthD++;

		for (; j - backCount >= 0 && i - backCount >= 0
				&& b[i - backCount][j - backCount] == MNKCellState.FREE && lengthD + extraD < k; backCount++)
			extraD++;
		for (; j + forwardCount < n && i + forwardCount < n
				&& b[i + forwardCount][j + forwardCount] == MNKCellState.FREE
				&& lengthD + extraD < k; forwardCount++)
			extraD++;

		// Antidiagonal
		for (backCount = 1; j + backCount < n && i - backCount >= 0
				&& b[i - backCount][j + backCount] == player; backCount++)
			lengthAD++;

		for (forwardCount = 1; j - forwardCount >= 0 && i + forwardCount < n
				&& b[i + forwardCount][j - forwardCount] == player; forwardCount++)
			lengthAD++;
		for (; j + backCount < n && i - backCount >= 0
				&& b[i - backCount][j + backCount] == MNKCellState.FREE && lengthAD + extraAD < k; backCount++)
			extraAD++;
		for (; j - forwardCount >= 0 && i + forwardCount < n
				&& b[i + forwardCount][j - forwardCount] == MNKCellState.FREE
				&& lengthAD + extraAD < k; forwardCount++)
			extraAD++;

		int value;
		value = ((extraH + lengthH) >= k ? (int) Math.pow(10, lengthH - k + 3) + lengthH : 0) +
				((extraV + lengthV) >= k ? (int) Math.pow(10, lengthV - k + 3) + lengthV : 0) +
				((extraD + lengthD) >= k ? (int) Math.pow(10, lengthD - k + 3) + lengthD : 0) +
				((extraAD + lengthAD) >= k ? (int) Math.pow(10, lengthAD - k + 3) + lengthAD : 0);
		return value;
	}

	private void printMatrix(Set<MNKCell> matrix) {
		matrix.forEach((row) -> {
			System.out.print("[ " + row.i);
			System.out.println(row.j + " ]");
		});
	}

	private void printMatrix(MNKCellState[][] matrix) {
		Arrays.stream(matrix).forEach((row) -> {
			System.out.print("[");
			Arrays.stream(row).forEach((el) -> System.out.print(" " + el + " "));
			System.out.println("]");
		});
	}
	
}
