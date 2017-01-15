import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class ClientList extends ArrayList<ObjectOutputStream> {
		public synchronized boolean add(ObjectOutputStream cl) {
			return super.add(cl);
		}
		public synchronized boolean remove(ObjectOutputStream cl) {
			return super.remove(cl);
		}
	}