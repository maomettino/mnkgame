import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Collections;
import javax.naming.directory.DirContext;
public class Test {
	private static int[][] swag;
	private static int[][] vagina;
	public static void diocane() {
		PriorityQueue q = new PriorityQueue<Move>(new Comparatore());
		
		q.add(new Move(0,0,15));
		q.add(new Move(0,0,20));
		q.add(new Move(0,0,25));
		q.add(new Move(0,0,30));
		for(int i=0;i<4;i++) {
			Move m = (Move)q.remove();
        	System.out.println("Nigger: "+m.length);
		}
	}
    public static void main(String[] args) {
		swag = new int[2][2];
		vagina = new int[2][2];
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 2; j++){
				swag[i][j] = 0;
				vagina[i][j] = swag[i][j];
			}
		vagina[1][1] = 1;
		printMatrix(vagina);
		printMatrix(swag);
    }

	public static void nigger(int[][] nigg) {
		nigg[1][1] = 1;
		//printMatrix(swag);
    }
	
	public static class Move  {
		public  int i,j,length;
		public Move(int i, int j ,int length) {
			this.i = i; 
			this.j = j;
			this.length = length;
		}
	}

	public static class Comparatore implements Comparator<Move> {

		//what we need is the reverse natural order, hence the existence of this comparator
			@Override
			public int compare(Move m1, Move m2) {
				if (m1.length < m2.length)
					return 1;
				else if (m1.length > m2.length)
					return -1;
				return 0;
				}
		}
	
   static private void printMatrix(int[][] matrix) {
		Arrays.stream(matrix).forEach((row) -> {
			System.out.print("[");
			Arrays.stream(row).forEach((el) -> System.out.print(" " + el + " "));
			System.out.println("]");
		});
	}
}
