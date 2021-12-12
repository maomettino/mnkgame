package mnkgame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.Moves;
import mnkgame.Node;

public class AlphaBetaPruning {
	private final int m, n, k;
	private final int MAX_DEPTH = 5;
	private final int WIN = 1;
	private final int DEFEAT = -1;
	private final int ALPHA = -1000;
	private final int BETA = 1000;
	private final int TIMEOUT;
	private int currentDepth;
	private int saddamMoves, foeMoves;
	private MNKCellState saddam;
	private MNKCellState foe;
	private MNKCellState[][] b;
	private MNKCellState[][] globalBoard;
	private int[] saddamlastRegularMove;
	private int[] foelastRegularMove;
//FOR SOME REASON THE K-2 MOVES SOMETIMES ARE OUT OF BOUNDS
	public AlphaBetaPruning(int m, int n, int k, boolean first, int timeout_in_secs) {
		currentDepth = -1;
		saddamMoves = 0;
		foeMoves = 0;
		TIMEOUT = timeout_in_secs;
		this.m = m;
		this.n = n;
		this.k = k;
		saddam = first ? MNKCellState.P1 : MNKCellState.P2;
		foe = first ? MNKCellState.P2 : MNKCellState.P1;
		b = new MNKCellState[m][n];
		globalBoard = new MNKCellState[m][n];
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				globalBoard[i][j] = MNKCellState.FREE;
	}

	public void signFoeMove(MNKCell foeCell) {
		foeMoves++;
		globalBoard[foeCell.i][foeCell.j] = foe;
	}

	public MNKCell getMove(MNKCell saddamLastCell, MNKCell foeLastCell) {
		currentDepth = -1;
		saddamMoves++;
		foeMoves++;
		globalBoard[saddamLastCell.i][saddamLastCell.j] = saddam;
		globalBoard[foeLastCell.i][foeLastCell.j] = foe;
		if( saddamlastRegularMove == null) {
			saddamlastRegularMove = new int[2];
			saddamlastRegularMove[0] = saddamLastCell.i;
			saddamlastRegularMove[1] = saddamLastCell.j;
		}
		if(foelastRegularMove == null) {
			foelastRegularMove = new int[2];
			foelastRegularMove[0] = foeLastCell.i;
			foelastRegularMove[1] = foeLastCell.j;
		}
		System.out.println("swag "+saddamlastRegularMove[0]);
		// make sure that local board matches the global one
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				b[i][j] = globalBoard[i][j];
		Node father = new Node(foeLastCell.i, foeLastCell.j, ALPHA, BETA, ALPHA, saddamlastRegularMove, foelastRegularMove,
				false);
		Node node = alphaBetaPruning(father);
		// System.out.println("Benito Mussolini "+ node.i);
		saddamlastRegularMove = node.myLastRegularMove;
		foelastRegularMove = node.foeLastRegularMove;
		return new MNKCell(node.i, node.j);
	}

	private Node alphaBetaPruning(Node father) {
		if (father.isLeaf)
			return father;
		currentDepth++;
		Node[] children = findBestNodes(father);
		printMatrix(b);
		for(Node child: children) {
			System.out.println("i: "+child.i + " j: " + child.j);
		}
		// System.out.println(children);
		for (int i = 0; i < children.length; i++) {
			b[children[i].i][children[i].j] = children[i].isSaddam ? saddam : foe;
			// System.out.println("totaler krieg "+children[i].i);
			Node child = alphaBetaPruning(children[i]);
			b[child.i][child.j] = MNKCellState.FREE;
			// System.out.println("father "+father.value);
			// System.out.println("child "+child.value);
			if (children[i].isSaddam) {
				if (currentDepth == 0 && child.value >= father.value) {
					// System.out.println("ubermensch ");
					// relative best child, in the end it will be the best
					father.bestChild = child;
					// System.out.println("heil hitler best "+father.bestChild.i);
				}
				father.value = Math.max(father.value, child.value);
				father.alpha = Math.max(father.alpha, father.value);
			} else {
				father.value = Math.min(father.value, child.value);
				father.beta = Math.min(father.beta, father.value);
			}
			// System.out.println("heil hitler");
			if (father.beta <= father.alpha) {
				break;
			}
		}
		currentDepth--;
		if (currentDepth == -1) {
			// System.out.println("heil hitler best "+father.bestChild.i);
			return children[0];//father.bestChild;
		}

		else {
			// System.out.println("heil hitler "+father.i);
			return father;
		}

	}

