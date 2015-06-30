import sys;
import gzip;

#f1 = gzip.open('nodes.xml.gz','w', 1);
#f2 = gzip.open('ways.xml.gz', 'w', 1);
#f3 = gzip.open('relations.xml.gz', 'w', 1);

f1 = open('nodes.xml','w');
f2 = open('ways.xml', 'w');
f3 = open('relations.xml', 'w');

i=0;

current_file = f1;
line=''
counter=0
for line in sys.stdin:
	if (counter<=1):
		f2.write(line);
		f3.write(line);
	counter=counter+1;
	if (i==0 and line.lstrip().startswith('<way ')):
		current_file=f2;
		i=1;

        if (i==1 and line.lstrip().startswith('<relation ')):
                current_file=f3;
                i=2;

	current_file.write(line);

f1.write(line);
f2.write(line);

f1.close()
f2.close()
f3.close()
