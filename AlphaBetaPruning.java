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

public class AlphaBetaPruning {
	private final int m, n, k;
	private final int MAX_DEPTH = 3;
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
	Set<MNKCell> saddamAdjacentCells;
	Set<MNKCell> foeAdjacentCells;
	Set<MNKCell> saddamAdjacentCellsCopy;
	Set<MNKCell> foeAdjacentCellsCopy;
	int[][] direction = new int[][] { { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0, 1 }, { 1, 1 }, { 1, 0 }, { 1, -1 },
			{ 0, -1 } };

	//classic heuristic parameters
	private int saddamJump, saddamJumpCopy, foeJump, foeJumpCopy, saddamMoves, foeMoves;
	private Stack<int[]> saddamHistory, foeHistory;

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

		//classic heuristic paramaters initialization
		saddamJump=0; saddamJumpCopy = 0; foeJump = 0; foeJumpCopy = 0; saddamMoves = 0; foeMoves =0;
		saddamHistory = new Stack<int[]>();
		foeHistory = new Stack<int[]>();
	}

	public void signFoeMove(MNKCell foeCell) {
		b[foeCell.i][foeCell.j] = foe;
		updateAdjacentCells(foeAdjacentCells, foeCell);
	}

	public MNKCell clandestino(MNKCell saddamLastCell, MNKCell foeLastCell, MNKCellState[][] board,
			Stack<int[]> saddamHistory, Stack<int[]> foeHistory) {
		currentDepth = -1;
		// System.out.println("saddam state "+saddamLastCell.state);
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
		// printMatrix(b);
		Node father = new Node(foeLastCell.i, foeLastCell.j, ALPHA, BETA, ALPHA, false, true);
		Node node = inefficiente(father);
		if (node.bestChild != null) {
			System.out.println("cell selected by beta-pruning: " + node.bestChild.i + " " + node.bestChild.j
					+ " with regularity " + node.bestChild.regular);
			return new MNKCell(node.bestChild.i, node.bestChild.j);
		}
		System.out.println("no cell was found by beta-pruning: ");
		return new MNKCell(-1, -1);
	}

	private Node inefficiente(Node father) {
		currentDepth++;
		System.out.println("current father " + father.i + " " + father.j + " at depth " + currentDepth);
		boolean removed = false;
		Set<MNKCell> saveState = new HashSet<MNKCell>();
		saveState.addAll(father.isSaddam ? saddamAdjacentCells : foeAdjacentCells);
		updateAdjacentCells((father.isSaddam ? saddamAdjacentCells : foeAdjacentCells),
				new MNKCell(father.i, father.j));
		if ((father.isSaddam ? foeAdjacentCells : saddamAdjacentCells).remove(new MNKCell(father.i, father.j))) {
			removed = true;
		}

		Node[] children = getNodes(father);
		if (children.length == 0) {
			System.out.println("i: " + father.i + " j: " + father.j + " has no children");
			return new Node(father.i, father.j, father.isSaddam, 0);
		}

		System.out.println("children:");
		for (Node child : children) {
			System.out.println("i: " + child.i + " j: " + child.j);
		}
		int foeValue = 0;
		if (currentDepth == MAX_DEPTH - 1) {
			foeValue = getHeuristicValue(father.i, father.j, father.isSaddam);
		}
		for (Node c : children) {
			Node child;
			// System.out.println("checking c i: " + c.i + " j: " + c.j);
			if (isWinningCell(c.i, c.j, c.isSaddam ? saddam : foe)) {
				child = new Node(c.i, c.j, c.isSaddam, c.isSaddam ? WIN : DEFEAT);
				System.out.println("the child " + child.i + " " + child.j + " is winning for "
						+ (child.isSaddam ? "saddam" : "foe"));
			} else {

				if (currentDepth == MAX_DEPTH - 1) {
					// System.out.println("c right before leaf stuff i: " + c.i + " j: " + c.j);
					child = getHeuristicLeaf(c.i, c.j, c.isSaddam, foeValue);
					System.out.println("the child " + child.i + " " + child.j + " is just a leaf ");
				} else {
					b[c.i][c.j] = c.isSaddam ? saddam : foe;
					child = inefficiente(c);
					b[c.i][c.j] = MNKCellState.FREE;
				}
			}
			if (child.isSaddam) {

				// if (currentDepth == 0) {
				// System.out.println("il valore del figlio aggiornato i: " + child.i+" j: "+
				// child.j+ " value: " +child.value);
				// System.out.println("il valore del padre " + father.value);
				// }

				if (currentDepth == 0 && child.value > father.value) {
					// relative best child, in the end it will be the best
					// System.out.println("best");
					father.bestChild = child;
				}
				// System.out.println("depth " + currentDepth);
				// System.out.println("il valore del figlio pre-confronto max " + child.value);
				// System.out.println("il valore del padre pre-confronto max " + father.value);
				father.value = Math.max(father.value, child.value);
				father.alpha = Math.max(father.alpha, father.value);
				// System.out.println("il valore del padre post-confronto max " + father.value);
			} else {
				// System.out.println("depth " + currentDepth);
				// System.out.println("il valore del figlio pre-confronto min " + child.value);
				// System.out.println("il valore del padre pre-confronto min " + father.value);
				father.value = Math.min(father.value, child.value);
				father.beta = Math.min(father.beta, father.value);
				// System.out.println("il valore del padre post-confronto min " + father.value);
			}
			if (father.beta <= father.alpha) {
				System.out.println("cut off");
				break;
			}
			if ((child.isSaddam && child.value == WIN) || (!child.isSaddam && child.value == DEFEAT)) {
				System.out.println(
						"since this move is winning for the current player we look no further in the current sub-tree");
				break;
			}
		}

		if (father.isSaddam) {
			saddamAdjacentCells = saveState;
		}

		else {
			foeAdjacentCells = saveState;
		}
		if (removed) {
			(father.isSaddam ? foeAdjacentCells : saddamAdjacentCells).add(new MNKCell(father.i, father.j));
		}

		currentDepth--;
		return father;
	}

	private Node[] getNodes(Node father) {

		Set<MNKCell> set = new HashSet<MNKCell>();
		set.addAll(saddamAdjacentCells);
		set.addAll(foeAdjacentCells);
		Node[] children = new Node[set.size()];
		int i = 0;
		if (currentDepth == MAX_DEPTH - 1) {
			int otherPlayerValue = getHeuristicValue(father.i, father.j, father.isSaddam);
			for (MNKCell cell : set) {
				children[i] = getHeuristicLeaf(cell.i, cell.j, !father.isSaddam, otherPlayerValue);
				i++;
			}
		} else {
			for (MNKCell cell : set) {
				children[i] = new Node(cell.i, cell.j, father.alpha, father.beta, father.isSaddam ? ALPHA : BETA,
						!father.isSaddam, true);
				i++;
			}
		}
		return children;
	}

	private void updateAdjacentCells(Set<MNKCell> set, MNKCell myCell, MNKCell foeCell) {
		if (myCell != null) {
			set.addAll(getCellAdjacents(myCell));
			set.remove(myCell);
		}
		if (foeCell != null) {
			set.remove(foeCell);
		}

	}

	private void updateAdjacentCells(Set<MNKCell> set, MNKCell myCell) {
		if (myCell != null) {
			set.addAll(getCellAdjacents(myCell));
			set.remove(myCell);
		}
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
		// System.out.println("Vittoria orizzontale back: " + n);
		for (int k = 1; j + k < this.n && b[i][j + k] == s; k++)
			n++; // forward check
		// System.out.println("Vittoria orizzontale totale: " + n);
		if (n >= this.k)
			return true;

		// Vertical check
		n = 1;
		for (int k = 1; i - k >= 0 && b[i - k][j] == s; k++)
			n++; // backward check
		for (int k = 1; i + k < m && b[i + k][j] == s; k++)
			n++; // forward check
		// System.out.println("Vittoria verticale: " + (n ));
		if (n >= this.k)
			return true;

		// Diagonal check
		n = 1;
		for (int k = 1; i - k >= 0 && j - k >= 0 && b[i - k][j - k] == s; k++)
			n++; // backward check
		for (int k = 1; i + k < m && j + k < this.n && b[i + k][j + k] == s; k++)
			n++; // forward check
		// System.out.println("Vittoria diagonale: " + (n >= this.k));
		if (n >= this.k)
			return true;

		// Anti-diagonal check
		n = 1;
		for (int k = 1; i - k >= 0 && j + k < this.n && b[i - k][j + k] == s; k++)
			n++; // backward check
		for (int k = 1; i + k < m && j - k >= 0 && b[i + k][j - k] == s; k++)
			n++; // backward check
		// System.out.println("Vittoria antidiagonale: " + (n >= this.k));
		if (n >= this.k)
			return true;

		return false;
	}

	private Set<MNKCell> getCellAdjacents(MNKCell cell) {
		Set<MNKCell> set = new HashSet<MNKCell>();
		for (int[] d : direction) {
			int i = cell.i + d[0];
			int j = cell.j + d[1];
			// System.out.println("current i and j "+i+" "+j);
			if (i < n && j < n && i >= 0 && j >= 0 && b[i][j] == MNKCellState.FREE) {
				// System.out.println("adding "+i+" "+j);
				set.add(new MNKCell(i, j));
				// System.out.println("added "+i+" "+j);
			}

		}
		return set;
	}

	private Node getHeuristicLeaf(int i, int j, boolean isSaddam, int foeValue) {
		int myValue = getHeuristicValue(i, j, isSaddam);
		Node node = new Node(i, j, isSaddam, myValue - foeValue);
		// System.out.println("returning node i: " + i + " j: " + j + "");
		return node;
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
		/*
		 * if(lengthH == k)
		 * return (isSaddam?WIN:DEFEAT);
		 * if(lengthV == k)
		 * return (isSaddam?WIN:DEFEAT);
		 * if(lengthD == k)
		 * return (isSaddam?WIN:DEFEAT);
		 * if(lengthAD == k)
		 * return (isSaddam?WIN:DEFEAT);
		 */
		value = ((extraH + lengthH) >= k ? (int) Math.pow(10, lengthH - k + 3) + lengthH : 0) +
				((extraV + lengthV) >= k ? (int) Math.pow(10, lengthV - k + 3) + lengthV : 0) +
				((extraD + lengthD) >= k ? (int) Math.pow(10, lengthD - k + 3) + lengthD : 0) +
				((extraAD + lengthAD) >= k ? (int) Math.pow(10, lengthAD - k + 3) + lengthAD : 0);
		/*
		 * System.out.println("for node i: "+i+" j: "+j+":");
		 * System.out.println("lengthH "+lengthH+" extraH: "+extraH);
		 * System.out.println("lengthV "+lengthV+" extraV: "+extraV);
		 * System.out.println("lengthD "+lengthD+" extraD: "+extraD);
		 * System.out.println("lengthAD "+lengthAD+" extraAD: "+extraAD);
		 * System.out.println("H "+(int)Math.pow(10,lengthH-k+3));
		 * System.out.println("V "+(int)Math.pow(10,lengthV-k+3));
		 * System.out.println("D "+(int)Math.pow(10,lengthD-k+3));
		 * System.out.println("AD "+(int)Math.pow(10,lengthAD-k+3));
		 */
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


	/*
	@SuppressWarnings("unchecked")
	public MNKCell getMove(MNKCell saddamLastCell, MNKCell foeLastCell,
			MNKCellState[][] board,
			Stack<int[]> saddamHistory, Stack<int[]> foeHistory) {
		saddamMoves++;
		foeMoves++;
		currentDepth = -1;
		if (!timeouted) {
			b[saddamLastCell.i][saddamLastCell.j] = saddam;
			b[foeLastCell.i][foeLastCell.j] = foe;
			this.saddamHistory.push(new int[] { saddamLastCell.i, saddamLastCell.j });
			this.foeHistory.push(new int[] { foeLastCell.i, foeLastCell.j });
			System.out.println("before beta pruning");
			// printMatrix(b);

		} else {
			for (int i = 0; i < m; i++)
				for (int j = 0; j < n; j++)
					b[i][j] = board[i][j];
			this.saddamHistory = (Stack<int[]>) saddamHistory.clone();
			this.foeHistory = (Stack<int[]>) foeHistory.clone();
			saddamJump = saddamJumpCopy;
			foeJump = foeJumpCopy;
			timeouted = false;
		}
		saddamAdjacentCells.remove(saddamLastCell);
		saddamAdjacentCells.remove(foeLastCell);
		saddamAdjacentCells.addAll(getCellAdjacents(saddamLastCell));
		foeAdjacentCells.remove(saddamLastCell);
		foeAdjacentCells.remove(foeLastCell);
		foeAdjacentCells.addAll(getCellAdjacents(foeLastCell));
		Node father = new Node(foeLastCell.i, foeLastCell.j, ALPHA, BETA, ALPHA,
				false, true);
		// System.out.println("prima del beta-pruning: ");
		Node node = alphaBetaPruning(father);
		foeJumpCopy = foeJump;
		if (node.bestChild != null) {
			if (!node.bestChild.regular)
				saddamJump++;
			saddamJumpCopy = saddamJump;
			System.out.println("after beta pruning");
			// printMatrix(b);
			System.out.println("cell selected by beta-pruning: " + node.bestChild.i + " "
					+ node.bestChild.j
					+ " with regularity " + node.bestChild.regular);
			return new MNKCell(node.bestChild.i, node.bestChild.j);
		}
		saddamJumpCopy = saddamJump;

		System.out.println("no cell was found by beta-pruning: ");
		return new MNKCell(-1, -1);
	}

	private Node alphaBetaPruning(Node father) {
		currentDepth++;
		// System.out.println("depth " + currentDepth);

		System.out.println(
				"examining node i: " + father.i + " j: " + father.j
						+ (father.isLeaf ? ", which is a leaf" : " at depth " + currentDepth));

		Node[] children = findBestNodes(father);
		// System.out.println("dopo find best nodes ");

		int childrenJump = father.isSaddam ? foeJump : saddamJump;
		for (int i = 0; i < children.length; i++) {
			Node child;
			// System.out.println("for child i: "+children[i].i +" j: "+children[i].j + "
			// saddamJump " + saddamJump + " foeJump "+ foeJump+" saddamHistory size "+
			// saddamHistory.size()+ " foeHistory size "+ foeHistory.size() );
			if (currentDepth != MAX_DEPTH - 1 && !children[i].isLeaf) {
				if (children[i].isSaddam) {
					if (children[i].regular)
						saddamJump = 0;
					else
						saddamJump++;
					saddamHistory.push(new int[] { children[i].i, children[i].j });
				} else {
					if (children[i].regular)
						foeJump = 0;
					else
						foeJump++;
					foeHistory.push(new int[] { children[i].i, children[i].j });
				}
				b[children[i].i][children[i].j] = children[i].isSaddam ? saddam : foe;
				// System.out.println("prima della ricorsione: ");
				child = alphaBetaPruning(children[i]);
				// System.out.println("dopo la ricorsione ");
				b[child.i][child.j] = MNKCellState.FREE;
				if (children[i].isSaddam) {
					saddamJump = childrenJump;
					saddamHistory.pop();
				} else {
					foeJump = childrenJump;
					foeHistory.pop();
				}

			} else
				child = children[i];
			if (children[i].isSaddam) {

				if (currentDepth == 0 && child.value > father.value) {
					father.bestChild = child;
				}

				father.value = Math.max(father.value, child.value);
				father.alpha = Math.max(father.alpha, father.value);
			} else {
				father.value = Math.min(father.value, child.value);
				father.beta = Math.min(father.beta, father.value);
			}
			if (father.beta <= father.alpha) {
				System.out.println("cut off");
				break;
			}
		}
		currentDepth--;
		return father;
	}

	private Set<MNKCell> getAdjacentCells(boolean isSaddam) {
	  Set<MNKCell> set = new HashSet<MNKCell>();
	  int saddamCount = 0;
	  int foeCount = 0;
	  if (isSaddam) {
	  set.addAll(saddamAdjacentCells);
	  for (int i = 1; i <= currentDepth; i++) {
	  if (isSaddam) {
	  int[] c = saddamHistory.get(saddamMoves + saddamCount);
	  // System.out.println("adding cell i: "+c[0]+" "+c[1]);
	  MNKCell cell = new MNKCell(c[0], c[1]);
	  set.addAll(getCellAdjacents(cell));
	  set.remove(cell);
	  saddamCount += 1;
	  } else {
	  // System.out.println("foeHistory size: "+foeHistory.size()+" foeCount
	  // "+foeCount+" foeMoves "+foeMoves);
	  int[] c = foeHistory.get(foeMoves + foeCount);
	  // System.out.println("removing cell i: "+c[0]+" "+c[1]);
	  set.remove(new MNKCell(c[0], c[1]));
	  foeCount += 1;
	  }
	  isSaddam = !isSaddam;
	  }
	  
	  } else {
	  set.addAll(foeAdjacentCells);
	  int count;
	  for (int i = 1; i <= currentDepth; i++) {
	  if (!isSaddam) {
	  int[] c = saddamHistory.get(saddamMoves + saddamCount);
	  // System.out.println("removing cell i: "+c[0]+" "+c[1]);
	  set.remove(new MNKCell(c[0], c[1]));
	  saddamCount += 1;
	  } else {
	  // System.out.println("foeHistory size: "+foeHistory.size()+" foeCount
	  // "+foeCount+" foeMoves "+foeMoves);
	  int[] c = foeHistory.get(foeMoves + foeCount);
	  // System.out.println("adding cell i: "+c[0]+" "+c[1]);
	  MNKCell cell = new MNKCell(c[0], c[1]);
	  set.addAll(getCellAdjacents(cell));
	  set.remove(cell);
	  foeCount += 1;
	  }
	  isSaddam = !isSaddam;
	  }
	  }
	  return set;
	  }

	  private Node[] findBestNodes(Node father) {
	  Node[] children;
	  Moves myMoves = new Moves();
	  int[] myAroundCell = !father.isSaddam ?
	  (saddamHistory.get(saddamHistory.size() - 1 - saddamJump))
	  : (foeHistory.get(foeHistory.size() - 1 - foeJump));
	  int[] foeAroundCell = !father.isSaddam ? (foeHistory.get(foeHistory.size() -
	  1 - foeJump))
	  : (saddamHistory.get(saddamHistory.size() - 1 - saddamJump));
	  if (currentDepth == 0)
	  System.out.println("checking " + (!father.isSaddam ? "saddam" : "foe") +
	  " around with pivot i: "
	  + myAroundCell[0] + " j: " + myAroundCell[1]);
	  checkAround(myAroundCell[0], myAroundCell[1], myMoves, true);
	  if (myMoves.win == null) {
	  Moves foeMoves = new Moves();
	  if (currentDepth == 0)
	  System.out.println("checking " + (!father.isSaddam ? "foe" : "saddam") +
	  " around with pivot i: "
	  + foeAroundCell[0] + " j: " + foeAroundCell[1]);
	  checkAround(foeAroundCell[0], foeAroundCell[1], foeMoves, false);
	  if (foeMoves.win == null) {
	  if (myMoves.twoWin == null) {
	  if (foeMoves.twoWin == null) {
	  children = new Node[myMoves.q.size()];
	  if (currentDepth == MAX_DEPTH - 1) {
	  int foeValue = getHeuristicValue(father.i, father.j,
	  father.isSaddam);
	  for (int i = 0; !myMoves.q.isEmpty(); i++) {
	  int[] m = myMoves.q.remove();
	  Node child;
	  child = getHeuristicLeaf(m[0], m[1], !father.isSaddam, foeValue);
	  children[i] = child;
	  }
	  } else {
	  for (int i = 0; !myMoves.q.isEmpty(); i++) {
	  int[] m = myMoves.q.remove();
	  Node child;
	  child = new Node(m[0], m[1], father.alpha, father.beta, father.isSaddam ?
	  ALPHA : BETA,
	  !father.isSaddam, true);
	  children[i] = child;
	  }
	  }
	  } else {
	  if (currentDepth == MAX_DEPTH - 1) {
	  children = new Node[] { getHeuristicLeaf(foeMoves.twoWin[0],
	  foeMoves.twoWin[1],
	  !father.isSaddam, getHeuristicValue(father.i,
	  father.j, father.isSaddam)) };
	  } else {
	  children = new Node[] {
	  new Node(foeMoves.twoWin[0], foeMoves.twoWin[1], father.alpha, father.beta,
	  father.isSaddam ? ALPHA : BETA, !father.isSaddam,
	  false) };// checkRegularity(myMoves.q, foeMoves.twoWin)
	  }
	  }
	  } else {
	  children = new Node[] { new Node(myMoves.twoWin[0], myMoves.twoWin[1],
	  !father.isSaddam,
	  !father.isSaddam ? WIN : DEFEAT) };
	  }
	  
	  } else {
	  if (currentDepth == MAX_DEPTH - 1) {
	  children = new Node[] {
	  getHeuristicLeaf(foeMoves.win[0], foeMoves.win[1], !father.isSaddam,
	  getHeuristicValue(
	  father.i, father.j, father.isSaddam)) };
	  } else {
	  children = new Node[] { new Node(foeMoves.win[0], foeMoves.win[1],
	  father.alpha, father.beta,
	  father.isSaddam ? ALPHA : BETA, !father.isSaddam,
	  false) }; // checkRegularity(myMoves.q, foeMoves.win)
	  }
	  
	  }
	  
	  } else
	  children = new Node[] {
	  new Node(myMoves.win[0], myMoves.win[1], !father.isSaddam, !father.isSaddam ?
	  WIN : DEFEAT) };
	  if (children.length == 0) {
	  System.out.println("the node mahmoud i: " + father.i + " j: " + father.j
	  + " has no children, gotta check the adjacents");
	  
	  Set<MNKCell> set = getAdjacentCells(!father.isSaddam);
	  children = new Node[set.size()];
	  int i = 0;
	  if (currentDepth == MAX_DEPTH - 1) {
	  int otherPlayerValue = getHeuristicValue(father.i, father.j,
	  father.isSaddam);
	  for (MNKCell cell : set) {
	  children[i] = getHeuristicLeaf(cell.i, cell.j, !father.isSaddam,
	  otherPlayerValue);
	  i++;
	  }
	  ;
	  } else {
	  for (MNKCell cell : set) {
	  children[i] = new Node(cell.i, cell.j, father.alpha, father.beta,
	  father.isSaddam ? ALPHA : BETA,
	  !father.isSaddam, true);
	  i++;
	  }
	  ;
	  }
	  if (children.length == 0) {
	  Set<MNKCell> set = getAdjacentCells(father.isSaddam);
	  int i=0;
	  if(currentDepth == MAX_DEPTH-1) {
	  int otherPlayerValue= getHeuristicValue(father.i, father.j, father.isSaddam);
	  for(MNKCell cell : set) {
	  children[i] = getHeuristicLeaf(cell.i, cell.j, !father.isSaddam,
	  otherPlayerValue);
	  i++;
	  };
	  }
	  else {
	  for( MNKCell cell : set ) {
	  children[i] = new Node(cell.i, cell.j, father.alpha, father.beta,
	  father.isSaddam ? ALPHA : BETA, !father.isSaddam, true);
	  i++;
	  };
	  }
	  
	  }

	  return children; }
	}
	  
	  private void checkAround(int i, int j, Moves moves, boolean full) {
	  // System.out.println("checking around of i: " + i + " j: " + j);
	  PriorityQueue<int[]> q;
	  q = new PriorityQueue<int[]>(8, new Comparatore());
	  MNKCellState player = b[i][j];
	  int length = 1, backExtra = 0, forwardExtra = 0, backCount, forwardCount;
	  boolean freeBack[] = { false, false }, freeForward[] = { false, false },
	  freeExtraForward = false,
	  freeExtraBack = false;
	  // System.out.println("Starting check around for depth " + currentDepth);
	  
	  // Horizontal
	  for (backCount = 1; j - backCount >= 0 && b[i][j - backCount] == player;
	  backCount++)
	  length++;
	  for (forwardCount = 1; j + forwardCount < n && b[i][j + forwardCount] ==
	  player; forwardCount++)
	  length++;
	  if (j - backCount >= 0 && b[i][j - backCount] == MNKCellState.FREE) {
	  if (length == k - 1) {
	  // System.out.println("winning back H move " + i + " " + (j - backCount));
	  int move[] = { i, j - backCount };
	  moves.win = move;
	  return;
	  }
	  int rest = k - length - 1;
	  for (int c = 1; j - backCount - c >= 0 && b[i][j - backCount - c] == player
	  && c <= rest; c++)
	  backExtra++;
	  if (length + backExtra == k - 1) {
	  // System.out.println("winning back H move " + i + " " + (j - backCount));
	  int move[] = { i, j - backCount };
	  moves.win = move;
	  return;
	  }
	  freeBack[0] = true;
	  if (j - backCount - 1 >= 0 && b[i][j - backCount - 1] == MNKCellState.FREE)
	  freeBack[1] = true;
	  if (j - backCount - backExtra - 1 >= 0 && b[i][j - backCount - backExtra - 1]
	  == MNKCellState.FREE)
	  freeExtraBack = true;
	  if (full) {
	  int[] backMove = { i, j - backCount, length + backExtra };
	  q.add(backMove);
	  }
	  }
	  if (j + forwardCount < n && b[i][j + forwardCount] == MNKCellState.FREE) {
	  // System.out.println("checking free forward H cell " + i + " " + (j +
	  // forwardCount));
	  if (length == k - 1) {
	  int move[] = { i, j + forwardCount };
	  moves.win = move;
	  return;
	  }
	  int rest = k - length - 1;
	  
	  for (int c = 1; j + forwardCount + c < n && b[i][j + forwardCount + c] ==
	  player
	  && c <= rest; c++)
	  forwardExtra++;
	  if (length + forwardExtra == k - 1) {
	  int move[] = { i, j + forwardCount };
	  moves.win = move;
	  return;
	  }
	  freeForward[0] = true;
	  if (j + forwardCount + 1 < n && b[i][j + forwardCount + 1] ==
	  MNKCellState.FREE)
	  freeForward[1] = true;
	  if (j + forwardCount + forwardExtra + 1 < n
	  && b[i][j + forwardCount + forwardExtra + 1] == MNKCellState.FREE)
	  freeExtraForward = true;
	  if (full) {
	  int[] forwardMove = { i, j + forwardCount, length + forwardExtra };
	  q.add(forwardMove);
	  
	  }
	  }
	  if (length == k - 2) {
	  if (freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
	  j = freeBack[1] ? j - backCount : j + forwardCount;
	  int move[] = { i, j };
	  // System.out.println("found 2 win H cell : " + i + " " + j);
	  moves.twoWin = move;
	  }
	  } else {
	  if (freeBack[0] && length + backExtra == k - 2 && freeForward[0] &&
	  freeExtraBack) {
	  int move[] = { i, j - backCount };
	  moves.twoWin = move;
	  return;
	  }
	  if (freeForward[0] && length + forwardExtra == k - 2 && freeBack[0] &&
	  freeExtraForward) {
	  // System.out.println("found 2 win forward H move " + i + " " + (j +
	  // forwardCount));
	  int move[] = { i, j + forwardCount };
	  moves.twoWin = move;
	  return;
	  }
	  }
	  length = 1;
	  backExtra = 0;
	  forwardExtra = 0;
	  freeBack[0] = false;
	  freeBack[1] = false;
	  freeForward[0] = false;
	  freeForward[1] = false;
	  freeExtraForward = false;
	  freeExtraBack = false;
	  
	  // Vertical
	  for (backCount = 1; i - backCount >= 0 && b[i - backCount][j] == player;
	  backCount++)
	  length++;
	  for (forwardCount = 1; i + forwardCount < n && b[i + forwardCount][j] ==
	  player; forwardCount++)
	  length++;
	  if (i - backCount >= 0 && b[i - backCount][j] == MNKCellState.FREE) {
	  if (length == k - 1) {
	  int move[] = { i - backCount, j };
	  moves.win = move;
	  return;
	  }
	  int rest = k - length - 1;
	  for (int c = 1; i - backCount - c >= 0 && b[i - backCount - c][j] == player
	  && c <= rest; c++)
	  backExtra++;
	  if (length + backExtra == k - 1) {
	  int move[] = { i - backCount, j };
	  moves.win = move;
	  return;
	  }
	  freeBack[0] = true;
	  if (i - backCount - 1 >= 0 && b[i - backCount - 1][j] == MNKCellState.FREE)
	  freeBack[1] = true;
	  if (i - backCount - backExtra - 1 >= 0 && b[i - backCount - backExtra - 1][j]
	  == MNKCellState.FREE)
	  freeExtraBack = true;
	  if (full) {
	  int[] backMove = { i - backCount, j, length + backExtra };
	  q.add(backMove);
	  }
	  }
	  if (i + forwardCount < n && b[i + forwardCount][j] == MNKCellState.FREE) {
	  if (length == k - 1) {
	  int move[] = { i + forwardCount, j };
	  moves.win = move;
	  return;
	  }
	  int rest = k - length - 1;
	  for (int c = 1; i + forwardCount + c < n && b[i + forwardCount + c][j] ==
	  player
	  && c <= rest; c++)
	  forwardExtra++;
	  if (length + forwardExtra == k - 1) {
	  int move[] = { i + forwardCount, j };
	  moves.win = move;
	  return;
	  }
	  freeForward[0] = true;
	  if (i + forwardCount + 1 < n && b[i + forwardCount + 1][j] ==
	  MNKCellState.FREE)
	  freeForward[1] = true;
	  if (i + forwardCount + forwardExtra + 1 < n
	  && b[i + forwardCount + forwardExtra + 1][j] == MNKCellState.FREE)
	  freeExtraForward = true;
	  if (full) {
	  int[] forwardMove = { i + forwardCount, j, length + forwardExtra };
	  q.add(forwardMove);
	  }
	  }
	  if (length == k - 2) {
	  if (freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
	  i = freeBack[1] ? i - backCount : i + forwardCount;
	  int move[] = { i, j };
	  // System.out.println("found 2 winning V cell " + i + " " + j);
	  moves.twoWin = move;
	  }
	  } else {
	  if (freeBack[0] && length + backExtra == k - 2 && freeForward[0] &&
	  freeExtraBack) {
	  int move[] = { i - backCount, j };
	  moves.twoWin = move;
	  return;
	  }
	  if (freeForward[0] && length + forwardExtra == k - 2 && freeBack[0] &&
	  freeExtraForward) {
	  int move[] = { i + forwardCount, j };
	  moves.twoWin = move;
	  return;
	  }
	  }
	  length = 1;
	  backExtra = 0;
	  forwardExtra = 0;
	  freeBack[0] = false;
	  freeBack[1] = false;
	  freeForward[0] = false;
	  freeForward[1] = false;
	  freeExtraForward = false;
	  freeExtraBack = false;
	  
	  // Diagonal
	  for (backCount = 1; j - backCount >= 0 && i - backCount >= 0
	  && b[i - backCount][j - backCount] == player; backCount++)
	  length++;
	  for (forwardCount = 1; j + forwardCount < n && i + forwardCount < n
	  && b[i + forwardCount][j + forwardCount] == player; forwardCount++)
	  length++;
	  if (j - backCount >= 0 && i - backCount >= 0
	  && b[i - backCount][j - backCount] == MNKCellState.FREE) {
	  if (length == k - 1) {
	  int move[] = { i - backCount, j - backCount };
	  moves.win = move;
	  return;
	  }
	  int rest = k - length - 1;
	  for (int c = 1; j - backCount - c >= 0 && i - backCount - c >= 0
	  && b[i - backCount - c][j - backCount - c] == player && c <= rest; c++)
	  backExtra++;
	  if (length + backExtra == k - 1) {
	  int move[] = { i - backCount, j - backCount };
	  moves.win = move;
	  return;
	  }
	  freeBack[0] = true;
	  if (j - backCount - 1 >= 0 && i - backCount - 1 >= 0
	  && b[i - backCount - 1][j - backCount - 1] == MNKCellState.FREE)
	  freeBack[1] = true;
	  if (j - backCount - backExtra - 1 >= 0 && i - backCount - backExtra - 1 >= 0
	  && b[i - backCount - backExtra - 1][j - backCount - backExtra - 1] ==
	  MNKCellState.FREE)
	  freeExtraBack = true;
	  if (full) {
	  int[] backMove = { i - backCount, j - backCount, length + backExtra };
	  q.add(backMove);
	  }
	  }
	  if (j + forwardCount < n && i + forwardCount < n
	  && b[i + forwardCount][j + forwardCount] == MNKCellState.FREE) {
	  if (length == k - 1) {
	  int move[] = { i + forwardCount, j + forwardCount };
	  moves.win = move;
	  return;
	  }
	  int rest = k - length - 1;
	  for (int c = 1; j + forwardCount + c < n && i + forwardCount + c < n
	  && b[i + forwardCount + c][j + forwardCount + c] == player && c <= rest; c++)
	  forwardExtra++;
	  if (length + forwardExtra == k - 1) {
	  int move[] = { i + forwardCount, j + forwardCount };
	  moves.win = move;
	  return;
	  }
	  freeForward[0] = true;
	  if (j + forwardCount + 1 < n && i + forwardCount + 1 < n
	  && b[i + forwardCount + 1][j + forwardCount + 1] == MNKCellState.FREE)
	  freeForward[1] = true;
	  if (j + forwardCount + forwardExtra + 1 < n && i + forwardCount +
	  forwardExtra + 1 < n
	  && b[i + forwardCount + forwardExtra + 1][j + forwardCount + forwardExtra +
	  1] == MNKCellState.FREE)
	  freeExtraForward = true;
	  if (full) {
	  int[] forwardMove = { i + forwardCount, j + forwardCount, length +
	  forwardExtra };
	  q.add(forwardMove);
	  }
	  }
	  if (length == k - 2) {
	  if (freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
	  i = freeBack[1] ? i - backCount : i + forwardCount;
	  j = freeBack[1] ? j - backCount : j + forwardCount;
	  int move[] = { i, j };
	  // System.out.println("found 2 win D cell " + i + " " + j);
	  moves.twoWin = move;
	  }
	  } else {
	  if (freeBack[0] && length + backExtra == k - 2 && freeForward[0] &&
	  freeExtraBack) {
	  int move[] = { i - backCount, j - backCount };
	  moves.twoWin = move;
	  return;
	  }
	  if (freeForward[0] && length + forwardExtra == k - 2 && freeBack[0] &&
	  freeExtraForward) {
	  int move[] = { i + forwardCount, j + forwardCount };
	  moves.twoWin = move;
	  return;
	  }
	  }
	  length = 1;
	  backExtra = 0;
	  forwardExtra = 0;
	  freeBack[0] = false;
	  freeBack[1] = false;
	  freeForward[0] = false;
	  freeForward[1] = false;
	  freeExtraForward = false;
	  freeExtraBack = false;
	  
	  // Antidiagonal
	  for (backCount = 1; j + backCount < n && i - backCount >= 0
	  && b[i - backCount][j + backCount] == player; backCount++)
	  length++;
	  for (forwardCount = 1; j - forwardCount >= 0 && i + forwardCount < n
	  && b[i + forwardCount][j - forwardCount] == player; forwardCount++)
	  length++;
	  
	  if (j + backCount < n && i - backCount >= 0 && b[i - backCount][j +
	  backCount] == MNKCellState.FREE) {
	  if (length == k - 1) {
	  int move[] = { i - backCount, j + backCount };
	  moves.win = move;
	  return;
	  }
	  int rest = k - length - 1;
	  for (int c = 1; j + backCount + c < n && i - backCount - c >= 0
	  && b[i - backCount - c][j + backCount + c] == player && c <= rest; c++)
	  backExtra++;
	  if (length + backExtra == k - 1) {
	  int move[] = { i - backCount, j + backCount };
	  moves.win = move;
	  return;
	  }
	  freeBack[0] = true;
	  if (j + backCount + 1 >= 0 && i - backCount - 1 >= 0
	  && b[i - backCount - 1][j + backCount - 1] == MNKCellState.FREE)
	  freeBack[1] = true;
	  if (i - backCount - backExtra - 1 >= 0 && j + backCount + backExtra + 1 < n
	  && b[i - backCount - backExtra - 1][j + backCount + backExtra + 1] ==
	  MNKCellState.FREE)
	  freeExtraBack = true;
	  if (full) {
	  int[] backMove = { i - backCount, j + backCount, length + backExtra };
	  q.add(backMove);
	  }
	  }
	  
	  if (j - forwardCount >= 0 && i + forwardCount < n
	  && b[i + forwardCount][j - forwardCount] == MNKCellState.FREE) {
	  if (length == k - 1) {

	  int move[] = { i + forwardCount, j - forwardCount };
	  moves.win = move;
	  return;
	  }
	  
	  int rest = k - length - 1;
	  for (int c = 1; j - forwardCount - c >= 0 && i + forwardCount + c < n
	  && b[i + forwardCount + c][j - forwardCount - c] == player && c <= rest; c++)
	  forwardExtra++;
	  
	  if (length + forwardExtra == k - 1) {
	  int move[] = { i + forwardCount, j - forwardCount };
	  moves.win = move;
	  return;
	  }
	  freeForward[0] = true;
	  if (j - forwardCount - 1 >= 0 && i + forwardCount + 1 < n
	  && b[i + forwardCount + 1][j - forwardCount - 1] == MNKCellState.FREE)
	  freeForward[1] = true;
	  if (i + forwardCount + forwardExtra + 1 < n && j - forwardCount -
	  forwardExtra - 1 >= 0
	  && b[i + forwardCount + forwardExtra + 1][j - forwardCount - forwardExtra -
	  1] == MNKCellState.FREE)
	  freeExtraForward = true;
	  if (full) {
	  int[] forwardMove = { i + forwardCount, j - forwardCount, length +
	  forwardExtra };
	  q.add(forwardMove);
	  }
	  }
	  
	  if (length == k - 2) {
	  if (freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
	  i = freeBack[1] ? i - backCount : i + forwardCount;
	  j = freeBack[1] ? j + backCount : j - forwardCount;
	  int move[] = { i, j };
	  // System.out.println("found 2 winning AD cell " + i + " " + j);
	  moves.twoWin = move;
	  }
	  } else {
	  if (freeBack[0] && length + backExtra == k - 2 && freeForward[0] &&
	  freeExtraBack) {
	  int move[] = { i - backCount, j + backCount };
	  moves.twoWin = move;
	  return;
	  }
	  if (freeForward[0] && length + forwardExtra == k - 2 && freeBack[0] &&
	  freeExtraForward) {
	  int move[] = { i + forwardCount, j - forwardCount };
	  moves.twoWin = move;
	  return;
	  }
	  }
	  if (full)
	  moves.q = q;
	  }
	  
	  private boolean checkRegularity(PriorityQueue<int[]> q, int[] cell) {
	  if (q == null) {
	  System.out.println("la queue e' vuota e la depth " + currentDepth);
	  // printMatrix(b);
	  return false;
	  } else {
	  for (int[] m : q) {
	  if (m[0] == cell[0] && m[1] == cell[1])
	  return true;
	  }
	  }
	  
	  return false;
	  }
	  
	  public void test() {
	  currentDepth = 3;
	  // last moves
	  saddamHistory.push(new int[] { 0, 1 });
	  b[0][1] = saddam;
	  foeHistory.push(new int[] { 0, 2 });
	  b[0][2] = foe;
	  saddamMoves++;
	  foeMoves++;
	  
	  // tree moves
	  saddamHistory.push(new int[] { 0, 0 });
	  b[0][0] = saddam;
	  foeHistory.push(new int[] { 1, 1 });
	  b[1][1] = foe;
	  saddamHistory.push(new int[] { 2, 2 });
	  b[2][2] = saddam;
	  
	  foeHistory.push(new int[] { 3, 3 });
	  b[3][3] = foe;
	  
	  printMatrix(getAdjacentCells(false));
	  
	  }

*/

	}
