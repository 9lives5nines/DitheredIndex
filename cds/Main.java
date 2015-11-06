import java.util.List;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.SortedSet;
import java.util.Iterator;
import java.util.Scanner;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

import javax.imageio.*;
import java.io.*;

import java.text.NumberFormat;
//
//Written by: Diana Arrieta
//Purpose: Project 2013
//
/////////////////////////////////////////////////////////////////////
class Compressor
{
   int W, H, MAXVAL;
   String P;
   RGB [][] oArray, dArray, cArray;
   RGB [] palette;
   int [] common;
   double [][] bMatrix;
   String colors;
//-------------------------------------------------------------------
   public Compressor (String filein) throws Exception
   {
      FileInputStream fi = new FileInputStream(new File(filein));
      for (int i = 0; i < 3; i++)
      {
         String s = "";
         for (int c; (c = fi.read()) != 10;)
         {
            s = s+(char)c;
         }
         if ( i == 0 )
         {
            if ( s.equals("P6")) P = s;
         }
         if ( i == 1 )
         {
            //Strip comment lines
            if ( s.charAt(0) == '#' ) i--;
         }
         if ( i == 1 )
         {
            String [] split = s.split(" ");
            W = Integer.parseInt(split[0]);
            H = Integer.parseInt(split[1]);
         }
         if ( i == 2 ) MAXVAL = Integer.parseInt(s);
      }

      oArray = new RGB [W] [H];
      dArray = new RGB [W] [H];
      cArray = new RGB [W] [H];
      for (int y = 0; y < H; y++)
      {
         for (int x = 0; x < W; x++)
         {
            oArray[x][y] = new RGB(fi.read(), fi.read(), fi.read());
            dArray[x][y] = new RGB(oArray[x][y].r,
                                   oArray[x][y].g,
                                   oArray[x][y].b);
         }
      }
      fi.close();
   }
//-------------------------------------------------------------------
   public void SetPalette ( String s )
   {
      this.colors = s;
      //Set palette
      switch (s)
      {
         case "2-Colors":
            this.palette = new RGB[]
            {
               new RGB(0,0,0),
               new RGB(255,255,255)
            };
            this.common = new int [palette.length];
            break;
         case "8-Colors":
            //Easy Palette
            this.palette = new RGB[]
            {
               new RGB(  0,   0,   0),
               new RGB(  0,   0, 255),
               new RGB(  0, 255,   0),
               new RGB(  0, 255, 255),
               new RGB(255,   0,   0),
               new RGB(255,   0, 255),
               new RGB(255, 255,   0),
               new RGB(255, 255, 255)
            };
            this.common = new int [palette.length];
            break;
         case "16-Colors":
            //Arbitrary Palette
            this.palette = new RGB[]
            {
               new RGB (0x080000), new RGB (0x201A0B),
               new RGB (0x2B347C), new RGB (0x2B7409),
               new RGB (0x234309), new RGB (0x432817),
               new RGB (0x492910), new RGB (0x5D4F1E),
               new RGB (0x6A94AB), new RGB (0x9C6B20),
               new RGB (0xA9220F), new RGB (0xD0CA40),
               new RGB (0xD5C4B3), new RGB (0xE8A077),
               new RGB (0xFCE76E), new RGB (0xFCFAE2)
            };
            this.common = new int [palette.length];
            break;
         case "216-Colors":
            //Web Safe Colors
            this.palette = new RGB [216];
            palette[0] = new RGB (0,0,0);
            for ( int n = 1; n < 216; n++ )
            {
               palette[n] = new RGB (palette[n-1].r,palette[n-1].g, palette[n-1].b);
               palette[n].b = palette[n-1].b + 51;
               if ( palette[n].b > 255 ) { palette[n].b = 0; palette[n].g += 51;}
               if ( palette[n].g > 255 ) { palette[n].g = 0; palette[n].r += 51;}
            }
            this.common = new int [16];
            break;

         default: System.out.println(s);
      }
   }
//-------------------------------------------------------------------
   public void Ordered_Dither ( RGB [][] in)
   {
      dArray = new RGB [in.length][in[0].length];

      bMatrix = new double [][]
      // {
         // { 1,  9,  3, 11},
         // {13,  5, 15,  7},
         // { 4, 12,  2, 10},
         // {16,  8, 14,  6},
      // };

      {
         {  1, 49, 13, 61,  4, 52, 16, 64},
         { 33, 17, 45, 29, 36, 20, 48, 32},
         {  9, 57,  5, 53, 12, 60,  8, 56},
         { 41, 25, 37, 21, 44, 28, 40, 24},
         {  3, 51, 15, 63,  2, 50, 14, 62},
         { 35, 19, 47, 31, 34, 18, 46, 30},
         { 11, 59,  7, 55, 10, 58,  6, 54},
         { 43, 27, 39, 23, 42, 26, 38, 22},
      };

      for ( int y = 0; y < H; y++ )
      {
         for ( int x = 0; x < W; x++ )
         {
            //RGB oldpixel = in[x][y].add(bMatrix[x % 4][y % 4]);
            RGB oldpixel = in[x][y].add(bMatrix[y % 8][x % 8]);
            RGB newpixel = find_closest(oldpixel);
            dArray[x][y] = newpixel;
         }
      }
   }
//-------------------------------------------------------------------
   public void Floyd_Dither ( RGB [][] in )
   {
      //      X   7
      //  3   5   1
     
      dArray = new RGB [in.length][in[0].length];

      for (int y = 0; y < H; y++)
      {
         for (int x = 0; x < W; x++)
         {
            dArray[x][y] = new RGB (in[x][y].r,in[x][y].g,in[x][y].b);
         }
      }

      for ( int y = 0; y < H; y++ )
      {
         for ( int x = 0; x < W; x++ )
         {
            RGB oldpixel = dArray[x][y];
            RGB newpixel = find_closest(oldpixel);
            dArray[x][y] = newpixel;
            RGB error = oldpixel.sub(newpixel);
            if ( x < W-1 ) dArray[x+1][y]   = dArray[x+1][y]  .add(error.mul(7./16));
            if ( x > 1
              && y < H-1 ) dArray[x-1][y+1] = dArray[x-1][y+1].add(error.mul(3./16));
            if ( y < H-1 ) dArray[x  ][y+1] = dArray[x  ][y+1].add(error.mul(5./16));
            if ( x < W-1
              && y < H-1 ) dArray[x+1][y+1] = dArray[x+1][y+1].add(error.mul(1./16));
         }
      }
   }
//-------------------------------------------------------------------
   public void Jarvis_Dither( RGB [][] in )
   {
      //         X   7   5 
      // 3   5   7   5   3
      // 1   3   5   3   1
   
      dArray = new RGB [in.length][in[0].length];

      for (int y = 0; y < H; y++)
      {
         for (int x = 0; x < W; x++)
         {
            dArray[x][y] = new RGB (in[x][y].r,in[x][y].g,in[x][y].b);
         }
      }

      for ( int y = 0; y < H; y++ )
      {
         for ( int x = 0; x < W; x++ )
         {
            RGB oldpixel = dArray[x][y];
            RGB newpixel = find_closest(oldpixel);
            dArray[x][y] = newpixel;
            RGB error = oldpixel.sub(newpixel);

            if ( x < W-1 ) dArray[x+1][y]   = dArray[x+1][y]  .add(error.mul(7./48));
            if ( x < W-2 ) dArray[x+2][y]   = dArray[x+2][y]  .add(error.mul(5./48));

            if ( x > 2
              && y < H-1 ) dArray[x-2][y+1] = dArray[x-2][y+1].add(error.mul(3./48));
            if ( x > 1
              && y < H-1 ) dArray[x-1][y+1] = dArray[x-1][y+1].add(error.mul(5./48));
            if ( y < H-1 ) dArray[x  ][y+1] = dArray[x  ][y+1].add(error.mul(7./48));
            if ( x < W-1
              && y < H-1 ) dArray[x+1][y+1] = dArray[x+1][y+1].add(error.mul(5./48));
            if ( x < W-2
              && y < H-1 ) dArray[x+2][y+1] = dArray[x+2][y+1].add(error.mul(3./48));

            if ( x > 2
              && y < H-2 ) dArray[x-2][y+2] = dArray[x-2][y+2].add(error.mul(1./48));
            if ( x > 1
              && y < H-2 ) dArray[x-1][y+2] = dArray[x-1][y+2].add(error.mul(3./48));
            if ( y < H-2 ) dArray[x  ][y+2] = dArray[x  ][y+2].add(error.mul(5./48));
            if ( x < W-1
              && y < H-2 ) dArray[x+1][y+2] = dArray[x+1][y+2].add(error.mul(3./48));
            if ( x < W-2
              && y < H-2 ) dArray[x+2][y+2] = dArray[x+2][y+2].add(error.mul(1./48));
         }
      }
   }
//-------------------------------------------------------------------
   public void Relevance_Dither ( RGB [][] in )
   {
   
      //      X   4  1
      //      4   2
      //      1
      
      dArray = new RGB [in.length][in[0].length];

      for (int y = 0; y < H; y++)
      {
         for (int x = 0; x < W; x++)
         {
            dArray[x][y] = new RGB (in[x][y].r,in[x][y].g,in[x][y].b);
         }
      }

      for ( int y = 0; y < H; y++ )
      {
         for ( int x = 0; x < W; x++ )
         {
            RGB oldpixel = dArray[x][y];
            RGB newpixel = find_closest(oldpixel);
            dArray[x][y] = newpixel;

            RGB error = oldpixel.sub(newpixel);

            if ( x < W-1 ) dArray[x+1][y]   = dArray[x+1][y]  .add(error.mul(4./12));
            if ( x < W-2 ) dArray[x+2][y]   = dArray[x+2][y]  .add(error.mul(1./12));

            if ( y < H-1 ) dArray[x  ][y+1] = dArray[x  ][y+1].add(error.mul(4./12));
            if ( x < W-1
              && y < H-1 ) dArray[x+1][y+1] = dArray[x+1][y+1].add(error.mul(2./12));

            if ( y < H-2 ) dArray[x  ][y+2] = dArray[x  ][y+2].add(error.mul(1./12));
         }
      }
   }
//-------------------------------------------------------------------
   public RGB find_closest ( RGB oldpixel )
   {
      RGB close = palette[0];
      for ( RGB n : palette )
         if ( n.diff(oldpixel) < close.diff(oldpixel))
            close = n;
      return close;
   }
//-------------------------------------------------------------------
   public void save(RGB [][] in, String fileout) throws Exception
   {
      //Reprint header
      FileOutputStream fo = new FileOutputStream(new File(fileout));

      for (int i = 0; i < P.length(); i++)
      {
         fo.write(P.charAt(i));
      } fo.write(10);

      String WH = W + " " + H;
      for (int i = 0; i < WH.length(); i++)
      {
         fo.write(WH.charAt(i));
      } fo.write(10);

      String MAX = MAXVAL + "";
      for (int i = 0; i < 3; i++)
      {
         fo.write(MAX.charAt(i));
      } fo.write(10);

      for ( int y = 0; y < H; y++)
      {
         for ( int x = 0; x < W; x++)
         {
            fo.write((int)in[x][y].r);
            fo.write((int)in[x][y].g);
            fo.write((int)in[x][y].b);
         }
      }
      fo.close();
   }
//-------------------------------------------------------------------
   public void DXT ( String filein, String fileout ) throws Exception
   {
      //Check the OS environment to run an external program
      String env = System.getProperty("os.name");

      if ( env.contains("Windows") )
      {
         ProcessBuilder p = new ProcessBuilder
         ( "crunch.exe", "-file", filein, "/out", fileout );

         //Crunch has an output that clogs the buffer...
         p.redirectOutput(new File("output.txt"));
         final Process process = p.start();
         process.waitFor();

      }
      else
      {
         System.out.println("Operating System not supported: " + env);
         return;
      }
   }
//-------------------------------------------------------------------
   public void D_DXT ( String filein, String fileout ) throws Exception
   {
      //Check the OS environment to run an external program
      String env = System.getProperty("os.name");

      if ( env.contains("Windows") )
      {
         ProcessBuilder p = new ProcessBuilder
         ( "crunch.exe", "-file", filein, "-fileformat", "tga", "/out", fileout );

         //Crunch has an output that clogs the buffer...
         p.redirectOutput(new File("output.txt"));
         final Process process = p.start();
         process.waitFor();
      }
      else
      {
         System.out.println("Operating System not supported: " + env);
         return;
      }
   }
//-------------------------------------------------------------------
   public void ETC ( String filein, String fileout ) throws Exception
   {
      //Check the OS environment to run an external program
      String env = System.getProperty("os.name");

      if ( env.contains("Windows") )
      {
         ProcessBuilder p = new ProcessBuilder("etcpack.exe", filein, fileout);

         //etcpack has an output that clogs the buffer...
         p.redirectOutput(new File("output.txt"));
         final Process process = p.start();
         process.waitFor();
      }
      else
      {
         System.out.println("Operating System not supported: " + env);
         return;
      }
   }
//-------------------------------------------------------------------
   public void D_ETC ( String filein, String fileout ) throws Exception
   {
      //Check the OS environment to run an external program
      String env = System.getProperty("os.name");

      if ( env.contains("Windows") )
      {
         ProcessBuilder p = new ProcessBuilder("etcpack.exe", filein, fileout );

         //etcpack has an output that clogs the buffer...
         p.redirectOutput(new File("output.txt"));
         final Process process = p.start();
         process.waitFor();
      }
      else
      {
         System.out.println("Operating System not supported: " + env);
         return;
      }
   }
//-------------------------------------------------------------------
   public void REL1 ( RGB [][] in, String fileout ) throws Exception
   {
      List<RGB> paletteList = Arrays.asList(palette);

      int pArray [][] = new int [W][H];
      for ( int y = 0; y < H; y++)
      {
         for ( int x = 0; x < W; x++)
         {
            if ( paletteList.contains(dArray[x][y]) )
            {
               pArray[x][y] = paletteList.indexOf(in[x][y]);
            }
            else
            {
               pArray[x][y] = paletteList.indexOf(find_closest(in[x][y]));
            }
         }
      }
      
      //Reprint header
      FileOutputStream fo = new FileOutputStream(new File(fileout));

      fo.write('B'); fo.write('1'); fo.write(10);
      for ( int i = 0; i < colors.length(); i++ )
      {
         fo.write(colors.charAt(i));
      } fo.write(10);

      for (int i = 0; i < P.length(); i++)
      {
         fo.write(P.charAt(i));
      } fo.write(10);

      String WH = W + " " + H;
      for (int i = 0; i < WH.length(); i++)
      {
         fo.write(WH.charAt(i));
      } fo.write(10);

      String MAX = MAXVAL + "";
      for (int i = 0; i < 3; i++)
      {
         fo.write(MAX.charAt(i));
      } fo.write(10);

      for ( int y = 0; y < H; y+=2 )
      {
         for ( int x = 0; x < W; x+=2 )
         {
            fo.write(pArray[x][y]);
            fo.write(pArray[x+1][y+1]);
         }
      }

      fo.close();
   }
//-------------------------------------------------------------------
   public void REL2 ( RGB [][] in, String fileout ) throws Exception
   {
      List<RGB> paletteList = Arrays.asList(palette);

      int pArray [][] = new int [W][H];
      int [] freq = new int [paletteList.size()];
      Map<Integer,Integer> unsortmap = new TreeMap<Integer, Integer>();

      for ( int y = 0; y < H; y++)
      {
         for ( int x = 0; x < W; x++)
         {
            if ( paletteList.contains(dArray[x][y]) )
            {
               pArray[x][y] = paletteList.indexOf(in[x][y]);
               freq[paletteList.indexOf(in[x][y])]++;
            }
            else
            {
               pArray[x][y] = paletteList.indexOf(find_closest(in[x][y]));
               freq[paletteList.indexOf(find_closest(in[x][y]))]++;
            }
         }
      }

      for ( int i = 0; i < freq.length; i++)
      {
         unsortmap.put(i,freq[i]);
      }

      //Reprint header
      FileOutputStream fo = new FileOutputStream(new File(fileout));

      fo.write('B'); fo.write('2'); fo.write(10);
      for ( int i = 0; i < colors.length(); i++ )
      {
         fo.write(colors.charAt(i));
      } fo.write(10);

      for (int i = 0; i < P.length(); i++)
      {
         fo.write(P.charAt(i));
      } fo.write(10);

      String WH = W + " " + H;
      for (int i = 0; i < WH.length(); i++)
      {
         fo.write(WH.charAt(i));
      } fo.write(10);

      String MAX = MAXVAL + "";
      for (int i = 0; i < 3; i++)
      {
         fo.write(MAX.charAt(i));
      } fo.write(10);

      Iterator<?> itr = valueSortedMap(unsortmap).iterator();

      for ( int i = 0; i < common.length && itr.hasNext(); i ++ )
      {
         Map.Entry next = (Map.Entry)itr.next();
         common[i] = (int)next.getKey();
         fo.write((int)next.getKey());
      }

      for ( int y = 0; y < H; y+=2 )
      {
         for ( int x = 0; x < W; x+=4 )
         {
            fo.write(pArray[x  ][y]);
            fo.write(pArray[x+2][y]);

            int [] diff = new int [6];
            diff[0] = closest(pArray[x+1][y  ], common);
            diff[1] = closest(pArray[x  ][y+1], common);
            diff[2] = closest(pArray[x+1][y+1], common);

            diff[3] = closest(pArray[x+3][y  ], common);
            diff[4] = closest(pArray[x+2][y+1], common);
            diff[5] = closest(pArray[x+3][y+1], common);

            String s = "";
            for ( int i = 0; i < diff.length; i++ )
            {
               String n = Integer.toBinaryString(diff[i]);
               while ( n.length() < 4 ) n = "0" + n;
               s += n;
            }

            fo.write(Integer.parseInt(s.substring( 0, 8),2));
            fo.write(Integer.parseInt(s.substring( 9,16),2));
            fo.write(Integer.parseInt(s.substring(17,24),2));
         }
      }

      fo.close();
   }
//-------------------------------------------------------------------
   public void D_REL1_v1 ( String filein ) throws Exception
   {
      FileInputStream fi = new FileInputStream(new File(filein));
      Scanner sc = new Scanner(fi);

      sc.nextLine(); //Remove magic number
      SetPalette(sc.nextLine());

      this.P = sc.nextLine();

      int comment;
      String WH = sc.nextLine();

      for ( comment = 0; WH.charAt(0) == '#'; comment++ )
      {
         //Count and strip comments
         WH = sc.nextLine();
      }

      String [] split = WH.split(" ");
      this.W = Integer.parseInt(split[0]);
      this.H = Integer.parseInt(split[1]);

      MAXVAL = Integer.parseInt(sc.nextLine());
      fi.close();

      fi = new FileInputStream(new File(filein));
      for (int i = 0; i < 5+comment; i++)
      {
         for (int c; (c = fi.read()) != 10;)
         {
         }
      }

      this.cArray = new RGB [W][H];

      for ( int y = 0; y < H; y+=2 )
      {
         for ( int x = 0; x < W; x+=2)
         {
            RGB n1 = palette[fi.read()];
            RGB n2 = palette[fi.read()];
            cArray [x][y] = new RGB((int)n1.r,
                                    (int)n1.g,
                                    (int)n1.b);
            cArray [x+1][y+1] = new RGB((int)n2.r,
                                        (int)n2.g,
                                        (int)n2.b);

            cArray [x+1][y] = new RGB((int)n1.r,
                                      (int)n1.g,
                                      (int)n1.b);
            cArray [x][y+1] = new RGB((int)n2.r,
                                      (int)n2.g,
                                      (int)n2.b);
         }
      }
      fi.close();
   }
//-------------------------------------------------------------------
   public void D_REL1_v2 ( String filein ) throws Exception
   {
      FileInputStream fi = new FileInputStream(new File(filein));
      Scanner sc = new Scanner(fi);

      sc.nextLine(); //Remove magic number
      SetPalette(sc.nextLine());

      this.P = sc.nextLine();

      int comment;
      String WH = sc.nextLine();

      for ( comment = 0; WH.charAt(0) == '#'; comment++ )
      {
         //Count and strip comments
         WH = sc.nextLine();
      }

      String [] split = WH.split(" ");
      this.W = Integer.parseInt(split[0]);
      this.H = Integer.parseInt(split[1]);

      MAXVAL = Integer.parseInt(sc.nextLine());
      fi.close();

      fi = new FileInputStream(new File(filein));
      for (int i = 0; i < 5+comment; i++)
      {
         for (int c; (c = fi.read()) != 10;)
         {
         }
      }

      this.cArray = new RGB [W][H];

      for ( int y = 0; y < H; y+=2 )
      {
         for ( int x = 0; x < W; x+=2)
         {
            RGB n1 = palette[fi.read()];
            RGB n2 = palette[fi.read()];
            cArray [x][y] = new RGB((int)n1.r,
                                    (int)n1.g,
                                    (int)n1.b);

            cArray [x+1][y+1] = new RGB((int)n2.r,
                                        (int)n2.g,
                                        (int)n2.b);

            cArray [x+1][y] = new RGB(
                                ((int)n1.r+(int)n2.r)/2,
                                ((int)n1.g+(int)n2.g)/2,
                                ((int)n1.b+(int)n2.b)/2);
            cArray [x][y+1] = new RGB(
                                ((int)n1.r+(int)n2.r)/2,
                                ((int)n1.g+(int)n2.g)/2,
                                ((int)n1.b+(int)n2.b)/2);
         }
      }

      fi.close();
   }
//-------------------------------------------------------------------
   public void D_REL2 ( String filein ) throws Exception
   {
      FileInputStream fi = new FileInputStream(new File(filein));
      Scanner sc = new Scanner(fi);

      sc.nextLine(); //Remove magic number
      SetPalette(sc.nextLine());

      this.P = sc.nextLine();

      int comment;
      String WH = sc.nextLine();

      for ( comment = 0; WH.charAt(0) == '#'; comment++ )
      {
         //Count and strip comments
         WH = sc.nextLine();
      }

      String [] split = WH.split(" ");
      W = Integer.parseInt(split[0]);
      H = Integer.parseInt(split[1]);

      MAXVAL = Integer.parseInt(sc.nextLine());
      fi.close();

      fi = new FileInputStream(new File(filein));
      for (int i = 0; i < 5+comment; i++)
      {
         for (int c; (c = fi.read()) != 10;)
         {
         }
      }

      for ( int i = 0; i < common.length; i ++ )
      {
         common[i] = fi.read();
      }

      this.cArray = new RGB [W][H];

      for ( int y = 0; y < H; y+=2 )
      {
         for ( int x = 0; x < W; x+=4)
         {
            int n1 = fi.read();
            int n2 = fi.read();

            int r1 = fi.read();
            int r2 = fi.read();
            int r3 = fi.read();

            String temp1 = Integer.toBinaryString(r1);
            while ( temp1.length() < 8 ) temp1 = "0" + temp1;

            String temp2 = Integer.toBinaryString(r2);
            while ( temp2.length() < 8 ) temp2 = "0" + temp2;

            String temp3 = Integer.toBinaryString(r3);
            while ( temp3.length() < 8 ) temp3 = "0" + temp3;

            int [] diff = new int [6];
            diff[0] = common[Integer.parseInt(temp1.substring(0,4), 2)];
            diff[1] = common[Integer.parseInt(temp1.substring(4,8), 2)];
            diff[2] = common[Integer.parseInt(temp2.substring(0,4), 2)];
            diff[3] = common[Integer.parseInt(temp2.substring(4,8), 2)];
            diff[4] = common[Integer.parseInt(temp3.substring(0,4), 2)];
            diff[5] = common[Integer.parseInt(temp3.substring(4,8), 2)];

            cArray [x  ][y  ] = new RGB(palette[n1].r,
                                        palette[n1].g,
                                        palette[n1].b);

            cArray [x+1][y  ] = new RGB(palette[diff[0]].r,
                                        palette[diff[0]].g,
                                        palette[diff[0]].b);

            cArray [x  ][y+1] = new RGB(palette[diff[1]].r,
                                        palette[diff[1]].g,
                                        palette[diff[1]].b);

            cArray [x+1][y+1] = new RGB(palette[diff[2]].r,
                                        palette[diff[2]].g,
                                        palette[diff[2]].b);

            cArray [x+2][y  ] = new RGB(palette[n2].r,
                                        palette[n2].g,
                                        palette[n2].b);

            cArray [x+3][y  ] = new RGB(palette[diff[3]].r,
                                        palette[diff[3]].g,
                                        palette[diff[3]].b);

            cArray [x+2][y+1] = new RGB(palette[diff[4]].r,
                                        palette[diff[4]].g,
                                        palette[diff[4]].b);

            cArray [x+3][y+1] = new RGB(palette[diff[5]].r,
                                        palette[diff[5]].g,
                                        palette[diff[5]].b);
         }
      }

      fi.close();
   }
//-------------------------------------------------------------------
   public BufferedImage array ( RGB [][] in )
   {
      int [] out  = new int [W*H];

      BufferedImage img = new BufferedImage(W, H,
                              BufferedImage.TYPE_INT_RGB);

      for ( int x = 0; x < W; x++)
      {
         for ( int y = 0; y < H; y++)
         {
            img.setRGB(x, y, new Color((int)in[x][y].r,
                                       (int)in[x][y].g,
                                       (int)in[x][y].b).getRGB());
         }
      }

      return img;
   }
//-------------------------------------------------------------------
   static <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>> valueSortedMap(Map<K,V> map)
   {
      SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
        new Comparator<Map.Entry<K,V>>()
      {

         @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2)
         {
            int res = e2.getValue().compareTo(e1.getValue());
            if (e1.getKey().equals(e2.getKey()))
            {
               return res; // Code will now handle equality properly
            }
            else
            {
               return res != 0 ? res : 1; // While still adding all entries
            }
         }
      });
      sortedEntries.addAll(map.entrySet());
      return sortedEntries;
   }
