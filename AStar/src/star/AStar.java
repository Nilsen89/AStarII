package star;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AStar {
	public void runner(String[] args) {
		Input input = new Input(args);
		Algorithm alg = new Algorithm(input.getBoard(), input.getGoal(), input.getStart());
		alg.findShortestPath(input.getRunType());
	}
	public static void main(String[] args) {
		System.out.println("Running...");
		AStar astar = new AStar();
		astar.runner(args);
	}
}

class Input {
	
	private ArrayList<ArrayList<Node>> board = new ArrayList<ArrayList<Node>>();
	private Node goal, start;
	private char runType;
	
	public Input(String[] args) {
		if (args.length == 2) {
			setRunType(args[1]);
		    String readLine = "";
		    String filename = "boards/"+args[0];
		    ArrayList<Node> tempBoard = new ArrayList<Node>();
		    try {
		    	BufferedReader br = new BufferedReader(new FileReader(filename));
		    	int i = 0;
		    	while ((readLine = br.readLine()) != null) {
		    		
		    		/*MAKE BOARD INTO ARRAY*/
		    		for(int z = 0; z < readLine.length(); z++) {
		    			Node tempNode = new Node(i, z, readLine.charAt(z));
		    			tempBoard.add(tempNode);
		    			if(tempNode.symbol == 'B') {
		    				goal = tempNode;
		    			} else if(tempNode.symbol == 'A') {
		    				start = tempNode;
		    			}
		    		}
		    		i++;
		    		board.add(tempBoard);
		    		tempBoard = new ArrayList<Node>();
		    	}
		    	br.close();
		    } catch (IOException e) {
		    	System.err.println("Error Happened: " + e);
		    }
		}
	}
	
	private void setRunType(String arg) {
		if(arg.equals("-astar")) {
			runType = 'a';
		} else if(arg.equals("-dijk")) {
			runType = 'd';
		} else if(arg.equals("-bfs")) {
			runType = 'b';
		} else {
			try {
				throw new Exception("Did you mean either of: '-astar / -dijk / -bfs' ?");
			} catch (Exception e) {e.printStackTrace();}
		}
		System.out.println("Running algorithm: " + arg.substring(1,arg.length()));
	}
	
	public char getRunType() {
		return runType;
	}

	public ArrayList<ArrayList<Node>> getBoard() {
		return board;
	}
	public Node getGoal() {
		return goal;
	}
	public Node getStart() {
		return start;
	}
}

class Algorithm {
	
	private Node goal, start;
	private int x, y;
	private ArrayList<Node> open, closed;
	private ArrayList<ArrayList<Node>> board;
	
	public Algorithm(ArrayList<ArrayList<Node>> board, Node goal, Node start) {
		this.start = start;
		this.goal = goal;
		x = start.x;
		y = start.y;
		this.board = board;
		open = new ArrayList<Node>();
		closed = new ArrayList<Node>();
	}
	
	public void findShortestPath(char runType) {
		printData();
		open.add(board.get(x).get(y));
		
		while(true) {
			findNeighbours();
			placeCurrentInClosed();
			sortOpened(runType);
			nextNode();
			if(x == goal.x && y == goal.y) {
				System.out.println("Path found!");
				showShortestPath();
				break;
			}
		}
		printData();
	}
	
	private void showShortestPath() {
		Node tempNode = goal.origin;
		while (tempNode != null) {
			tempNode.symbol = '@';
			tempNode = tempNode.origin;
		}
		board.get(start.x).get(start.y).symbol = 'A';
	}

	private void nextNode() {
		x = open.get(0).x;
		y = open.get(0).y;
	}

	private void sortOpened(char runType) {
		switch(runType) {
			case('a'):
				//AStar - Sort by F(x) 
				Collections.sort(open, new Comparator<Node>(){
				    public int compare(Node n1, Node n2) {
				    	if(n1.f > n2.f) {
				    		return 1;
				    	} else {
				    		return -1;
				    	}
				    }
				});
				break;
			case('b'):
				//BFS - First in first out:: Nothing to do here
				break;
			case('d'):
				//Dijkstas - Sort by G(x)
				Collections.sort(open, new Comparator<Node>(){
				    public int compare(Node n1, Node n2) {
				    	if(n1.g > n2.g) {
				    		return 1;
				    	} else {
				    		return -1;
				    	}
				    }
				});
				break;
				
		}
		Collections.sort(open, new Comparator<Node>(){
		    public int compare(Node n1, Node n2) {
		    	if(n1.f > n2.f) {
		    		return 1;
		    	} else {
		    		return -1;
		    	}
		    }
		});
	}

	private void placeCurrentInClosed() {
		closed.add(open.remove(0));
		visualizer();
	}
	
	private void visualizer() {
		for(int i = 0; i < open.size(); i++) {
			open.get(i).symbol = '+';
		}
		for(int i = 0; i < closed.size(); i++) {
			closed.get(i).symbol = '-';
		}
		board.get(start.x).get(start.y).symbol = 'A';
		board.get(goal.x).get(goal.y).symbol = 'B';
	}

	private void findNeighbours() {
		Node tempNode;
		for(int i = -1; i < 2; i++) {
			for(int z = -1; z < 2; z++) {
				if(Math.abs(i) == Math.abs(z)){
					continue;
				}
				int tempX = x + i;
				int tempY = y + z;
				if((tempX < 0 || tempX >= board.size())
				|| (tempY < 0 || tempY >= board.get(tempX).size())) {
					continue;
				}
				tempNode = board.get(tempX).get(tempY);
				if(!open.contains(tempNode) && !closed.contains(tempNode)) {
					if(!tempNode.wall) {
						updateH(tempNode);
						updateG(board.get(x).get(y), tempNode);
						updateF(tempNode);
						setOrigin(board.get(x).get(y), tempNode);
						open.add(tempNode);
					}
				}
			}
		}
		
	}

	private void updateH(Node node) {
		node.h = Math.sqrt(Math.pow(node.y - goal.y, 2) + Math.pow(node.x - goal.x, 2));
	}
	
	private void updateG(Node cur, Node discovered) {
		switch(discovered.symbol) {
			case ('.'):
				discovered.g = cur.g + 1;
				break;
			case ('r'):
				discovered.g = cur.g + 1;
				break;
			case ('g'):
				discovered.g = cur.g + 5;
				break;
			case ('f'):
				discovered.g = cur.g + 10;
				break;
			case ('m'):
				discovered.g = cur.g + 50;
				break;
			case ('w'):
				discovered.g = cur.g + 100;
				break;
		}
	}
	
	private void updateF(Node node) {
		 node.f = node.g + node.h;
	}
	
	private void setOrigin(Node cur, Node discovered) {
		discovered.origin = cur;
	}
	
	public void printData() {
		System.out.println("SIZE OF OPEN :: " + open.size());
		System.out.println("SIZE OF CLOSED :: " + closed.size());
		for(int x = 0; x < board.size(); x++) {
			for(int y = 0; y < board.get(x).size(); y++) {
				System.out.print(board.get(x).get(y).symbol);
			}
			System.out.println();
		}
	}
}

class Node {
	public int x, y;
	public double g, h, f;
	public boolean wall = false;
	public char symbol = '.';
	public Node origin = null;
	
	public Node(int x, int y, char symbol) {
		this.x = x;
		this.y = y;
		this.symbol = symbol;
		if(symbol == '#') {
			wall = true;
		}
	}
}