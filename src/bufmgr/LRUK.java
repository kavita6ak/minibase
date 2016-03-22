/**
 * 
 */
package bufmgr;

import java.util.HashMap;

/**
 * @author laxmikant
 *
 */
public class LRUK extends Replacer {

	/**
	 * private field An array to hold number of frames in the buffer pool
	 */

	private int frames[];

	/**
	 * private field number of frames used
	 */
	private int nframes;

	/**
	 * 
	 * private field HashMap to maintain Last K references of the frames   
	 * 
	 */
	private HashMap<Integer, long[]> HIST = new HashMap<Integer, long[]>();

	/**
	 *
	 * private field LAST which gives last reference of the all the frames in the buffer pool
	 * 
	 */
	private HashMap<Integer, Long> LAST = new HashMap<Integer, Long>();

	/**
	 * field CORRELATD_REFERENCE_PERIOD
	 * initially set to zero
	 */
	public long CORRELATD_REFERENCE_PERIOD = 0;

	/**
	 * 
	 *  
	 * 
	 */
	int K;

	/**
	 * This method updates position of the recently referenced frame in buffer pool.
	 * If the page is referenced, it shifts the frame reference ith timestamp to i+1. 
	 * @param frameNo
	 *            the frame number
	 */
	private void update(int frameNo) {

		long t = System.currentTimeMillis();
		int frame;
		boolean pageFound=false;
		try{
		Thread.sleep(10);
		}
		catch(Exception e){
			
		}
		int numBuffers = mgr.getNumBuffers();
		for (int i = 0; i < numBuffers; i++) 
		{
			frame = frames[i];

			if (frame == frameNo)  // if frame already in BUFFER POOL
			{
				pageFound=true;
			}

		}


		if(pageFound==true)

		{
			//System.out.println("FRAME FOUND: "+frame);
			if (HIST.containsKey(frameNo)) 
			{  // unneccessary

				long[] history = HIST.get(frameNo);
				if (t - LAST.get(frameNo) > CORRELATD_REFERENCE_PERIOD) 
				{
					long CORRELATD_REF_OF_REFD_PAGE = LAST.get(frameNo) - history[0];

					for (int j = K-1; j >=1; j--) 
					{
						history[j] = history[j - 1] + CORRELATD_REF_OF_REFD_PAGE;
					}

					history[0] = t;
					HIST.put(frameNo, history);
					LAST.put(frameNo, t);
				} 
				else 
				{
					LAST.put(frameNo, t);
				}

			}
			else
			{
				long [] history = new long[K];
				for(int j=0;j<K;j++)
				{
					history[j]=0L;
				}
				history[0]=t;
				HIST.put(frameNo, history);
				LAST.put(frameNo, t);
			}
		} 
		else   // if frameNo not in BUFFER POOL  /// FOUND==FALSE
		{

			if (!HIST.containsKey(frameNo)) // if frameNo NOT is in HIST
			{
				System.out.println("Frame not in HIST : " + frameNo );

				long[] history = new long[K];
				for(int j=0;j<K;j++)
				{
					history[j]=0L;
				}
				history[0] = t;
				HIST.put(frameNo, history);
				LAST.put(frameNo, t);
			} 
			else // if frameNo is in HIST
			{

				long[] history = HIST.get(frameNo);
				//	System.out.println("LAX  "+HIST.containsKey(frameNo) + history[K-1]);

				for (int j = K-1; j>= 1; j--) 
				{
					//System.out.println("j "+j);
					history[j] = history[j - 1];

				}

			}



		}

	}


	/**
	 * Calling super class the same method Initializing the frames[] with number
	 * of buffer allocated by buffer manager set number of frame used to -1
	 *
	 * @param mgr
	 *            a BufMgr object
	 * @see BufMgr
	 * @see Replacer
	 */
	public void setBufferManager(BufMgr mgr) {
		super.setBufferManager(mgr);
		frames = new int[mgr.getNumBuffers()];

		for(int i=0;i<mgr.getNumBuffers();i++)
		{
			frames[i]=-1;
		}
		nframes = 0;
	}

	/* public methods */

	/**
	 * Class constructor Initializing frames[] pinter = null.
	 * SET K=lastRef
	 */
	public LRUK(BufMgr mgrArg, int lastRef) {
		super(mgrArg);
		K=lastRef;
		frames = null;
	}

	/**
	 * call super class the same method pin the page in the given frame number
	 * move the page to the end of list
	 *
	 * @param frameNo
	 *            the frame number to pin
	 * @exception InvalidFrameNumberException
	 */
	public void pin(int frameNo) throws InvalidFrameNumberException {
		super.pin(frameNo);

		update(frameNo);

	}


