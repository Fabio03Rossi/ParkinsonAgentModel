package parkinson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;

import java.util.Random;

import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

public class Player extends GridObj {
	private static final Random rnd = new Random();
    
    private Hole targetHole;
    private Tile targetTile;
    private Tile tileToPush;
    
    private GridPoint pushPosition; // La cella in cui il player deve stare per spingere
    private GridPoint nextTilePos;  // La cella in cui il tile finirà dopo la spinta
	private PlayerState currentState = PlayerState.POSITIONING;
	
    private Strategy strategy;
	
	public Player(Context context, Grid<Object> grid)
	{
		super(context, grid);
        this.strategy = new CompetitiveStrategy();
	}

    @ScheduledMethod(start = 1, interval = 1, priority = 5)
    public void step1() {
        switch (currentState) {
            case POSITIONING:
                List<Tile> tiles = calcClosestTile(this);
                if (tiles != null && !tiles.isEmpty()) {

                    for(Tile t : tiles) {
                    	if(strategy.isItUseful(t)) {
                    		targetTile = t;
                    		break;
                    	}
                    }

                }
                if(targetTile != null){
                    targetTile.decrementUtility();
                    currentState = PlayerState.NEW_TRAJ_CALC;
                }

            break;
                
            case NEW_TRAJ_CALC:
            	if(calcTraj()) {
            		currentState = PlayerState.MOVING_TO_PUSH;
            	}
            	else {
                    // Se non c'è percorso, il Tile è incastrato. Resetta i target.
                    targetTile = null;
                    targetHole = null;
                    currentState = PlayerState.POSITIONING;
                }
            break;

            case MOVING_TO_PUSH:
                if (moveToPushPosition()) {
                    currentState = PlayerState.PUSHING;
                }
                break;

            case PUSHING:
                executePush();
                break;
        }
    }

    private boolean calcTraj() {
    	
    	if(!context.contains(targetTile)) return false;

    	// Calculate closes hole in the respect of closest tile    	
        GridPoint tileLoc = grid.getLocation(targetTile);

        if(strategy.isItStillUseful(targetTile) == false) {
            targetTile.incrementUtility();
            return false;
        }

        calcClosestHole(targetTile);
        GridPoint holeLoc = grid.getLocation(targetHole);
        
        // 1. Calcola il percorso per il Tile fino all'Hole
        List<GridPoint> tilePath = findPath(tileLoc, holeLoc);
        
        if (tilePath == null || tilePath.isEmpty()) {
            return false; // Nessun percorso possibile per il Tile
        }
        
        // 2. Il prossimo passo che il Tile deve fare
        nextTilePos = tilePath.get(0);
        // 3. Calcola dove deve mettersi il Player per spingerlo (direzione opposta)
        int dx = nextTilePos.getX() - tileLoc.getX();
        int dy = nextTilePos.getY() - tileLoc.getY();
        pushPosition = new GridPoint(tileLoc.getX() - dx, tileLoc.getY() - dy);

        if(grid.getLocation(this) == pushPosition) return true;
        
/*
        boolean found = false;
        
        if(!isValidPosition(pushPosition)) 
        {
        	for(GridDirection dir : GridDirection.values()) 
        	{
        		GridPoint x = getNghPoint(nextTilePos, dir);
        		if(isValidPosition(x)) {
        			found = true;
        			break;
        		}
        	}

        }
        else return true;
        */
        return true;
    }

    private boolean moveToPushPosition() {

        GridPoint myLoc = grid.getLocation(this);

        
    	// Calculate closes hole in the respect of closest tile
        GridPoint tileLoc = grid.getLocation(targetTile);
        //calcClosestHole(targetTile);
        GridPoint holeLoc = grid.getLocation(targetHole);
        
    	if(!context.contains(targetTile)) return false;
    	
        // Se sono già nella posizione di spinta, ho finito
        if (myLoc.equals(pushPosition)) {
            return true;
        }

        // Altrimenti, calcolo il percorso verso la pushPosition.
        List<GridPoint> playerPath = findPath(myLoc, pushPosition);

        if (playerPath != null && !playerPath.isEmpty()) {
            GridPoint nextStep = playerPath.get(0);
            grid.moveTo(this, nextStep.getX(), nextStep.getY());
            
            // Se il prossimo passo era esattamente la pushPosition, ritorno true per il prossimo tick
            if(nextStep.equals(pushPosition) == true) return true;
        }
        
        currentState = PlayerState.NEW_TRAJ_CALC;
        return false;
    }

    private void executePush() {
        
        GridPoint tileLoc = grid.getLocation(targetTile);
        GridPoint holeLoc = grid.getLocation(targetHole);
        
    	Context<Object> context = ContextUtils.getContext(this);
    	if(!context.contains(targetTile)) {
            currentState = PlayerState.POSITIONING;
            return; 
    	};
        
        // Controlla se un altro player sta spingendo il tile
        GridPoint targetPos = grid.getLocation(targetHole);
        
        targetTile.move(nextTilePos.getX(), nextTilePos.getY());
        
        // 2. Sposta il Player dove si trovava il Tile
        grid.moveTo(this, tileLoc.getX(), tileLoc.getY());
        
        // 3. Controlla se il Tile è finito sull'Hole
        if (nextTilePos.equals(targetPos)) {
            
            // Resetta la missione
            targetTile = null;
            targetHole = null;
            currentState = PlayerState.POSITIONING;
            return;
        }

        currentState = PlayerState.NEW_TRAJ_CALC;

    }

