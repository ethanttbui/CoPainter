import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

public class Path implements Serializable {
	ArrayList<Point> points = new ArrayList<Point>();
	Color color;
	int size;
	
	public Path(Color color, int size) {
		this.color = color;
		this.size = size;
	}
}