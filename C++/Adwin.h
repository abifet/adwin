/*
 *      Adwin.h
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
#include "List.h"

class Adwin  {

 public:
  
  Adwin(int _M);

  double getEstimation() const; 
  bool update(const double &value);
  void print() const;
  int length() const { return W; }

 private:

  void insertElement(const double &value);  
  void compressBuckets();  
  bool checkDrift();

  void deleteElement();  

  bool cutExpression(int n0,
		     int n1,
		     const double &u0,
		     const double &u1);


 private:

  const int MINTCLOCK;
  const int MINLENGTHWINDOW;
  const double DELTA;
  const int MAXBUCKETS;

 private:
  
  int mintTime;
  int mintClock;
  double mdblError;
  double mdblWidth;

  //BUCKET

  int bucketNumber;
  List bucketList;

  int W; // Width

  int lastBucketRow; 

  double sum; // running sum
  double var; // running variance


};





