import java.awt.*;
import javax.swing.Icon;

public class CircleIcon implements Icon {
	int size;
	public CircleIcon (int size) {
		this.size = size;
	};	
	public int getIconWidth() {
		return size;
	}
	public int getIconHeight() {
		return size;
	}
	public void paintIcon(Component c, Graphics g, int x, int y) {
		g.setColor(Color.BLACK);
		g.fillOval(x, y, getIconWidth(), getIconHeight());
	}
}
	