    // Classe interna per i Nodi dell'A*
    private class Node implements Comparable<Node> {
        GridPoint point;
        Node parent;
        double moveCost, globalCost;

        Node(GridPoint point, Node parent, double gCost, double hCost) {
            this.point = point;
            this.parent = parent;
            this.moveCost = gCost;
            this.globalCost = hCost;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.globalCost  + this.moveCost, other.globalCost + other.moveCost);
        }
    }

    private List<GridPoint> findPath(GridPoint start, GridPoint target) {
        PriorityQueue<Node> nodesToVisit = new PriorityQueue<>();
        Set<GridPoint> visitedNodes = new HashSet<>();

        nodesToVisit.add(new Node(start, null, 0, getManhattanDistance(start, target)));
        //System.out.println("start pos"+start.getX() + start.getY());
        while (!nodesToVisit.isEmpty()) {
            //System.out.println("visiting new node...");

            Node current = nodesToVisit.poll();

            if (current.point.equals(target)) {
            	// return list of grid directions
                //return pathing;
                return reconstructPath(current);
            }

            visitedNodes.add(current.point);
            // Controlla i 4 vicini 
            for (GridDirection dir : GridDirection.values()) {
                GridPoint neighborPos = this.getNghPoint(current.point, dir);

                if (!visitedNodes.contains(neighborPos)) {
                	
            		if(target.equals(neighborPos)) {
            			return reconstructPath(new Node(neighborPos, current, current.moveCost, getManhattanDistance(neighborPos, target)));
            		}
            		
            		if(!isValidPosition(neighborPos)) continue;
                	
                    double tentativeGCost = current.moveCost + 1;
                    Node neighborNode = new Node(neighborPos, current, tentativeGCost, getManhattanDistance(neighborPos, target));
                    
                    // Aggiungi nuovo nodo solo se non c'è o se è migliore di quelli trovati prima
                    boolean inNodesToVisit = false;
                    for (Node n : nodesToVisit) {
                        if (n.point.equals(neighborPos) && n.moveCost <= tentativeGCost) {
                        	inNodesToVisit = true;
                            break;
                        }
                    }
                    
                    if (!inNodesToVisit) {
                        nodesToVisit.add(neighborNode);
                    }
                }
            }
        }
        return null; // Percorso non trovato
    }

    private List<GridPoint> reconstructPath(Node node) {
        List<GridPoint> path = new ArrayList<>();
        // Salta il nodo finale (che è il target) e risale. 
        // Vogliamo l'elenco dei passi da fare partendo dalla posizione attuale.
        while (node.parent != null) {
            path.add(node.point);
            node = node.parent;
        }
        Collections.reverse(path);
        return path;
    }

    private double getManhattanDistance(GridPoint a, GridPoint b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    private boolean isValidPosition(GridPoint pos) {
        // Controllo limiti griglia

        Iterable<Object> objectsAt = grid.getObjectsAt(pos.getX(), pos.getY());
        //System.out.println("adj"+pos.getX() + pos.getY());
        
        for (Object obj : objectsAt) {
            // Nessuno può camminare sugli Hole
            if (obj instanceof GridObj && obj != this) {
                return false;
            }
        }
        return true;
    }

    // RICERCA TARGET (Base)
    
    // calc closest hole
    private void calcClosestHole(Object ref) {
        Iterable<Object> holeList = context.getObjects(Hole.class);
        double minDist = Double.MAX_VALUE;
        for (Object holeObj : holeList) {
            Hole hole = (Hole) holeObj;
            double curDist = getManhattanDistance(grid.getLocation(ref), grid.getLocation(hole));
            if (minDist > curDist) {
                minDist = curDist;
                this.targetHole = hole;
            }
        }
    }
    
    // calc closest tile in the respect of player
    private List<Tile> calcClosestTile(Object ref) {
        Context<Object> context = ContextUtils.getContext(this);
        Iterable<Object> tileList = context.getObjects(Tile.class);
        List<Tile> closestTiles = new ArrayList<>();

        double minDist = Double.MAX_VALUE;
        for (Object tileObj : tileList) {
            Tile tile = (Tile) tileObj;
            closestTiles.add(tile);
            double curDist = getManhattanDistance(grid.getLocation(ref), grid.getLocation(tile));
            if (minDist > curDist) {
                minDist = curDist;
                this.targetTile = tile;
            }
        }
        closestTiles.sort((tile1, tile2) -> Double.compare(
                getManhattanDistance(grid.getLocation(ref), grid.getLocation(tile1)),
                getManhattanDistance(grid.getLocation(ref), grid.getLocation(tile2))
            ));
        
        return closestTiles; 
    }
    
	public GridPoint getNghPoint(GridPoint src, GridDirection direction) {
		
		int newX = src.getX() + direction.getX();
		int newY = src.getY() + direction.getY();
		
		return new GridPoint(newX, newY);
	}
	
	public boolean move(GridDirection direction) {

		int newX = this.grid.getLocation(this).getX() + direction.getX();
		int newY = this.grid.getLocation(this).getY() + direction.getY();

		return this.grid.moveTo(this, newX, newY);
	}



}