	/**
	 * 
	 * Get the timestamp of the reference backward from K  which is non Zero.
	 * move the page to the end of list
	 *
	 * @param framNo
	 *            the frame number for which to find the non zero reference
	 * 
	 * 
	 */

	public int findNonZeroRef(int frame)
	{
		long[] history=HIST.get(frame);
		int f=0;
		for(int i=K-1;i>=0;i--)
		{
			if(history[i]>0)
			{
				f=i;
				break;
			}
		}
		return f;

	}

	/**
	 * 
	 * Returns the total frames in the buffer pool.
	 *
	 * @param 
	 *            The frames in the buffer pool
	 * 
	 * 
	 */
	public int[] getFrames()
	{
		return frames;
	}

	/**
	 * Finding a free frame in the buffer pool or choosing a page to replace
	 * using LRUK policy
	 *
	 * @return return the frame number 
	 * 
	 *     Throws the exception BufferPoolExceededException
	 */

	public int pick_victim() throws BufferPoolExceededException, PagePinnedException 
	{
		int numBuffers = mgr.getNumBuffers();
		int frame;
		long t = System.currentTimeMillis();
		long min = t;
		int victim = -1;



		if (nframes < numBuffers)   // if there are free frames in BUFFER POOL
		{

			frame = nframes++;
			frames[frame] = frame;
			state_bit[frame].state = Pinned;
			(mgr.frameTable())[frame].pin();
			//System.out.println(" There are free frames :" + frame );
			update(frame);
			return frame;
		}

		else  // if there are NO free frames in BUFFER POOL
		{

			for (int i = 0; i < numBuffers; i++) 
			{

				frame = frames[i];

				if (state_bit[frame].state != Pinned ) 
				{
					if(HIST.containsKey(frame))
					{
						if(t - LAST.get(frame) > CORRELATD_REFERENCE_PERIOD)
						{
							long[] history=HIST.get(frame);
							if(history!=null)
							{

								if( history[K-1]== 0) // find a reference which is more than 0
								{
									int refToReplace =  findNonZeroRef(frame);
									if(history[refToReplace] < min)
									{
										victim = frame;
										min = HIST.get(frame)[K-1];
									}

								}
								else   // check if 
								{
									if( history[K-1] < min )
									{
									victim = frame;
									//System.out.println(" One of the victim :" + victim );
									min = HIST.get(frame)[K-1];
								
									}
								}

							}


						}

					}


				}
			}


		}
		if (victim != -1)
		{
			
			
			state_bit[victim].state = Pinned;
			(mgr.frameTable())[victim].pin();
			//System.out.println("vitim " + victim); 	
			update(victim);
			return victim;
			
		}


		/*
		 * for ( int i = numBuffers-1; i>=0; --i ) { frame = frames[i]; if (
		 * state_bit[frame].state != Pinned ) { state_bit[frame].state = Pinned;
		 * (mgr.frameTable())[frame].pin(); update(frame); return frame; } }
		 */
		throw new BufferPoolExceededException(null, "BUFFER: BUFFER_EXCEEDED");
	}
	
	/**
	 * Find the Kth reference of the page
	 *
	 * @param pagenumber , reference K 
	 * @return return Kth reference of the page 
	 * 
	 * 
	 */
	public long back(int pagenumber,int k)
	{
		return LAST.get(pagenumber);
	}

	/**
	 * Get the Kth reference of the page from the History of the page
	 *
	 * @param pagenumber , reference K 
	 * @return return timestamp of Kth reference of the page 
	 * 
	 * 
	 */
	public long HIST(int pagenumber,int k)
	{
		if(HIST.get(pagenumber)[k]>0)
			return HIST.get(pagenumber)[k];

		long[] history=HIST.get(pagenumber);

		for(int i=k-1;i>=0;i--)
		{
			if(history[i]>0)
				return history[i];
		}
		return -1;		
	}

	/**
	 * get the page replacement policy name
	 *
	 * @return return the name of replacement policy used
	 */
	public String name() {
		return "LRUK";
	}

	/**
	 * print out the information of frame usage
	 */
	public void info() {
		super.info();

		System.out.print("LRUK REPLACEMENT");

		for (int i = 0; i < nframes; i++) {
			if (i % 5 == 0)
				System.out.println();
			System.out.print("\t" + frames[i]);

		}
		System.out.println();
	}

}
