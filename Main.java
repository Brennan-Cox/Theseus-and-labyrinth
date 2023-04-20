import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.StringTokenizer;
 
public class Main {
	
	public Main() {
		
		FastScanner input = new FastScanner(System.in);
		int rows = input.nextInt();
		int columns = input.nextInt();
		
		/*
		 * Much less space at maximum 4*10^6 to store byte 3D array
		 */
		byte[][][] graph = new byte[4][rows][columns];
		
		{//This section does some compression
			//rotational mapping
			HashMap<Character, Character> rotate = new HashMap<>();
			{
				rotate.put('+', '+');
				rotate.put('-', '|');
				rotate.put('|', '-');
				rotate.put('^', '>');
				rotate.put('>', 'v');
				rotate.put('v', '<');
				rotate.put('<', '^');
				rotate.put('L', 'U');
				rotate.put('U', 'R');
				rotate.put('R', 'D');
				rotate.put('D', 'L');
				rotate.put('*', '*');
			}
			
			//character to byte mapping
			/*
			 * Character to byte mapping works where each bit defines a direction of travel
			 * URDL where bit 1 is U 2 is R 3 is D and 4 is L
			 * Later some math is done to determine adjacency on the fly
			 * (not as memory heavy)
			 */
			HashMap<Character, Byte> charToByte = new HashMap<>();
			{
				charToByte.put('^', (byte)8);//1000
				charToByte.put('>', (byte)4);//0100
				charToByte.put('v', (byte)2);//0010
				charToByte.put('<', (byte)1);//0001
				charToByte.put('|', (byte)10);//1010
				charToByte.put('-', (byte)5);//0101
				charToByte.put('U', (byte)7);//0111
				charToByte.put('R', (byte)11);//1011
				charToByte.put('D', (byte)13);//1101
				charToByte.put('L', (byte)14);//1110
				charToByte.put('+', (byte)15);//1111
				charToByte.put('*', (byte)0);//0000
			}
			
			//basically for each position give each rotation at each state
			//This sets up a 3d graph of bytes corresponding to thier characters
			for (int row = 0; row < rows; row++) {
				
				String line = input.next();
				for (int column = 0; column < columns; column++) {
					
					char current = line.charAt(column);
					//given input per State log and rotate
					for (int i = 0; i < 4; i++) {
						
						graph[i][row][column] = charToByte.get(current);
						current = rotate.get(current);
					}
				}
			}
		}
		
		//bfs data struct
		ArrayDeque<Point> levelDeque = new ArrayDeque<>();
		
		{
			//save space
			short startX = Short.parseShort(Integer.toString(input.nextInt() - 1));
			short startY = Short.parseShort(Integer.toString(input.nextInt() - 1));
			
			//notice points are only kept while on the deque to process
			levelDeque.addFirst(new Point((byte)0, startX, startY));
			
			/* use of this "dummy" object will allow me to
			 * get away with knowing which level I am at in my BFS without 
			 * storing a big bulky 3d array of bytes
			 */
			levelDeque.addLast(new Point((byte) -1, (short) -1, (short) -1));
		}
		
		//simply note what x and y result in a finish
		short endX = Short.parseShort(Integer.toString(input.nextInt() - 1));
		short endY = Short.parseShort(Integer.toString(input.nextInt() - 1));
			
		//minimum moves
		long minimum = 0;
		boolean solved = false;
		
		//while deque has more than dummy
		while(levelDeque.size() > 1) {
			
			Point next = levelDeque.pop();
			if (next.state == -1) {
				
				//increment minimum and move dummy point, tells where new layer begins
				minimum++;
				levelDeque.addLast(next);
			} else {
				
				if (next.row == endX && next.column == endY) {
					
					solved = true;
					break;
					//ifDone^^
					//don't waste calculation time
				} else if (graph[next.state][next.row][next.column] != 0){
					
					byte state = next.state;
					short row = next.row;
					short column = next.column;
					//get all attributes
					
					if (graph[(state + 1) % 4][row][column] > 0) {
						levelDeque.addLast(new Point((byte) ((state + 1) % 4), row, column));
					}//attempt to go to adjacent state
					
					/*
					 * Section is concerned with going to adjacency when possible
					 */
					byte current = graph[state][row][column];
					
					//if can go up from position and can go down from position
					if ((current & 8) == 8 && row > 0 && (graph[state][row - 1][column] & 2) == 2) {
						levelDeque.addLast(new Point(state, (short)(row - 1), column));
					}
					
					//if can go right and recieve right
					if ((current & 4) == 4 && column + 1 < columns && (graph[state][row][column + 1] & 1) == 1) {
						levelDeque.addLast(new Point(state, row, (short)(column + 1)));
					}
					
					//can go down and recieve down
					if ((current & 2) == 2 && row + 1 < rows && (graph[state][row + 1][column] & 8) == 8) {
						levelDeque.addLast(new Point(state, (short)(row + 1), column));
					}
					
					//can do left and recieve left
					if ((current & 1) == 1 && column > 0 && (graph[state][row][column - 1] & 4) == 4) {
						levelDeque.addLast(new Point(state, row, (short)(column - 1)));
					}
					
					//now completely done with element therefore get rid of it
					//in other words this position will no longer go any direction
					graph[state][row][column] = 0;
				}
			}
		}
		
		if (solved) {
			System.out.println(minimum);
		} else {
			System.out.println(-1);
		}
		
		
	}
	
	public static void main(String[] args) {
		new Main();
	}
	
	class Point {
		byte state;
		short row;
		short column;
		
		public Point(byte state, short x, short y) {
			super();
			this.state = state;
			this.row = x;
			this.column = y;
		}
	}
	
	class FastScanner {
        BufferedReader br;
        StringTokenizer st;
 
        public FastScanner(Reader in) {
            br = new BufferedReader(in);
        }
 
        public FastScanner(InputStream in) {
            this(new InputStreamReader(in));
        }
 
        public String next() {
            while (st == null || !st.hasMoreElements()) {
                try {
                    st = new StringTokenizer(br.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return st.nextToken();
        }
 
        public int nextInt() {
            return Integer.parseInt(next());
        }
        public long nextLong() {
            return Long.parseLong(next());
        }
        public double nextDouble() { return Double.parseDouble(next());}
 
    }
}
