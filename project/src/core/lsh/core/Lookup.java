package lsh.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Read and store a dataset in corner->points format:
 * 	int,int,int\tid,double,double,double|[id...]
 */

public class Lookup {
	final Hasher hasher;
	final Set<Point> points;
	final Set<Corner> corners;
	final Set<String> ids;
	final Map<String,Point> id2point;
	final Map<Corner, Set<Point>> corner2points;
	final HashMap<Point, Set<Corner>> point2corners;
	
	public Lookup(Hasher hasher, boolean doPoints, boolean doCorners, boolean doIds, boolean doId2point, boolean doCorner2points, boolean doPoint2corners) {
		this.hasher = hasher;
		points = doPoints ? new HashSet<Point>() : null;
		corners = doCorners ? new HashSet<Corner>() : null;
		id2point = doId2point ? new HashMap<String,Point>() : null;
		ids = doIds ? new HashSet<String>() : null;
		corner2points = doCorner2points ? new HashMap<Corner, Set<Point>>() : null;
		point2corners = doPoint2corners ? new HashMap<Point, Set<Corner>>() : null;
	}

	private void load(Reader r) throws IOException {
		Utils.load_corner_points_format(r, points, corners, ids, id2point, corner2points, point2corners);
	}

	private Collection<Corner> getMatchingCorners(String id) {
		Set<Corner> found = new HashSet<Corner>();
		for(Corner corner: corners) {
			for(Point point: corner2points.get(corner)) {
				if (point.id.equals(id)) {
					if (! found.contains(corner))
						found.add(corner);
				}
			}
		}
		return found;
	}

	/**
	 * Usage: directory/file hasher N boxsize id
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String path = args[0];
		File svg = new File(path);
		Reader r = new FileReader(svg);

		
		int dim = Integer.parseInt(args[2]);
		double gridsize = Double.parseDouble(args[3]);
		Hasher hasher = null;
		if (args[1].startsWith("ortho")) {
			hasher = new OrthonormalHasher(dim, gridsize);
		} else if (args[1].startsWith("vertex")) {
			hasher = new VertexTransitiveHasher(dim, gridsize);
		} 

		Lookup lookup = new Lookup(hasher, true, true, true, true, true, true);
		lookup.load(r);
		if (args.length == 5) {
			Collection<Corner> corners = lookup.getMatchingCorners(args[4]);
			printPoints(corners, args[4]);
		} else while (true){
			System.out.print("Point id: ");
			byte[] bytes = new byte[1024];
			int len = System.in.read(bytes);
			if (len > 0) {
				String id = new String(bytes, "UTF-8");
				Collection<Corner> corners = lookup.getMatchingCorners(args[4]);
				printPoints(corners, id);				
			} else {
				break;
			}
		}
	}

	private static void printPoints(Collection<Corner> corners, String id) {
		System.out.println("Searching for id: " + id);
		System.out.println("# of corners: " + corners.size());
		for(Corner corner: corners) {
			System.out.print("\t" + corner.hashes[0]);
			for(int i = 1; i < corner.hashes.length; i++) {
				System.out.print(",");
				System.out.print(corner.hashes[i]);
			}
			System.out.println();
		}
	}
}
