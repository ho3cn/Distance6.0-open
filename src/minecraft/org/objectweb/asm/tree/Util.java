package org.objectweb.asm.tree;

import java.util.ArrayList;
import java.util.List;

final class Util {
  static <T> List<T> asArrayList(int length) {
    List<T> list = new ArrayList<>(length);
    for (int i = 0; i < length; i++)
      list.add(null); 
    return list;
  }
  
  static <T> List<T> asArrayList(T[] array) {
    if (array == null)
      return new ArrayList(); 
    ArrayList<T> list = new ArrayList<>(array.length);
    for (T t : array)
      list.add(t); 
    return list;
  }
  
  static List<Byte> asArrayList(byte[] byteArray) {
    if (byteArray == null)
      return new ArrayList(); 
    ArrayList<Byte> byteList = new ArrayList<>(byteArray.length);
    for (byte b : byteArray)
      byteList.add(b);
    return byteList;
  }
  
  static List<Boolean> asArrayList(boolean[] booleanArray) {
    if (booleanArray == null)
      return new ArrayList(); 
    ArrayList<Boolean> booleanList = new ArrayList<>(booleanArray.length);
    for (boolean b : booleanArray)
      booleanList.add(b);
    return booleanList;
  }
  
  static List<Short> asArrayList(short[] shortArray) {
    if (shortArray == null)
      return new ArrayList(); 
    ArrayList<Short> shortList = new ArrayList<>(shortArray.length);
    for (short s : shortArray)
      shortList.add(s);
    return shortList;
  }
  
  static List<Character> asArrayList(char[] charArray) {
    if (charArray == null)
      return new ArrayList(); 
    ArrayList<Character> charList = new ArrayList<>(charArray.length);
    for (char c : charArray)
      charList.add(c);
    return charList;
  }
  
  static List<Integer> asArrayList(int[] intArray) {
    if (intArray == null)
      return new ArrayList(); 
    ArrayList<Integer> intList = new ArrayList<>(intArray.length);
    for (int i : intArray)
      intList.add(i);
    return intList;
  }
  
  static List<Float> asArrayList(float[] floatArray) {
    if (floatArray == null)
      return new ArrayList(); 
    ArrayList<Float> floatList = new ArrayList<>(floatArray.length);
    for (float f : floatArray)
      floatList.add(f);
    return floatList;
  }
  
  static List<Long> asArrayList(long[] longArray) {
    if (longArray == null)
      return new ArrayList(); 
    ArrayList<Long> longList = new ArrayList<>(longArray.length);
    for (long l : longArray)
      longList.add(l);
    return longList;
  }
  
  static List<Double> asArrayList(double[] doubleArray) {
    if (doubleArray == null)
      return new ArrayList(); 
    ArrayList<Double> doubleList = new ArrayList<>(doubleArray.length);
    for (double d : doubleArray)
      doubleList.add(d);
    return doubleList;
  }
  
  static <T> List<T> asArrayList(int length, T[] array) {
    List<T> list = new ArrayList<>(length);
    for (int i = 0; i < length; i++)
      list.add(array[i]); 
    return list;
  }
}
