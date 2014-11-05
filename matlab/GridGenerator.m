x1= [0 0.4 0.8 1];
x2= [0 0.4 0.8 1];
x3= [0 0.4 0.8 1];

dlmwrite('GridPoints.txt',x1);
dlmwrite('GridPoints.txt',x2,'-append');
dlmwrite('GridPoints.txt',x3,'-append');