//-------------------------------------------------------------------
   public int closest ( int n, int [] a )
   {
      int close = 0;
      for ( int i = 1; i < a.length; i++ )
      {
         if ( Math.abs(n-a[i]) <= Math.abs(n-a[close]))
         {
            close = i;
         }
      }
      return close;
   }
//-------------------------------------------------------------------
}//end Compressor
/////////////////////////////////////////////////////////////////////
class RGB
{
   double r,g,b;
//-------------------------------------------------------------------
   public RGB (double r, double g, double b)
   {
      this.r = r;
      this.g = g;
      this.b = b;
   }
//-------------------------------------------------------------------
   public RGB (int color)
   {
      this.r = new Color(color).getRed();
      this.g = new Color(color).getGreen();
      this.b = new Color(color).getBlue();
   }
//-------------------------------------------------------------------
   public RGB sub ( RGB x )
   {
      return new RGB ( r - x.r, g - x.g, b - x.b );
   }
//-------------------------------------------------------------------
   public RGB add ( RGB x )
   {
      return new RGB ( r + x.r, g + x.g, b + x.b );
   }
//-------------------------------------------------------------------
   public RGB add ( double x )
   {
      return new RGB ( r + x, g + x, b + x );
   }
//-------------------------------------------------------------------
   public RGB mul ( double x )
   {
      return new RGB ( r * x, g * x, b * x );
   }
//-------------------------------------------------------------------
   public double diff ( RGB x )
   {
      double diff_r = r - x.r;
      double diff_g = g - x.g;
      double diff_b = b - x.b;
      return diff_r*diff_r + diff_g*diff_g + diff_b*diff_b;
   }
//-------------------------------------------------------------------
   public boolean equals ( RGB x )
   {
      return ( (int)r == (int)x.r &&
               (int)g == (int)x.g &&
               (int)b == (int)x.b );
   }
//-------------------------------------------------------------------
   public RGB clamp ()
   {
      return new RGB (Math.floor(Math.max(0, Math.min(255, r))),
                      Math.floor(Math.max(0, Math.min(255, g))),
                      Math.floor(Math.max(0, Math.min(255, b))));
   }
//-------------------------------------------------------------------
   public int pixelize ()
   {
      return new Color((int)r, (int)g, (int)b).getRGB();
   }
//-------------------------------------------------------------------
   public String toString()
   {
      return r + "_" + g + "_" + b;
   }
//-------------------------------------------------------------------
}//end RGB
/////////////////////////////////////////////////////////////////////
class Main
{
   static Compressor cmp;
//-------------------------------------------------------------------
   public static void main( String [] args) throws Exception
   {
      if ( args.length == 0 )
      {
         JFrame frame = new JFrame("ImageMonkey");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         //frame.setPreferredSize(new Dimension(800,600));

         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

         ImageMonkey stage = new ImageMonkey();
         stage.addComponentToPane(frame.getContentPane());

         frame.pack();
         Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
         frame.setLocation((d.width-frame.getWidth())/2,
                           (d.height-frame.getHeight())/2 );
         frame.setVisible(true);
      }
      else
      {
         //args[0] = Input file
         //args[1] = Palette
         //args[2] = Dither Method
         //args[3] = Dithered File
         //args[4] = Compression Method
         //args[5] = Decompression Method
         //args[6] = Compressed File
         //args[7] = Decompressed File
         
         ImageMonkey stage = new ImageMonkey();
         
         System.out.println(args[1] + " " + args[2] + " " + args[4] + " " + args[5] );
         
         long timeNow = 0;
         long timeLastChanged = 0;
         long elapsedTime = 0;

         String filein = args[0];
         String file_dithered  = args[3];
         cmp = new Compressor(filein);

         //Dither with specified palette
         cmp.SetPalette(args[1]);
         timeLastChanged = System.currentTimeMillis();
         if (args[2].equals("F")) cmp.Floyd_Dither(cmp.oArray);
         if (args[2].equals("O")) cmp.Ordered_Dither(cmp.oArray);
         if (args[2].equals("J")) cmp.Jarvis_Dither(cmp.oArray);
         if (args[2].equals("R")) cmp.Relevance_Dither(cmp.oArray);
         if (args[2].equals("N")); //Do Nothing!
         
         timeNow = System.currentTimeMillis();
         
         System.out.println("Time: " + (int)(timeNow-timeLastChanged) + "ms");
         System.out.println("Dithered RMS: " + stage.RMS(cmp.oArray,cmp.dArray));
         System.out.println("Dithered SNR: " + stage.SNR(stage.fxy,stage.total) + "\n");

         //Save Dithered Image
         cmp.save(cmp.dArray, file_dithered);

         String file_cmp = args[6];
         String fileout  = args[7];

         //Block Subtraction Methods
         if (args[4].equals("R1"))
         {
            timeLastChanged = System.currentTimeMillis();
            cmp.REL1(cmp.dArray, file_cmp);
            timeNow = System.currentTimeMillis();
            System.out.println("Time: " + (int)(timeNow-timeLastChanged) + "ms");
            System.out.println("Compressed Ratio: " +
                               stage.c_ratio(file_dithered, file_cmp) + "\n");
            
            timeLastChanged = System.currentTimeMillis();
            unpack ( args[5], file_cmp );
            timeNow = System.currentTimeMillis();
            cmp.save(cmp.cArray, fileout);
            
            System.out.println("Time: " + (int)(timeNow-timeLastChanged) + "ms");
            System.out.println("Decompressed RMS: " + stage.RMS(cmp.oArray,cmp.cArray));
            System.out.println("Decompressed SNR: " + stage.SNR(stage.fxy,stage.total));
            System.out.println();
         }
         
         if (args[4].equals("R2"))
         {
            timeLastChanged = System.currentTimeMillis();
            cmp.REL2(cmp.dArray, file_cmp);
            timeNow = System.currentTimeMillis();
            System.out.println("Time: " + (int)(timeNow-timeLastChanged) + "ms");
            System.out.println("Compressed Ratio: " +
                               stage.c_ratio(file_dithered, file_cmp) + "\n");
            
            timeLastChanged = System.currentTimeMillis();
            unpack ( args[5], file_cmp );
            timeNow = System.currentTimeMillis();
            cmp.save(cmp.cArray, fileout);
            
            System.out.println("Time: " + (int)(timeNow-timeLastChanged) + "ms");
            System.out.println("Decompressed RMS: " + stage.RMS(cmp.oArray,cmp.cArray));
            System.out.println("Decompressed SNR: " + stage.SNR(stage.fxy,stage.total));
            System.out.println();
         }
         
         if (args[4].equals("DXT"))
         {
            ProcessBuilder p = new ProcessBuilder
            ( "i_view32.exe", file_dithered, "/convert=temp.tga" );
            p.redirectOutput(new File("output.txt"));
            Process process = p.start();
            process.waitFor();
            
            timeLastChanged = System.currentTimeMillis();
            cmp.DXT("temp.tga", file_cmp);
            timeNow = System.currentTimeMillis();
            System.out.println("Time: " + (int)(timeNow-timeLastChanged) + "ms");
            System.out.println("Compressed Ratio: " +
                               stage.c_ratio(file_dithered,file_cmp) + "\n");
            
            timeLastChanged = System.currentTimeMillis();
            cmp.D_DXT(fileout, "temp.tga");
            timeNow = System.currentTimeMillis();

            p = new ProcessBuilder
            ( "i_view32.exe", "temp.tga", "/convert="+fileout);
            p.redirectOutput(new File("output.txt"));
            process = p.start();
            process.waitFor();

            cmp.cArray = new Compressor(fileout).oArray.clone(); 
            
            System.out.println("Time: " + (int)(timeNow-timeLastChanged) + "ms");
            System.out.println("Decompressed RMS: " + stage.RMS(cmp.oArray,cmp.cArray));
            System.out.println("Decompressed SNR: " + stage.SNR(stage.fxy,stage.total));
            System.out.println();
         }
         
         if (args[4].equals("ETC"))
         {
            timeLastChanged = System.currentTimeMillis();
            cmp.ETC(file_dithered, file_cmp);
            timeNow = System.currentTimeMillis();
            System.out.println("Time: " + (int)(timeNow-timeLastChanged) + "ms");
            System.out.println("Compressed Ratio: " +
                               stage.c_ratio(file_dithered,file_cmp) + "\n");
            
            timeLastChanged = System.currentTimeMillis();
            cmp.D_ETC(file_cmp, fileout);
            timeNow = System.currentTimeMillis();
            
            cmp.cArray = new Compressor(fileout).oArray.clone();
            
            System.out.println("Time: " + (int)(timeNow-timeLastChanged) + "ms");
            System.out.println("Decompressed RMS: " + stage.RMS(cmp.oArray,cmp.cArray));
            System.out.println("Decompressed SNR: " + stage.SNR(stage.fxy,stage.total));
            System.out.println();
         }
      }
   }
//-------------------------------------------------------------------
   public static void unpack ( String decmp, String filein ) throws Exception
   {
      FileInputStream fi = new FileInputStream(new File(filein));
      Scanner sc = new Scanner(fi);
      String line = sc.nextLine();

      if ( line.equals("B1") )
      {
         if ( decmp.equals("D_REL1_v1")) 
         {
            cmp.D_REL1_v1(filein);
         }
         if ( decmp.equals("D_REL1_v2"))
         {  
            cmp.D_REL1_v2(filein);
         }
      }
      else if ( line.equals("B2") )
      {
         if ( decmp.equals("D_REL2")) cmp.D_REL2(filein);
         
      }
   }
//-------------------------------------------------------------------
}//end class Main
/////////////////////////////////////////////////////////////////////
class ImageMonkey implements ActionListener
{
   private JPanel comboBoxPane, stage, statBoxPane;
   private String original;
   private JComboBox<String> cb_d, cb_c, cb_p;
   private JButton Dither, Compress;
   private JTextField file_in, file_dith, file_cmp;
   private JButton Unpack;
   private JLabel start, result;
   private JLabel ratio_d, rms_d, snr_d, time_d;
   private JLabel ratio_c, rms_c, snr_c, time_c;
   private JLabel ratio_dec, rms_dec, snr_dec, time_dec;
   private Compressor cmp;
   double fxy, total;
//-------------------------------------------------------------------
   public void addComponentToPane(Container pane) throws Exception
   {
      //Top Area: ToolBox Goodies
      comboBoxPane = new JPanel(); //use FlowLayout, Default

      String [] comboBoxItems_d =
      {"Relevance", "Floyd", "Ordered", "Jarvis", "None"};
      cb_d = new JComboBox<String>(comboBoxItems_d);
      cb_d.setEditable(false);
      comboBoxPane.add(cb_d);

      String [] comboBoxItems_p =
      {"216-Colors", "2-Colors", "8-Colors", "16-Colors"};
      cb_p = new JComboBox<String>(comboBoxItems_p);
      cb_p.setEditable(false);
      comboBoxPane.add(cb_p);

      Dither = new JButton("Dither");
      Dither.addActionListener(this);
      comboBoxPane.add(Dither);

      String [] comboBoxItems_c =
      {"REL1_v1", "REL1_v2", "REL2", "DXT", "ETC"};
      cb_c = new JComboBox<String>(comboBoxItems_c);
      cb_c.setEditable(false);
      comboBoxPane.add(cb_c);

      Compress = new JButton("Compress");
      Compress.addActionListener(this);
      comboBoxPane.add(Compress);

      comboBoxPane.add(new JLabel("Input File"));
      file_in = new JTextField("lena.ppm",10);
      comboBoxPane.add(file_in);

      Unpack = new JButton("Unpack");
      Unpack.addActionListener(this);
      comboBoxPane.add(Unpack);

      comboBoxPane.add(new JLabel("Dithered File"));
      file_dith = new JTextField("dith_lena.ppm",10);
      comboBoxPane.add(file_dith);

      comboBoxPane.add(new JLabel("Compressed File"));
      file_cmp = new JTextField("cmp_lena.ktx",10);
      comboBoxPane.add(file_cmp);

      //Creating StatBoxPane
      statBoxPane = new JPanel();
      statBoxPane.setLayout(new GridLayout(3,6));

      statBoxPane.add(new JLabel("Dithered"));
      ratio_d = new JLabel("Ratio:");
      ratio_d.setPreferredSize(new Dimension(85,15));
      statBoxPane.add(ratio_d);

      rms_d = new JLabel("RMS:");
      rms_d.setPreferredSize(new Dimension(80,15));
      statBoxPane.add(rms_d);

      snr_d = new JLabel("SNR:");
      snr_d.setPreferredSize(new Dimension(120,15));
      statBoxPane.add(snr_d);

      time_d = new JLabel("Time:");
      time_d.setPreferredSize(new Dimension(90,15));
      statBoxPane.add(time_d);

      statBoxPane.add(new JLabel("Compressed"));
      ratio_c = new JLabel("Ratio:");
      ratio_c.setPreferredSize(new Dimension(85,15));
      statBoxPane.add(ratio_c);

      rms_c = new JLabel("RMS:");
      rms_d.setPreferredSize(new Dimension(80,15));
      statBoxPane.add(rms_c);

      snr_c = new JLabel("SNR:");
      snr_c.setPreferredSize(new Dimension(120,15));
      statBoxPane.add(snr_c);

      time_c = new JLabel("Time:");
      time_c.setPreferredSize(new Dimension(90,15));
      statBoxPane.add(time_c);

      statBoxPane.add(new JLabel("Decompressed"));
      ratio_dec = new JLabel("Ratio:");
      ratio_dec.setPreferredSize(new Dimension(85,15));
      statBoxPane.add(ratio_dec);

      rms_dec = new JLabel("RMS:");
      rms_dec.setPreferredSize(new Dimension(80,15));
      statBoxPane.add(rms_dec);

      snr_dec = new JLabel("SNR:");
      snr_dec.setPreferredSize(new Dimension(120,15));
      statBoxPane.add(snr_dec);

      time_dec = new JLabel("Time:");
      time_dec.setPreferredSize(new Dimension(90,15));
      statBoxPane.add(time_dec);

      //Center Area: Picture Display Area
      //Images have been scaled down for sanitary reasons.
      JPanel stage = new JPanel();
      stage.setPreferredSize(new Dimension(1000,600));
      JScrollPane scroller = new JScrollPane(stage,
         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
         JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

      cmp = new Compressor("lena.ppm"); //Default Image

      start  = new JLabel(new ImageIcon(cmp.array(cmp.oArray)));
      //result = new JLabel(new ImageIcon(cmp.array(cmp.oArray)));
      stage.add(start);
      //stage.add(result);

      //Initialize the Layout
      pane.add(comboBoxPane, BorderLayout.PAGE_START);
      pane.add(scroller,     BorderLayout.CENTER);
      pane.add(statBoxPane,  BorderLayout.PAGE_END);
   }
//-------------------------------------------------------------------
   public void actionPerformed(ActionEvent event)
   {
      //Loads different toolboxes for each selection.
      Object source = event.getSource();

      long timeNow = 0 , timeLastChanged = 0, elapsedTime = 0;
      long timeNow2 = 0, timeLastChanged2 = 0, elapsedTime2 = 0;
      boolean dithered = false, compressed = false;

      try
      {
         //Image Operations
         if ( source == Dither)
         {
            String dither_c = (String)cb_d.getSelectedItem();
            if ( compressed ) cmp = new Compressor(file_in.getText());
            cmp.SetPalette((String)cb_p.getSelectedItem());

            timeLastChanged = System.currentTimeMillis();
            if(dither_c.equals("None"))
            {
               cmp.dArray = cmp.oArray.clone();
            }
            if(dither_c.equals("Floyd"))
            {
               cmp.Floyd_Dither(cmp.oArray);
            }
            else if(dither_c.equals("Ordered"))
            {
               cmp.Ordered_Dither(cmp.oArray);
            }
            else if (dither_c.equals("Jarvis"))
            {
               cmp.Jarvis_Dither(cmp.oArray);
            }
            else if (dither_c.equals("Relevance"))
            {
               cmp.Relevance_Dither(cmp.oArray);
            }

            timeNow = System.currentTimeMillis();
            start.setIcon(new ImageIcon(cmp.array(cmp.dArray)));
            cmp.save(cmp.dArray, file_dith.getText());

            time_d.setText("Time: " + (int)(timeNow-timeLastChanged) + "ms");
            ratio_d.setText("Ratio: " + c_ratio(file_in.getText(),
                                                file_dith.getText()));
            rms_d.setText("RMS: " + RMS(cmp.oArray, cmp.dArray));
            snr_d.setText("SNR: " + SNR(fxy, total));

            dithered = true;
         }

         if ( source == Compress )
         {
            String compress_c = (String)cb_c.getSelectedItem();

            if(compress_c.equals("DXT"))
            {
               //Preventing process exiting due to bad file extensions.
               String [] s = file_cmp.getText().split("\\.");
               file_cmp.setText(s[0]+".dds");

               //TGA Convertor
               ProcessBuilder p = new ProcessBuilder(
               "i_view32.exe", file_dith.getText(), "/convert=temp.tga" );
               p.redirectOutput(new File("output.txt"));
               Process process = p.start();
               process.waitFor();

               timeLastChanged = System.currentTimeMillis();
               cmp.DXT("temp.tga", file_cmp.getText());
               timeNow = System.currentTimeMillis();

               timeLastChanged2 = System.currentTimeMillis();
               cmp.D_DXT(file_cmp.getText(), "temp.tga");
               timeNow2 = System.currentTimeMillis();

               //TGA Convertor
               p = new ProcessBuilder(
               "i_view32.exe", "temp.tga", "/convert=temp.ppm" );
               p.redirectOutput(new File("output.txt"));
               process = p.start();
               process.waitFor();

               cmp.cArray = new Compressor("temp.ppm").oArray.clone();
            }
            else if(compress_c.equals("ETC"))
            {
               //Preventing process exiting due to bad file extensions.
               String [] s = file_cmp.getText().split("\\.");
               file_cmp.setText(s[0]+".ktx");

               timeLastChanged = System.currentTimeMillis();
               cmp.ETC(file_dith.getText(), file_cmp.getText());
               timeNow = System.currentTimeMillis();

               timeLastChanged2 = System.currentTimeMillis();
               cmp.D_ETC(file_cmp.getText(), "temp.ppm");
               timeNow2 = System.currentTimeMillis();

               cmp.cArray = new Compressor("temp.ppm").oArray.clone();
            }
            else if(compress_c.equals("REL1_v1"))
            {
               timeLastChanged = System.currentTimeMillis();
               cmp.REL1(cmp.dArray, file_cmp.getText());
               timeNow = System.currentTimeMillis();

               timeLastChanged2 = System.currentTimeMillis();
               cmp.D_REL1_v1(file_cmp.getText());
               timeNow2 = System.currentTimeMillis();

               cmp.save(cmp.cArray, "temp.ppm");
            }
            else if(compress_c.equals("REL1_v2"))
            {
               timeLastChanged = System.currentTimeMillis();
               cmp.REL1(cmp.dArray, file_cmp.getText());
               timeNow = System.currentTimeMillis();

               timeLastChanged2 = System.currentTimeMillis();
               cmp.D_REL1_v2(file_cmp.getText());
               timeNow2 = System.currentTimeMillis();

               cmp.save(cmp.cArray, "temp.ppm");
            }
            else if(compress_c.equals("REL2"))
            {
               timeLastChanged = System.currentTimeMillis();
               cmp.REL2(cmp.dArray, file_cmp.getText());
               timeNow = System.currentTimeMillis();

               timeLastChanged2 = System.currentTimeMillis();
               cmp.D_REL2(file_cmp.getText());
               timeNow2 = System.currentTimeMillis();

               cmp.save(cmp.cArray, "temp.ppm");
            }
            else
            {
               //Do nothing?
            }

            start.setIcon(new ImageIcon(cmp.array(cmp.cArray)));

            time_c.setText("Time: " + (int)(timeNow-timeLastChanged) + "ms");
            ratio_c.setText("Ratio: " + c_ratio(file_dith.getText(),
                                                file_cmp.getText()));
            rms_c.setText("RMS: ");
            snr_c.setText("SNR: ");

            time_dec.setText("Time: " + (int)(timeNow2-timeLastChanged2) + "ms");
            ratio_dec.setText("Ratio: ");
            rms_dec.setText("RMS: " + RMS(cmp.oArray, cmp.cArray));
            snr_dec.setText("SNR: " + SNR(fxy, total));

            compressed = true;
            dithered = false;
         }

         if ( source == Unpack )
         {
            FileInputStream fi = new FileInputStream(new File(file_in.getText()));
            Scanner sc = new Scanner(fi);
            String line = sc.nextLine();

            timeLastChanged = System.currentTimeMillis();
            if ( line.equals("P6") )
            {
               cmp = new Compressor(file_in.getText());
            }
            else if ( line.equals("B1") )
            {
               cmp.D_REL1_v1(file_in.getText());
            }
            else if ( line.equals("B2") )
            {
               cmp.D_REL1_v1(file_in.getText());
            }
            timeNow = System.currentTimeMillis();

            if (line.equals("B1") || line.equals("B2"))
            {
               cmp.save(cmp.cArray, "temp.ppm");
               cmp.oArray = cmp.cArray.clone();
            }
            else if ( line.equals("P6") )
            {
               //cmp.oArray = cmp.cArray.clone();
            }

            start.setIcon(new ImageIcon(cmp.array(cmp.oArray)));
            time_dec.setText("Time: " + (int)(timeNow-timeLastChanged) + "ms");
            ratio_dec.setText("Ratio: ");
            rms_dec.setText("RMS: ");
            snr_dec.setText("SNR: ");

            compressed = false;
            dithered = false;
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
//-------------------------------------------------------------------
   public String c_ratio (String file_in, String file_out) throws Exception
   {
      FileInputStream fi = new FileInputStream(new File(file_in));
      FileInputStream fo = new FileInputStream(new File(file_out));

      double charCount_in  = 0;
      double charCount_out = 0;

      for (int c; (c = fi.read()) != -1;)
      {
         charCount_in++;
      }
      for (int c; (c = fo.read()) != -1;)
      {
         charCount_out++;
      }

      NumberFormat nf = NumberFormat.getNumberInstance();
      nf.setMaximumFractionDigits(3);

      return nf.format(charCount_in/charCount_out);
   }
//-------------------------------------------------------------------
   public String RMS (RGB [][] in, RGB [][] out)  throws Exception
   {
      double diff_r = 0;
      double diff_g = 0;
      double diff_b = 0;
      fxy = 0;
      total = 0;
      double M = 0;
      double N = 0;

      for ( int y = 0; y < in[0].length; y++)
      {
         for ( int x = 0; x < in.length; x++)
         {
            diff_r = in[x][y].r - out[x][y].r;
            diff_g = in[x][y].g - out[x][y].g;
            diff_b = in[x][y].b - out[x][y].b;
            total += Math.pow(diff_r,2) + Math.pow(diff_g,2) + Math.pow(diff_b,2);
            fxy   += Math.pow(in[x][y].r+in[x][y].g+in[x][y].b,2);
            M++;
         }
      }

      N = in.length;
      M = in[0].length;
      total = total/(M*N);
      total = Math.sqrt(total);

      NumberFormat nf = NumberFormat.getNumberInstance();
      nf.setMaximumFractionDigits(3);

      return nf.format(total);
   }
//-------------------------------------------------------------------
   public String SNR (double fxy, double total) throws Exception
   {
      double SNR = fxy/total;

      NumberFormat nf = NumberFormat.getNumberInstance();
      nf.setMaximumFractionDigits(3);

      return nf.format(SNR);
   }
//-------------------------------------------------------------------
}//end class ImageMonkey
/////////////////////////////////////////////////////////////////////
