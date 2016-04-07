package tests;

import java.util.*;

import iterator.*;
import heap.*;
import global.*;
import index.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import diskmgr.*;
import bufmgr.*;
import btree.*;
import catalog.*;

public class FileUtils {
  static private boolean OK = true;
  static private boolean FAIL = false;
  
  static boolean status = FAIL;

  public static HeapfileRID createRelationFromFile(String fileName) {
    File file = new File("./"+fileName);
    Heapfile f = null;
    ArrayList<RID> ridList = new ArrayList<RID>();
    AttrType[] relationDataTypes = null;
    short [] stringSizes = null;
    
    try {
        Scanner fileScanner = new Scanner(file);

        String header = fileScanner.nextLine();
        String[] dataTypes = header.split(",");

        relationDataTypes = new AttrType[dataTypes.length];

        int numberOfStringAttributes = 0;
        for(int i = 0 ; i < relationDataTypes.length; i++) {
            switch (dataTypes[i]) {
              case "attrString":
                relationDataTypes[i] = new AttrType(AttrType.attrString); numberOfStringAttributes++; break;
              case "attrInteger":
                relationDataTypes[i] = new AttrType(AttrType.attrInteger); break;
              case "attrReal":
                relationDataTypes[i] = new AttrType(AttrType.attrReal); break;
              case "attrSymbol":
                relationDataTypes[i] = new AttrType(AttrType.attrSymbol); break;
              default:
                relationDataTypes[i] = new AttrType(AttrType.attrNull); break;
            }
        }

        stringSizes = null;
        if(numberOfStringAttributes > 0) {
          stringSizes = new short [1];
          for(int i = 0; i < stringSizes.length; i++) {
            stringSizes[i] = 30;
          }
        }

        Tuple t = new Tuple();

        try {
          t.setHdr((short) relationDataTypes.length, relationDataTypes, stringSizes);
        }
        catch (Exception e) {
          System.err.println("*** error in Tuple.setHdr() ***");
          status = FAIL;
          e.printStackTrace();
        }

        RID rid;
    	int size = t.size();
    	
        boolean status = OK;
        t = new Tuple(size);
        try {
        	t.setHdr((short) relationDataTypes.length, relationDataTypes, stringSizes);
        }
        catch (Exception e) {
          System.err.println("*** error in Tuple.setHdr() ***");
          status = FAIL;
          e.printStackTrace();
        }

    		try {
    			f = new Heapfile(file.getName()+".in");
    		} catch (Exception e) {
    			System.err.println("*** error in Heapfile constructor ***");
    			status = FAIL;
    			e.printStackTrace();
    		}
    	int count = 0;
    	
    	
        while(fileScanner.hasNextLine()) {
          String line = fileScanner.nextLine();
          String[] attributes = line.split(",");
     
          try {
            for(int i = 0; i < attributes.length; i++) {
              switch (dataTypes[i]) {
                case "attrString":
                  t.setStrFld(i+1, attributes[i]); break;
                case "attrInteger":
                  t.setIntFld(i+1, Integer.parseInt(attributes[i])); break;
                case "attrReal":
                  t.setFloFld(i+1, Float.parseFloat(attributes[i])); break;
              }
            }
          } catch (Exception e) {
      				System.err.println("*** Heapfile error in Tuple.setStrFld() ***");
      				status = FAIL;
      				e.printStackTrace();
    			}

          try {
        	  	count++;
      
				rid = f.insertRecord(t.returnTupleByteArray());
				
				ridList.add(rid);
			} catch (Exception e) {
				System.out.println("#Records: " +count);
				System.err.println("*** error in Heapfile.insertRecord() ***");
				status = FAIL;
				e.printStackTrace();
				Runtime.getRuntime().exit(1);
			}
        }

        if (status != OK) {
    			// bail out
    			System.err.println("*** Error creating relation for "+file.getName());
    			Runtime.getRuntime().exit(1);
    		}
    } catch(Exception e) {
      System.out.println("Embarrassing!");
    }
    
    Object[] objArr = ridList.toArray();
    HeapfileRID hfrid = new HeapfileRID(f, Arrays.copyOf(objArr, objArr.length, RID[].class), relationDataTypes, stringSizes);
    
		return hfrid;
  }

