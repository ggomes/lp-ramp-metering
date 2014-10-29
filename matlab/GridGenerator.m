x1= [0 0.5 1];
x2= 0:0.01:1;
x3= 0:0.01:1;

dlmwrite('GridPoints.txt',x1);
dlmwrite('GridPoints.txt',x2,'-append');
dlmwrite('GridPoints.txt',x3,'-append');