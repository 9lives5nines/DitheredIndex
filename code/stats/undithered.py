import glob
from itertools import islice

undither = open ('undithered.out', 'w')

files = glob.glob("undithered.txt")

for file in files:
   count = 0
   with open(file) as target:
      while True:
         if count == 0 or count == 5:
            lines = list(islice(target,1))
            if not lines:
               break
            name = lines[0].split()[0]
            undither.write("\multicolumn{7}{ |c| }{" +name+"} \\\ \hline\n")
            count = 0
         lines = list(islice(target,12))
         count = count + 1
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
         #undither.write(r + s + "\n")
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
         s += ' & '
         #s += ' \\\ \hline'
         undither.write(s)
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
         
         undither.write(s + "\n")