/*
 *      Adwin.cpp
 *      
 *      Copyright (C) 2008 Universitat Politècnica de Catalunya
 *      @author Albert Bifet - Ricard Gavaldà (abifet,gavalda@lsi.upc.edu)
 *     
 *      This program is free software; you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation; either version 2 of the License, or
 *      (at your option) any later version.
 *      
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *      
 *      You should have received a copy of the GNU General Public License
 *      along with this program; if not, write to the Free Software
 *      Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 *      MA 02110-1301, USA.
 */

#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include "Adwin.h"

static int bucketSize(int Row)
{
  return (int) pow(2,Row);
}
   
////////////////////////////////////////////////////////////////////////////////

Adwin::Adwin(int M):MINTCLOCK(1),MINLENGTHWINDOW(16),DELTA(.01),MAXBUCKETS(M),bucketList(MAXBUCKETS)
{
  mintTime=0;
  mintClock=MINTCLOCK;
  mdblError=0;
  mdblWidth=0;     
  //Init buckets
  lastBucketRow=0; 
  sum = 0;
  var = 0;
  W = 0;
  bucketNumber=0;
}
 
////////////////////////////////////////////////////////////////////////////////

bool Adwin::update(const double &value)
{
  insertElement(value);
  compressBuckets();  
  return checkDrift();
}

////////////////////////////////////////////////////////////////////////////////

void Adwin::insertElement(const double &value)
{
  //Insert new bucket	
  W++;
  bucketList.head->addBack(value,0.0);	
  bucketNumber++; 

  // update stats
  if (W > 1) {
    var += (W-1) * (value-sum/(W-1)) * (value-sum/(W-1))/W;
  }
  sum+=value;
}

////////////////////////////////////////////////////////////////////////////////  

void Adwin::compressBuckets()
{
  //Traverse the list of buckets in increasing order

  ListNode* cursor = bucketList.head;
  ListNode* nextNode;

  int i=0;//int cont=0;
       		   
  do {//printf(" %d : Contador",cont++);                  
    //Find the number of buckets in a row
    int k=cursor->size;
    //If the row is full, merge buckets
    if(k==MAXBUCKETS+1){
      //  printf(" x%d : Contador",cont++);                
      nextNode=cursor->next;
      if (nextNode==NULL){
	bucketList.addToTail();
	nextNode=cursor->next;
	lastBucketRow++;
      }
      int n1=bucketSize(i); 
      int n2=bucketSize(i);
      double u1=cursor->sum[0]/n1;
      double u2=cursor->sum[1]/n2;
      double incVariance=n1*n2*(u1-u2)*(u1-u2)/(n1+n2);       
      nextNode->addBack(cursor->sum[0]+cursor->sum[1],cursor->variance[0]+cursor->variance[1]+incVariance);
      bucketNumber--;
      cursor->dropFront(2);
      if (nextNode->size<=MAXBUCKETS) break;
    }
    else
      break;
    cursor = cursor->next;
    i++;

  } while (cursor != NULL);
               
} 

////////////////////////////////////////////////////////////////////////////////

void Adwin::deleteElement()
{  
  //Update statistics
  ListNode* Node;
  Node=bucketList.tail;
  int n1=bucketSize(lastBucketRow);
  W-=n1;
  sum-=Node->sum[0];
  double u1=Node->sum[0]/n1;
  double incVariance=Node->variance[0]+n1*W*(u1-sum/W)*(u1-sum/W)/(n1+W);
  var-=incVariance;
  //Delete Bucket
  Node->dropFront();
  bucketNumber--;
  if (Node->size==0){
    bucketList.removeFromTail();
    lastBucketRow--;
  }
} 

////////////////////////////////////////////////////////////////////////////////

bool Adwin::checkDrift() 
{
  bool change = false;
  bool quit = false;
  ListNode* it;
  mintTime++;

  if((mintTime % mintClock==0) && (W > MINLENGTHWINDOW) ){

    bool blnTalla = true;
    
    while (blnTalla) {
      blnTalla = false;
      quit = false;
      int n0 = 0;
      int n1 = W;
      double u0 = 0;
      double u1 = sum;
      it = bucketList.tail;
      int i = lastBucketRow;
      do {    

	for (int k = 0; k < it->size; k++) {

	  if (i==0 && k==it->size-1) { 
	    quit=true;
	    break;
	  }	
   	 	
	  n0 += bucketSize(i);
	  n1 -= bucketSize(i);
	  u0 += it->sum[k];
	  u1 -= it->sum[k];
	  
	  int mintMinWinLength=5;
	  if(n0>=mintMinWinLength && n1>=mintMinWinLength && cutExpression(n0,n1,u0,u1)) { 
	  //if (cutExpression(n0,n1,u0,u1)) { 
	    blnTalla  = true;
	    change = true;

	    if (W>0) {
	      deleteElement();
	      quit=true;
	      break;
	    }
	  }
	}


	it = it->prev;
	i--;
      } while ( !quit && it!= NULL);
    }//End While
  }//End if
			   
  return change;
}


////////////////////////////////////////////////////////////////////////////////

bool Adwin::cutExpression(int N0, int N1, const double &u0, const double &u1)
{ 
  double n0 = double(N0);
  double n1 = double(N1);
  double n  = double(W);
  double diff = u0/n0 - u1/n1;   
  
  double v = var/W;
  double dd = log(2.0 * log(n) / DELTA);     // -- ull perque el ln n va al numerador.

  //per tenir probabilitat < DELTA/(2 ln n).
  /*double m = 1./n0 + 1./n1;
  double eps1 = sqrt(m * .5 * dd);
  double eps2 = 2./3. * m * dd + sqrt(2. * v * m * dd);
  double eps  = (eps1 > eps2 ? eps2 : eps1) ; //min(eps1,eps2);
    */        
  int mintMinWinLength=5;    
  double m= ((double)1/((n0-mintMinWinLength+1)))+ ((double)1/((n1-mintMinWinLength+1)));
  double eps= sqrt(2*m*v*dd)+(double) 2/3 *dd* m;              

  if (fabs(diff) > eps)
    return true; 
  else
    return false;        
}

////////////////////////////////////////////////////////////////////////////////

double Adwin::getEstimation() const
{
  if (W > 0)
    return sum / double(W);
  else
    return 0;   
}    

////////////////////////////////////////////////////////////////////////////////

void Adwin::print() const
{
  ListNode* it;
  it = bucketList.tail;
  if (it==NULL) printf(" It NULL");

  int i=lastBucketRow;
  do {
    for (int k=it->size-1;k>=0;k--) 
      printf(" %d [%1.4f de %d],",i,it->sum[k],bucketSize(i));
    
    printf("\n");
    it = it->prev;i--;
  } while (it != NULL);
}


////////////////////////////////////////////////////////////////////////////////