  static Query getQueryFromFile(String fileName) {
    // create query - predicates, tables, fields from tables
	Query query = new Query();
	try {
		File file = new File(fileName);
	    Scanner fileScanner = new Scanner(file);
	    
	    
	    String[] projections = fileScanner.nextLine().split(" ");
	
	    String[] relations  = fileScanner.nextLine().split(" ");
	    
	    if(relations.length == 2) {
		    query.outerRelation = relations[0];
		    query.innerRelation = relations[1];
	    } else {
	    	query.outerRelation = query.innerRelation = relations[0];
	    }
	
	    String[] predicates = new String[2];
	    predicates[0] = fileScanner.nextLine();
	    
	    if(fileScanner.hasNext() && fileScanner.nextLine().equals("AND")) {
	      predicates[1] = fileScanner.nextLine();
	    }
	
	    // create projection list
	    query.projectionList = new FldSpec[projections.length];
	    for(int i = 0; i < projections.length; i++) {
	      String[] projection = projections[i].split("_");
	      if(projection[0].equals(query.outerRelation)) {
	        query.projectionList[i] = new FldSpec(new RelSpec(RelSpec.outer), Integer.parseInt(projection[1]));
	      } else if(projection[0].equals(query.innerRelation)) {
	        query.projectionList[i] = new FldSpec(new RelSpec(RelSpec.innerRel), Integer.parseInt(projection[1]));
	      }
	    }
	
	    query.predicates = new CondExpr[2];
	    for(int i = 0; i < predicates.length; i++) {
	      if(predicates[i] != null) {
	        query.predicates[i] = new CondExpr();
	        String[] predicate = predicates[i].split(" ");
	
	        String[] field = predicate[0].split("_");
	        
	        query.predicates[i].next = null;
	        query.predicates[i].type1 = new AttrType(AttrType.attrSymbol);
	        query.predicates[i].type2 = new AttrType(AttrType.attrSymbol);
	        
	        if(!query.innerRelation.equals(query.outerRelation)) {
		        if(field[0].equals(query.outerRelation)) {
		          query.predicates[i].operand1.symbol = new FldSpec(new RelSpec(RelSpec.outer), Integer.parseInt(field[1]));
		        } else if(field[0].equals(query.innerRelation)) {
		          query.predicates[i].operand2.symbol = new FldSpec(new RelSpec(RelSpec.innerRel), Integer.parseInt(field[1]));
		        }
		
		        query.predicates[i].op = new AttrOperator(Integer.parseInt(predicate[1]));
		
		        field = predicate[2].split("_");
		        if(field[0].equals(query.outerRelation)) {
		          query.predicates[i].operand1.symbol = new FldSpec(new RelSpec(RelSpec.outer), Integer.parseInt(field[1]));
		        } else if(field[0].equals(query.innerRelation)) {
		          query.predicates[i].operand2.symbol = new FldSpec(new RelSpec(RelSpec.innerRel), Integer.parseInt(field[1]));
		        }
	      } else {
	    	  	query.predicates[i].operand1.symbol = new FldSpec(new RelSpec(RelSpec.outer), Integer.parseInt(field[1]));
	    	  	
		        query.predicates[i].op = new AttrOperator(Integer.parseInt(predicate[1]));
		        
		        field = predicate[2].split("_");
		        query.predicates[i].operand2.symbol = new FldSpec(new RelSpec(RelSpec.innerRel), Integer.parseInt(field[1]));
		        
	      }
	
	      }
	    }
	} catch(Exception e) {
		e.printStackTrace();
	}

    return query;
  }
}
