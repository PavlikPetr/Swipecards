package com.sonetica.topface.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

/*
 *   Класс для работы со стораджем,файлами,путями...
 */
public class FileSystem {
  //---------------------------------------------------------------------------
  public static String getFileName(String path) {
    return new File(path).getName();
  }
  //---------------------------------------------------------------------------
  public static String getExtension(String fileName) {
    fileName = getFileName(fileName);
    String[] arr = fileName.split(Pattern.quote("."));
    if(arr.length > 1)
      return arr[1];
    return null; 
  }
  //---------------------------------------------------------------------------
  public static String getPath(String path) {
    int n = path.lastIndexOf(File.separator);
    if(n>0)
      return path.substring(0,n);
    return null;
  }
  //---------------------------------------------------------------------------
  public static String removeExtension(String fileName) {
    int n = fileName.lastIndexOf(".");
    if(n>0)
      return fileName.substring(0,n);
    return null;
  }
  //---------------------------------------------------------------------------
  public static long getFileSize(String fileName) {
    File file = new File(fileName);
    if(file.exists())
      return file.length();
    return -1;
  }
  //---------------------------------------------------------------------------
  public static boolean removeFile(String fileName) {
    File file = new File(fileName);
    if(file.exists() && file.isDirectory()) {
      return new File(fileName).delete();
    }
    return false;
  }
  //---------------------------------------------------------------------------
  public static boolean removeDirectory(String path) {
    File dirName = new File(path);
    if(dirName.exists() && dirName.isDirectory()) {
      String[] files = dirName.list();
      for(int i=0; i<files.length; i++) {
        boolean success = removeDirectory(new File(dirName,files[i]).toString());
        if(!success)
          return false;
      }
    }
    return dirName.delete();
  }
  //---------------------------------------------------------------------------
  public static boolean isFileExist(String fileName) {
    return new File(fileName).exists();
  }
  //---------------------------------------------------------------------------
  public static String[] getDirectoryItems(String directory,final String filter) {
    File path = new File(directory);
    if(filter==null)
      return path.list();
    String[] list = path.list(
      new FilenameFilter() {      
        private Pattern pattern = Pattern.compile("[\\p{Print}]+"+"."+filter);
        @Override
        public boolean accept(File dir, String name) {        
          return pattern.matcher(name).matches();
        }
      });
    return list;
  }
  //---------------------------------------------------------------------------
}// FileSystem