	public Node[] findBestNodes(Node father) {
		Node[] children;
		Moves myMoves = new Moves();
		checkAround(father.myLastRegularMove[0], father.myLastRegularMove[1], myMoves, true);
		// System.out.println(myMoves.q.toArray());
		if (myMoves.win == null) {
			Moves foeMoves = new Moves();
			//System.out.println(currentDepth);
			checkAround(father.foeLastRegularMove[0], father.foeLastRegularMove[1], foeMoves, false);

			// System.out.println(foeMoves.q.toArray());
			if (foeMoves.win == null) {
				if (myMoves.twoWin == null) {
					if (foeMoves.twoWin == null) {
						children = new Node[myMoves.q.size()];
						if (currentDepth == MAX_DEPTH - 1) {
							for (int i = 0; !myMoves.q.isEmpty(); i++) {
								int[] m = myMoves.q.remove();
								Node child;
								System.out.println("slaques primo i : "+m[0]+ " j: "+ m[1]);
								child = new Node(m[0], m[1], !father.isSaddam, m[2]);// getHeuristicValue
								children[i] = child;
							}
							// System.out.println("dai cazzo negro");
						} else {
							for (int i = 0; !myMoves.q.isEmpty(); i++) {
								int[] m = myMoves.q.remove();
								Node child;
								System.out.println("slaques secondo i : "+m[0]+ " j: "+ m[1]);
								child = new Node(m[0], m[1], father.alpha, father.beta, father.isSaddam ? BETA : ALPHA,
										new int[]{m[0],m[1]}, father.foeLastRegularMove, !father.isSaddam);
								children[i] = child;
							}
							// System.out.println("dai cazzo");
						}
					} else {
						System.out.println("slaques terzo i : "+foeMoves.twoWin[0]+ " j: "+ foeMoves.twoWin[1]);
						children = new Node[] { new Node(foeMoves.twoWin[0], foeMoves.twoWin[1], father.alpha, father.beta,
								father.isSaddam ? BETA : ALPHA, father.myLastRegularMove, father.foeLastRegularMove, !father.isSaddam) };
						}
				} else {
					System.out.println("slaques quarto i : "+myMoves.twoWin[0]+ " j: "+ myMoves.twoWin[1]);					
					children = new Node[] { new Node(myMoves.twoWin[0], myMoves.twoWin[1], !father.isSaddam,
							!father.isSaddam ? WIN : DEFEAT) };
				}
					
			} else
				children = new Node[] { new Node(foeMoves.win[0], foeMoves.win[1], father.alpha, father.beta,
						father.isSaddam ? BETA : ALPHA, father.myLastRegularMove, father.foeLastRegularMove, !father.isSaddam) };
		} else
			children = new Node[] {
					new Node(myMoves.win[0], myMoves.win[1], !father.isSaddam, !father.isSaddam ? WIN : DEFEAT) };
		// System.out.println("dai cazzo ebreo");
		return children;
	}

