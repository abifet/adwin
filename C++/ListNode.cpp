/*
 *      ListNode.cpp
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
#include "ListNode.h"

using namespace std;

////////////////////////////////////////////////////////////////////////////////

ListNode::ListNode(int _M)
  :M(_M),
   size(0),
   sum(M+1,0.0),
   variance(M+1,0.0),
   next(NULL),
   prev(NULL)
{}

////////////////////////////////////////////////////////////////////////////////

void ListNode::addBack(const double &value, const double &var)
{
  // insert a Bucket at the end
  sum[size] = value;
  variance[size] = var;
  size++;
}

//////////////////////////////////////////////////////////////////////////////// 

void ListNode::dropFront(int n)
{
  // drop first n elements
  for (int k = n; k <= M;k++) {    
    sum[k-n] = sum[k];
    variance[k-n] = variance[k];
  }
  
  for (int k = 1; k <= n; k++) {
    sum[M - k + 1] = 0;
    variance[M - k + 1] = 0;
  }
  
  size-=n;

}
