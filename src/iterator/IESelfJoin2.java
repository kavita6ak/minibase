/**
 * 
 */
package iterator;

import index.IndexException;

import java.io.IOException;

import bufmgr.PageNotReadException;
import global.AttrType;
import global.GlobalConst;
import global.TupleOrder;
import heap.Heapfile;
import heap.InvalidTupleSizeException;
import heap.InvalidTypeException;
import heap.Tuple;

/**
 * @author laxmikant
 *
 */
public class IESelfJoin2 extends Iterator implements GlobalConst {

	/**
	 * @param args
	 * 
	 * IESelfJoin
	 */
	
	private AttrType  _in1[], _in2[];
	  private  int        in1_len, in2_len;
	  private  Iterator  p_i1,        // pointers to the two iterators. If the
	    p_i2;               // inputs are sorted, then no sorting is done
	  private  TupleOrder  _order;                      // The sorting order.
	  private  CondExpr  OutputFilter[];
	  
	  private  boolean      get_from_in1, get_from_in2;        // state variables for get_next
	  private  int        jc_in1, jc_in2;
	  private  boolean        process_next_block;
	  private  short     inner_str_sizes[];
	  private  IoBuf    io_buf1,  io_buf2;
	  private  Tuple     TempTuple1,  TempTuple2;
	  private  Tuple     tuple1,  tuple2;
	  private  boolean       done;
	  private  byte    _bufs1[][],_bufs2[][];
	  private  int        _n_pages; 
	  private  Heapfile temp_file_fd1, temp_file_fd2;
	  private  AttrType   sortFldType;
	  private  int        t1_size, t2_size;
	  private  Tuple     Jtuple;
	  private  FldSpec   perm_mat[];
	  private  int        nOutFlds;
	  
	
	// Constructor
	  public IESelfJoin2(AttrType    in1[],               
			   int     len_in1,                        
			   short   s1_sizes[],
			   AttrType    in2[],                
			   int     len_in2,                        
			   short   s2_sizes[],
			   
			   int     join_col_in1,                
			   int      sortFld1Len,
			   int     join_col_in2,                
			   int      sortFld2Len,
			   
			   int     amt_of_mem,               
			   Iterator     am1,                
			   Iterator     am2,                
			   
			   boolean     in1_sorted,                
			   boolean     in2_sorted,                
			   TupleOrder order,
			   
			   CondExpr  outFilter[],                
			   FldSpec   proj_list[],
			   int       n_out_flds
			   )
			   {
		  
			   }
	  
	  
	  
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public Tuple get_next() throws IOException, JoinsException, IndexException,
			InvalidTupleSizeException, InvalidTypeException,
			PageNotReadException, TupleUtilsException, PredEvalException,
			SortException, LowMemException, UnknowAttrType,
			UnknownKeyTypeException, Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws IOException, JoinsException, SortException,
			IndexException {
		// TODO Auto-generated method stub
		
	}

}