	public void checkAround(int i, int j, Moves moves, boolean full) {
		PriorityQueue<int[]> q;
		q = new PriorityQueue<int[]>(8, new Comparatore());
		MNKCellState player = b[i][j];
		int length = 1, backExtra = 0, forwardExtra = 0, backCount, forwardCount;
		boolean freeBack[] = { false, false }, freeForward[] = { false, false }, freeExtraForward = false, freeExtraBack = false;
		 System.out.println("niggers");
		for (backCount = 1; j - backCount >= 0 && b[i][j - backCount] == player; backCount++)
			length++;
		for (forwardCount = 1; j + forwardCount < n && b[i][j + forwardCount] == player; forwardCount++)
			length++;
		System.out.println("forwardCount " + forwardCount);
		if (j - backCount >= 0 && b[i][j - backCount] == MNKCellState.FREE) {
			if (length == k - 1) {
				System.out.println("i : "+i+ " j: "+ (j - backCount));
				int move[] = { i, j - backCount };
				moves.win = move;
				return;
			}
			int rest = k - length - 1;
			for (int c = 1; j - backCount - c >= 0 && b[i][j - backCount - c] == player && c <= rest; c++)
				backExtra++;
			if (length + backExtra == k - 1) {
				int move[] = { i, j - backCount };
				moves.win = move;
				return;
			}
			freeBack[0] = true;
			if (j - backCount - 1 >= 0 && b[i][j - backCount - 1] == MNKCellState.FREE)
				freeBack[1] = true;
			if (j - backCount - backExtra -1 >=0 && b[i][j - backCount - backExtra -1 ] ==MNKCellState.FREE)
				freeExtraBack = true;
			if (full) {
				int[] backMove = { i, j - backCount, length + backExtra };
				 System.out.println("mossa back H "+backMove[0] + backMove[1]);
				q.add(backMove);
			}
		}
		System.out.println(" j " + j + " forwardCount" + forwardCount);
		if (j + forwardCount < n && b[i][j + forwardCount] == MNKCellState.FREE) {
			//System.out.println("zoccolajhuyuyusù");
			if (length == k - 1) {
				int move[] = { i, j + forwardCount };
				moves.win = move;
				return;
			}
			int rest = k - length - 1;

			for (int c = 1; j + forwardCount + c < n && b[i][j + forwardCount + c] == player
					&& c <= rest; c++)
				forwardExtra++;
			System.out.println("length " + length + " forwardExtra " + forwardExtra);
			if (length + forwardExtra == k - 1) {
				//System.out.println("zoccolasù");
				int move[] = { i, j + forwardCount };
				moves.win = move;
				return;
			}
			freeForward[0] = true;
			if (j + forwardCount + 1 < n && b[i][j + forwardCount + 1] ==MNKCellState.FREE )
				freeForward[1] = true;
			if (j + forwardCount + forwardExtra + 1 < n && b[i][j + forwardCount + forwardExtra + 1] ==MNKCellState.FREE )
				freeExtraForward = true;
			 System.out.println("puttana");
			if (full) {
				int[] forwardMove = { i, j + forwardCount, length + forwardExtra };
				q.add(forwardMove);
				 System.out.println("mossa forward H "+forwardMove[0] + forwardMove[1]);

			}
		}

		if (length == k - 2 && freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
			j = freeBack[1] ? j - backCount : j + forwardCount;
			int move[] = { i, j };
			System.out.println("jason : "+j);
			moves.twoWin = move;
		}
		System.out.println("odio i negri");

		if ( freeBack[0] && length + backExtra == k - 2 && freeForward[0] && freeExtraBack ) {
			System.out.println("zoccolasù");
			int move[] = { i, j - backCount};
			moves.twoWin = move;
			return;
		}
		System.out.println("e le donne");

		if ( freeForward[0] && length + forwardExtra == k - 2 && freeBack[0] && freeExtraForward ) {
			System.out.println("zoccolaccia j: "+j+" forwardCount "+forwardCount);
			
			int move[] = { i, j + forwardCount };
			moves.twoWin = move;
			return;
		}
		System.out.println("dio maledetto");

		length = 1; backExtra = 0; forwardExtra = 0;
		freeBack[0] = false; freeBack[1] = false; freeForward[0] = false; freeForward[1] = false; freeExtraForward = false; freeExtraBack = false;
		// Vertical
		for (backCount = 1; i - backCount >= 0 && b[i - backCount][j] == player; backCount++)
			length++;
		for (forwardCount = 1; i + forwardCount < n && b[i + forwardCount][j] == player; forwardCount++)
			length++;
		if (i - backCount >= 0 && b[i - backCount][j] == MNKCellState.FREE) {
			if (length == k - 1) {
				int move[] = { i - backCount, j };
				moves.win = move;
				return;
			}
			int rest = k - length - 1;
			for (int c = 1; i - backCount - c >= 0 && b[i - backCount - c][j] == player && c <= rest; c++)
				backExtra++;
			if (length + backExtra == k - 1) {
				int move[] = { i - backCount, j };
				moves.win = move;
				return;
			}
			freeBack[0] = true;
			if (i - backCount - 1 >= 0 && b[i - backCount - 1][j] == MNKCellState.FREE)
				freeBack[1] = true;
			if (i - backCount - backExtra -1 >=0 && b[i - backCount - backExtra -1][j] ==MNKCellState.FREE)
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
			for (int c = 1; i + forwardCount + c < n && b[i + forwardCount + c][j] == player
					&& c <= rest; c++)
				forwardExtra++;
			if (length + forwardExtra == k - 1) {
				int move[] = { i + forwardCount, j };
				moves.win = move;
				return;
			}
			freeForward[0] = true;
			if (i + forwardCount + 1 < n && b[i + forwardCount + 1][j] == MNKCellState.FREE)
				freeForward[1] = true;
			if (i + forwardCount + forwardExtra + 1 < n && b[i + forwardCount + forwardExtra + 1][j] ==MNKCellState.FREE )
				freeExtraForward = true;
			if (full) {
				int[] forwardMove = { i + forwardCount, j, length + forwardExtra };
				q.add(forwardMove);
			}
		}
		if (length == k - 2 && freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
			i = freeBack[1] ? i - backCount : i + forwardCount;
			int move[] = { i, j };
			moves.twoWin = move;
		}
		if ( freeBack[0] && length + backExtra == k - 2 && freeForward[0] && freeExtraBack ) {
			System.out.println("zoccolasjjjjjjù");
			int move[] = { i - backCount, j };
			moves.twoWin = move;
			return;
		}
		if ( freeForward[0] && length + forwardExtra == k - 2 && freeBack[0] && freeExtraForward ) {
			System.out.println("zoccolashhhhù i "+i + "+ forwardCount "+forwardCount);
			int move[] = { i + forwardCount,j };
			moves.twoWin = move;
			return;
		}
		length = 1; backExtra = 0; forwardExtra = 0;
		freeBack[0] = false; freeBack[1] = false; freeForward[0] = false; freeForward[1] = false;freeExtraForward = false; freeExtraBack = false;
		System.out.println("odio la nigeria");

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
			if (j - backCount - backExtra -1 >=0 && i - backCount - backExtra -1 >=0 && b[i - backCount -backExtra -1][j - backCount - backExtra -1 ] ==MNKCellState.FREE)
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
			if (j + forwardCount + forwardExtra + 1 < n && i + forwardCount + forwardExtra + 1 <n && b[i + forwardCount + forwardExtra +1][j + forwardCount + forwardExtra + 1] ==MNKCellState.FREE )
				freeExtraForward = true;
			if (full) {
				int[] forwardMove = { i + forwardCount, j + forwardCount, length + forwardExtra };
				q.add(forwardMove);
			}
		}
		if (length == k - 2 && freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
			i = freeForward[1] ? i - backCount : i + forwardCount;
			j = freeBack[1] ? j - backCount : j + forwardCount;
			int move[] = { i, j };
			moves.twoWin = move;
		}
		if ( freeBack[0] && length + backExtra == k - 2 && freeForward[0] && freeExtraBack ) {
			System.out.println("zoccolasgggggggù");
			int move[] = { i - backCount, j - backCount };
			moves.twoWin = move;
			return;
		}
		if ( freeForward[0] && length + forwardExtra == k - 2 && freeBack[0] && freeExtraForward ) {
			System.out.println("zoccolasttttttù");
			int move[] = { i + forwardCount, j + forwardCount };
			moves.twoWin = move;
			return;
		}
		length = 1; backExtra = 0; forwardExtra = 0;
		freeBack[0] = false; freeBack[1] = false; freeForward[0] = false; freeForward[1] = false;freeExtraForward = false; freeExtraBack = false;
		System.out.println("mi masturbo sulle loli");

