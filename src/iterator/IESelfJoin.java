package iterator;
/**
 * 
 */

/**
 * @author laxmikant
 *
 */

import heap.*;
import global.*;
import bufmgr.*;
import diskmgr.*;
import index.*;

import java.lang.*;
import java.util.ArrayList;
import java.io.*;

public class IESelfJoin extends Iterator implements GlobalConst{
	
	private AttrType      _in1[],  _in2[];
	private   int        in1_len, in2_len;
	private  Iterator  L11,L12 , L21,L22;             // Pointers to iterators after sorting
         
	private   Iterator 	left;
	private   Iterator  right;
	 private   short t1_str_sizescopy[];
	private   short t2_str_sizescopy[];
	private   CondExpr LeftFilter[];
	private   CondExpr RightFilter[];
	private   int        n_buf_pgs;        // # of buffer pages available.
	private   boolean        done,         // Is the join complete
	get_from_outer;                 // if TRUE, a tuple is got from outer
	private   Tuple     right_tuple, left_tuple;
	private   Tuple     Jtuple;           // Joined tuple
	private   FldSpec   perm_mat[];
	private   int        nOutFlds;
	private   ArrayList<Integer> p;    // PERMUTATION ARRAY
	private   int[] bit;  // BIT ARRAY
	 private  AttrType   LeftSortFldType;
	 private  AttrType   RightSortFldType;
	 private int bTraverse=0; // 
	private int pIndex=0; 
	private int join_column1;
	private int join_column2;
	private TupleOrder LOrder, ROrder;
	
	private int     sortF1,      sortF2;
	private int amt_memory;
	private int tempIndex=0;
	
	// Compare Tuple
	boolean compareTuple(Tuple t1, Tuple t2) throws FieldNumberOutOfBoundException, IOException
	{
		for(int i=0;i<4;i++)
		{
			if(( t1.getIntFld(0)==t2.getIntFld(0)) 
					&& ( t1.getIntFld(1)==t2.getIntFld(1))
					&& ( t1.getIntFld(2)==t2.getIntFld(2))
					&& ( t1.getIntFld(3)==t2.getIntFld(3))
					)
			{
				return true;
			}
		}
		
		return false;
		
	}
	
	
	/**constructor
	 *Initialize the two relations which are joined, including relation type,
	 *@param in1  Array containing field types of R.
	 *@param len_in1  # of columns in R.
	 *@param t1_str_sizes shows the length of the string fields.
	 *@param in2  Array containing field types of S
	 *@param len_in2  # of columns in S
	 *@param  t2_str_sizes shows the length of the string fields.
	 *@param amt_of_mem  IN PAGES
	 *@param am1  access method for left i/p to join
	 *@param relationName  access hfapfile for right i/p to join
	 *@param outFilter   select expressions
	 *@param rightFilter reference to filter applied on right i/p
	 *@param proj_list shows what input fields go where in the output tuple
	 *@param n_out_flds number of outer relation fileds
	 * @throws Exception 
	 * @throws UnknownKeyTypeException 
	 * @throws UnknowAttrType 
	 * @throws LowMemException 
	 * @throws PredEvalException 
	 * @throws PageNotReadException 
	 * @throws InvalidTypeException 
	 * @throws InvalidTupleSizeException 
	 * @throws IndexException 
	 * @throws JoinsException 
	 *@exception NestedLoopException exception from this class
	 */
	
	
	public  IESelfJoin(
			AttrType    in1[],               
			   int     len_in1,                        
			   short   s1_sizes[],
			   AttrType    in2[],                
			   int     len_in2,                        
			   short   s2_sizes[],  
				int     amt_of_mem,  
				
				int     join_col_in1,                
				   int      sortFld1Len,
				   int     join_col_in2,                
				   int      sortFld2Len,
				
				   TupleOrder LeftOrder, 
				   TupleOrder RightOrder, 
				   
				Iterator     am1,
				Iterator	 am2,
				CondExpr leftFilter[],      
				CondExpr rightFilter[],    
				FldSpec   proj_list[],
				int        n_out_flds
				) throws JoinsException, IndexException, InvalidTupleSizeException, InvalidTypeException, PageNotReadException, PredEvalException, LowMemException, UnknowAttrType, UnknownKeyTypeException, Exception
	{
		
		_in1 = new AttrType[in1.length];
		_in2 = new AttrType[in2.length];
		System.arraycopy(in1,0,_in1,0,in1.length);
		System.arraycopy(in2,0,_in2,0,in2.length);
		in1_len = len_in1;
		in2_len = len_in2;
		
		left = am1;
		right = am2;
		t1_str_sizescopy =  s1_sizes;
		t2_str_sizescopy =  s2_sizes;
		left_tuple = new Tuple();
		right_tuple = new Tuple();
		Jtuple = new Tuple();
		LeftFilter = leftFilter;
		RightFilter  = rightFilter;
		
		n_buf_pgs    = amt_of_mem;
		done  = false;
		get_from_outer = true;
		
		AttrType[] Jtypes = new AttrType[n_out_flds];
		short[]    t_size;

		perm_mat = proj_list;
		nOutFlds = n_out_flds;
		
		join_column1=join_col_in1;
		join_column2=join_col_in2;
		sortF1=sortFld1Len;
		sortF2=sortFld2Len;
		amt_memory=amt_of_mem;
		
		//Sort L1
		 L11 = am1;
	      L21 = am2;
	      
	     // Sort L1 in Given order
		try {
		  L11 = new Sort(in1, (short)len_in1, s1_sizes, am1, join_col_in1,
				  LeftOrder, sortFld1Len, amt_of_mem / 2);
		  L12 = new Sort(in1, (short)len_in1, s1_sizes, am1, join_col_in1,
				  LeftOrder, sortFld1Len, amt_of_mem / 2);
		}catch(Exception e){
		  throw new SortException (e, "Left Sort failed");
		}
		
		// Sort L2 in Given order
		try {
			  L21 = new Sort(in2, (short)len_in2, s2_sizes, am2, join_col_in2,
					  RightOrder, sortFld2Len, amt_of_mem / 2);
			  L22 = new Sort(in2, (short)len_in2, s2_sizes, am2, join_col_in2,
					  RightOrder, sortFld2Len, amt_of_mem / 2);
			}catch(Exception e){
			  throw new SortException (e, "Right Sort failed");
			}
	      
			int right_index=0;
			Iterator 	tempLeft = L11 ;
		    Iterator  tempRight= L21;
		while((right_tuple = tempRight.get_next())!=null)
		{
			RightSortFldType=_in2[join_col_in2 - 1];
			
			// get the value of from LEFT tuple
			
			
			
		
		
		/// Computation of Permutation array
			tempRight = L21;
			right_index=0;
	      while ((left_tuple = tempLeft.get_next()) != null)
		 {
	    	  
	    	  LeftSortFldType=_in1[join_col_in1-1];
	    	  
	    	  if(compareTuple(left_tuple, right_tuple)==true)
	    	  {
	    		  p.add(right_index+1);
	    	  }
	    	  
	    	  
	    	  right_index++;
		}
		}
	      
		
		//inititialize bit array to 0.
		bit= new int[p.size()];
		//Setting up the final output Tuple size 
		try {
			
			t_size = TupleUtils.setup_op_tuple(Jtuple, Jtypes,
					in1, len_in1, in2, len_in2,
					s1_sizes, s2_sizes,
					proj_list, nOutFlds);
		}
		catch (TupleUtilsException e)
		{
			System.out.println();
		}
		
		
		
		
	} 	
	
