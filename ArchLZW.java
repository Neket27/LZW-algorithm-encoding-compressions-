import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class ArchLZW {

   public static void main(String[] args) throws IOException {
      Scanner sc = new Scanner(System.in);
      System.out.print("Название исходного файла: ");
//      String pathIn = sc.nextLine();
      String pathIn ="/home/neket/Рабочий стол/Variant8.txt";
//      String pathIn="/home/neket/Рабочий стол/Variant8out.txt";
      System.out.print("Название выходного файла: ");
//      String pathOut = sc.nextLine();
      String pathOut ="/home/neket/Рабочий стол/Variant8out.txt";
//      String pathOut ="/home/neket/Рабочий стол/Variant8Q";
      System.out.print("Режим работы [1 - Архивация, 2 - Дезархивация]\n> ");
      int mode = sc.nextInt();
      File fileIn = new File(pathIn);
      File fileOut = new File(pathOut);
      if(!fileIn.exists()) {
         System.out.println("Файл '" + pathIn + "' не существует.");
      } else {
         System.out.println("{-Размер блока DINAMIC-}\n");
         long time;
         RandomAccessFile in;
         RandomAccessFile out;
         if(mode == 1) {
            time = System.currentTimeMillis();
            in = new RandomAccessFile(fileIn.getAbsolutePath(), "r");
            out = new RandomAccessFile(fileOut.getAbsolutePath() + ".txt", "rw");
            compressFile(in, out);
            in.close();
            out.close();
            System.out.println("Архивация прошла за: ~" + (System.currentTimeMillis() - time) / 1000L + " с.\n");
         } else if(mode == 2) {
            time = System.currentTimeMillis();
            in = new RandomAccessFile(fileIn.getAbsolutePath(), "r");
            out = new RandomAccessFile(fileOut.getAbsolutePath(), "rw");
            decompressFile(in, out);
            in.close();
            out.close();
            System.out.println("Дезархивация прошла за: ~" + (System.currentTimeMillis() - time) / 1000L + " с.");
         }

      }
   }

   public static void compressFile(RandomAccessFile in, RandomAccessFile out) throws IOException {
      System.out.println("[-Архивация началась-] + [-Размер исходного файла " + in.length() + " байт-]");
      long time = System.currentTimeMillis();
      StringBuilder sb = new StringBuilder();
      String lineBuffer;

      while((lineBuffer = in.readLine()) != null) {
         sb.append(lineBuffer).append("\n");
      }

      sb.delete(sb.length() - 1, sb.length());
      System.out.println("> Считано " + sb.length() + " символов и занесено в буфер. (t: " + (System.currentTimeMillis() - time) + " мс)");
      time = System.currentTimeMillis();
      List<Integer> compressed = compress(sb.toString());

       for (Integer com : compressed) {
           out.writeInt(com);
       }

      System.out.println("> Сброшено в файл " + compressed.size() * 4 + " байт. (t: " + (System.currentTimeMillis() - time) + " мс)");
   }

   public static void decompressFile(RandomAccessFile in, RandomAccessFile out) throws IOException {
      System.out.println("[-Дезархивация началась-] + [-Размер исходного файла " + in.length() + " байт-]");
      long time = System.currentTimeMillis();
      ArrayList<Integer> buffer = new ArrayList<>();

      while(in.getFilePointer() <= in.length()) {
         try {
            buffer.add(in.readInt());
         } catch (EOFException var6) {
            break;
         }
      }

      System.out.println("> Считано " + buffer.size() + " символов и занесено в буфер. (t: " + (System.currentTimeMillis() - time) + " мс)");
      time = System.currentTimeMillis();
      String decompressed = decompress(buffer);
      out.writeBytes(decompressed);
      System.out.println("> Сброшено в файл " + decompressed.length() + " символов. (t: " + (System.currentTimeMillis() - time) + " мс)");
   }

   private static List<Integer> compress(String uncompressed) {
      int dictSize = 256;
      HashMap<String,Integer> dictionary = new HashMap<>();

      for(int buffer = 0; buffer < 256; ++buffer) {
         dictionary.put(String.valueOf((char)buffer), buffer);
      }

      String var10 = "";
      ArrayList<Integer> result = new ArrayList<>();
      char[] var8;
      int var7 = (var8 = uncompressed.toCharArray()).length;

      for(int var6 = 0; var6 < var7; ++var6) {
         char symbol = var8[var6];
         StringBuilder word = new StringBuilder();
         word.append(var10).append(symbol);
         if(!dictionary.containsKey(word.toString())) {
            result.add(dictionary.get(var10));
            dictionary.put(word.toString(), dictSize++);
            var10 = String.valueOf(symbol);
         } else {
            var10 = word.toString();
         }
      }

      if(!var10.equals("")) {
         result.add(dictionary.get(var10));
      }

      return result;
   }

   private static String decompress(List<Integer> compressed) {
      ArrayList<String> dict = new ArrayList<>();

      for(int word = 0; word < 256; ++word) {
         dict.add(String.valueOf((char)word));
      }

      String var7 = String.valueOf((char)(compressed.remove(0)).intValue());
      StringBuilder result = new StringBuilder(var7);

      String entry;
      for(Iterator var5 = compressed.iterator(); var5.hasNext(); var7 = entry) {
         int code = ((Integer)var5.next()).intValue();
         if(code < dict.size()) {
            entry = dict.get(code);
         } else {
            if(code != dict.size()) {
               throw new IllegalArgumentException("Ошибка при считывание символов, позиция: " + code);
            }

            entry = var7.concat(String.valueOf(var7.charAt(0)));
         }

         result.append(entry);
         dict.add(var7.concat(String.valueOf(entry.charAt(0))));
      }

      return result.toString();
   }
}
