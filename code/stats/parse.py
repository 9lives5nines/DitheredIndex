import glob
from itertools import islice

dither = open ('dither.out', 'w')
compress = open ('compress.out', 'w')
decompress = open ('decompress.out', 'w')

files = glob.glob("stats_*.txt")

for file in files:
   name = file[6:-4].title()
   dither.write("\multicolumn{5}{ |c| }{" +name+"} \\\ \hline\n")
   compress.write("\multicolumn{5}{ |c| }{" +name+"} \\\ \hline\n")
   decompress.write("\multicolumn{6}{ |c| }{" +name+"} \\\ \hline\n")
   with open(file) as target:
      while True:
         lines = list(islice(target,12))
         if not lines:
            break
            
         lines = map(lambda s: s.strip(), lines)
         lines = [line for line in lines if line.strip()]
         
         para = lines[0].split();
         s = para[0] + " & "
         if para[1] == 'F':
            s += 'Floyd & '
         if para[1] == 'O':
            s += 'Ordered & '
         if para[1] == 'J':
            s += 'Jarvis & '
         if para[1] == 'R':
            s += 'Relevance & '
         if para[1] == "N":
            s += 'None & '
         r = s + ""
         s = ''
            
         s += lines[1].split()[1]
         s += ' & '
         s += lines[2].split()[2]
         s += ' & '
         s += lines[3].split()[2]
         s += ' \\\ \hline'
         dither.write(r + s + "\n")
         s = ''
         
         if para[2] == 'R1':
            s += 'Relevance 1 & '
         if para[2] == 'R2':
            s += 'Relevance 2 & '
         if para[2] == 'DXT':
            s += 'Direct X & '
         if para[2] == 'ETC':
            s += 'Ericsson & '
            
         s += lines[4].split()[1]
         s += ' & '
         s += lines[5].split()[2]
         s += ' \\\ \hline'
         compress.write(r + s + "\n")
         s = ''
         
         if para[3] == 'D_REL1_v1':
            s += 'Relevance 1 v1 & '
         if para[3] == 'D_REL1_v2':
            s += 'Relevance 1 v2 & '
         if para[3] == 'D_REL2':
            s += 'Relevance 2 & '
         if para[3] == 'DXT':
            s += 'Direct X & '
         if para[3] == 'ETC':
            s += 'Ericsson & '

         s += lines[6].split()[1]
         s += ' & '
         s += lines[7].split()[2]
         s += ' & '
         s += lines[8].split()[2]
         s += ' \\\ \hline'
         
         decompress.write(r + s + "\n")