		// Antidiagonal
		for (backCount = 1; j + backCount < n && i - backCount >= 0
				&& b[i - backCount][j + backCount] == player; backCount++)
			length++;
		for (forwardCount = 1; j - forwardCount >= 0 && i + forwardCount < n
				&& b[i + forwardCount][j - forwardCount] == player; forwardCount++)
			length++;
			System.out.println("jeff bezos");

		if (j + backCount < n && i - backCount >= 0 && b[i - backCount][j + backCount] == MNKCellState.FREE) {
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
			if (i - backCount - backExtra -1 >=0 && j + backCount + backExtra +1 <n && b[i - backCount - backExtra -1][j + backCount + backExtra +1 ] ==MNKCellState.FREE)
				freeExtraBack = true;
			if (full) {
				int[] backMove = { i - backCount, j + backCount, length + backExtra };
				q.add(backMove);
			}
		}
		System.out.println("no non vero");

		if (j - forwardCount >= 0 && i + forwardCount < n
				&& b[i + forwardCount][j - forwardCount] == MNKCellState.FREE) {
					System.out.println("chill");

			if (length == k - 1) {
				int move[] = { i - forwardCount, j + forwardCount };
				moves.win = move;
				return;
			}

			int rest = k - length - 1;
			for (int c = 1; j - forwardCount - c >= 0 && i + forwardCount + c < n
					&& b[i + forwardCount + c][j - forwardCount - c] == player && c <= rest; c++)
				forwardExtra++;

			if (length + forwardExtra == k - 1) {
				int move[] = { i - forwardCount, j + forwardCount };
				moves.win = move;
				return;
			}
			freeForward[0] = true;
			if (j - forwardCount - 1 >= 0 && i + forwardCount + 1 < n
					&& b[i + forwardCount + 1][j - forwardCount - 1] == MNKCellState.FREE)
				freeForward[1] = true;
			if (i + forwardCount + forwardExtra + 1 < n && j - forwardCount -forwardExtra -1 >=0 && b[i+ forwardCount + forwardExtra +1][j - forwardCount - forwardExtra - 1] ==MNKCellState.FREE )
				freeExtraForward = true;
			 System.out.println("are disgusting");
			if (full) {
				int[] forwardMove = { i + forwardCount, j - forwardCount, length + forwardExtra };
				q.add(forwardMove);
			}
		}
		System.out.println("odio il congo");

