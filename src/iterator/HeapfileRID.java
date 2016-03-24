package iterator;

import global.AttrType;
import global.RID;
import heap.Heapfile;

public class HeapfileRID {
	public final Heapfile hf;
	public final RID[] rid;
	public final AttrType[] datatypes;
	public final short [] stringSizes;
	
	public HeapfileRID(Heapfile hf, RID[] rid, AttrType[] datatypes, short[] stringSizes) {
		this.hf = hf;
		this.rid = rid;
		this.datatypes = datatypes;
		this.stringSizes = stringSizes;
	}
}
