package tests;

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

public class Query {
  CondExpr[] predicates;

  String outerRelation;
  String innerRelation;

  FldSpec[] projectionList;

  public String toString() {
    String result = "";
    for(int i = 0; i < projectionList.length; i++) {
      result += "Project value relation: "+projectionList[i].relation.key
              + " Project value offset: "+projectionList[i].offset+"\n";
    }

    for(int i = 0; i < predicates.length; i++) {
    	if(predicates[i] != null) {
    		result += "Operator: "+predicates[i].op.attrOperator+"\n"
    				+ "Type1: "+predicates[i].type1.attrType+"\n"
    				+ "Type2: "+predicates[i].type2.attrType+"\n"
    				+ "Operand1 offset: "+predicates[i].operand1.symbol.offset+"\n"
    				+ "Operand1 relation: "+predicates[i].operand1.symbol.relation.key+"\n"
    				+ "Operand2 offset: "+predicates[i].operand2.symbol.offset+"\n"
    				+ "Operand2 relation: "+predicates[i].operand2.symbol.relation.key+"\n";
    	}
    }

    return "Outer Relation: "+outerRelation + "\n"
            + "Inner Relation: "+innerRelation + "\n"
            + result;
  }
}