	public Tuple getRightTuple(int index) throws JoinsException, IndexException, InvalidTupleSizeException, InvalidTypeException, PageNotReadException, TupleUtilsException, PredEvalException, LowMemException, UnknowAttrType, UnknownKeyTypeException, Exception
	{
		Tuple tempTuple=null;
		
		
	//	 Iterator tempL1 = new Sort(_in1, (short)in1_len, t1_str_sizescopy, left, join_column1,
			//	  LOrder, sortF1, amt_memory / 2);
		while(tempIndex<=index)
		{
			tempTuple=L12.get_next();
			tempIndex++;
		}
		return tempTuple;
		
		
	}
	
	public Tuple getLeftTuple(int index) throws JoinsException, IndexException, InvalidTupleSizeException, InvalidTypeException, PageNotReadException, TupleUtilsException, PredEvalException, LowMemException, UnknowAttrType, UnknownKeyTypeException, Exception
	{
		Tuple tempTuple=null;
		
		 Iterator tempL1 = new Sort(_in1, (short)in1_len, t1_str_sizescopy, left, join_column1,
				  LOrder, sortF1, amt_memory / 2);
		
		 while(index>=0)
		{
			tempTuple=tempL1.get_next();
			index--;
		}
		return tempTuple;
		
		
	}
	
	
	@Override
	public Tuple get_next() 
			
			throws IOException, JoinsException, IndexException, InvalidTupleSizeException,
			InvalidTypeException, PageNotReadException, TupleUtilsException, PredEvalException, SortException,
			LowMemException, UnknowAttrType, UnknownKeyTypeException, Exception 
	{
		// TODO Auto-generated method stub
		
		int right_index=0;
		
		while(pIndex<p.size())
		{
			if(bit[p.get(pIndex)-1]==0)
			{
				bit[p.get(pIndex)-1]=1;
				bTraverse=p.get(pIndex);
			}
			
			while(bTraverse<p.size())
			{
				if(bit[bTraverse]==1)
				{
					Tuple leftTuple= getLeftTuple(p.get(pIndex)-1);
					Tuple rightTuple=getRightTuple(bTraverse);
					
					
					Projection.Join(leftTuple, _in1, 
						      rightTuple, _in2, 
						      Jtuple, perm_mat, nOutFlds);
					return Jtuple;	      
				}
			}
		}
      
		
		
		return null;
	}
	@Override
	public void close() throws IOException, JoinsException, SortException, IndexException {
		// TODO Auto-generated method stub
		
	}

}