		if (length == k - 2 && freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
			i = freeBack[1] ? i - backCount : i + forwardCount;
			j = freeBack[1] ? j + backCount : j - forwardCount;
			int move[] = { i, j };
			moves.twoWin = move;
		}
		if ( freeBack[0] && length + backExtra == k - 2 && freeForward[0] && freeExtraBack ) {
			System.out.println("zoccolasgggggggù");
			int move[] = { i - backCount, j + backCount };
			moves.twoWin = move;
			return;
		}
		if ( freeForward[0] && length + forwardExtra == k - 2 && freeBack[0] && freeExtraForward ) {
			System.out.println("zoccolasttttttù");
			int move[] = { i + forwardCount, j - forwardCount };
			moves.twoWin = move;
			return;
		}
		System.out.println("and filthy");
		if (full)
			moves.q = q;
		//if (q.isEmpty())
		/*	System.out.println("porcodio");
		q.forEach(e -> {
			System.out.println("i: " + e[0] + " j: " + e[1] + " length: " + e[2]);
		});*/
	}

	public void test() {
		b[3][0] = foe;
		b[2][0] = saddam;
		b[0][0] = saddam;
		b[2][2] = saddam;
		b[2][3] = saddam;
		b[3][3] = foe;
		printMatrix(b);
		Moves myMoves = new Moves();
		Moves foeMoves = new Moves();
		checkAround(2, 0, myMoves, true);
		checkAround(3, 3, foeMoves, false);
		System.out.println("mossa vincente "+myMoves.win[0]+ myMoves.win[1]);
		
	}

	private void printMatrix(MNKCellState[][] matrix) {
		Arrays.stream(matrix).forEach((row) -> {
			System.out.print("[");
			Arrays.stream(row).forEach((el) -> System.out.print(" " + el + " "));
			System.out.println("]");
		});
	}
}
