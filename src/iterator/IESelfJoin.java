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
	private  Iterator  p_i1,  p_i2;             // Pointers to iterators after sorting
         
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
	
	/*
	 * 
	 *  int     len_in1,                        
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
		
		
		
		//Sort L1
		 p_i1 = am1;
	      p_i2 = am2;
	      
	     // Sort L1 in Given order
		try {
		  p_i1 = new Sort(in1, (short)len_in1, s1_sizes, am1, join_col_in1,
				  LeftOrder, sortFld1Len, amt_of_mem / 2);
		}catch(Exception e){
		  throw new SortException (e, "Left Sort failed");
		}
		
		// Sort L2 in Given order
		try {
			  p_i2 = new Sort(in2, (short)len_in2, s2_sizes, am2, join_col_in2,
					  RightOrder, sortFld2Len, amt_of_mem / 2);
			}catch(Exception e){
			  throw new SortException (e, "Right Sort failed");
			}
	      
			int right_index=0;
			Iterator 	tempLeft = p_i1 ;
		    Iterator  tempRight= p_i2;
		while((left_tuple = tempLeft.get_next())!=null)
		{
			LeftSortFldType=_in1[join_col_in1-1];
			
			// get the value of from left tuple
			
			
			
		
		
		/// Computation of Permutation array
			tempRight = p_i2;
			right_index=0;
	      while ((right_tuple = tempRight.get_next()) != null)
		 {
	    	  
	    	  RightSortFldType=_in2[join_col_in2-1];
	    	  
	    	  switch (RightSortFldType.attrType)
				{
				case AttrType.attrInteger:
						int left_value=left_tuple.getIntFld(join_col_in1-1);
						int right_value=right_tuple.getIntFld(join_col_in2-1);
						if(left_value==right_value)
						{
							p.add(right_index+1);
						}
				  break;
				case AttrType.attrReal:
						double left_value_real=left_tuple.getFloFld(join_col_in1-1);
						double right_value_real=right_tuple.getFloFld(join_col_in2-1);
						if(left_value_real==right_value_real)
						{
							p.add(right_index+1);
						}
				  break;
				case AttrType.attrString:
						String left_value_string=left_tuple.getStrFld(join_col_in1-1);
						String right_value_string=right_tuple.getStrFld(join_col_in2-1);
						if(left_value_string.equals(right_value_string))
						{
							p.add(right_index+1);
						}
				  break;
				
				}
		 
	    	  
	    	  right_index++;
		}
		}
	      
		
		
		
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
			bit[p.get(pIndex)-1]=1;   //set bit array to 1
			while(bTraverse<p.size())
			{
				if(bit[bTraverse]==1)
				{
					return 
				}
				
				// Now get next JOIN tuple with checking "1" right to  bit[p.get(pIndex)-1]
				if
			}
		}
      
		
		
		return null;
	}
	@Override
	public void close() throws IOException, JoinsException, SortException, IndexException {
		// TODO Auto-generated method stub
		
	}

}
