clear 
% close all

% here = fileparts(mfilename('fullpath'));
% root = fileparts(here);
% outfolder = fullfile(root,'out');
% 
% B = BatchAnalysis2;
% B.read(outfolder,'batch_data_10');

load aaa

% B.plot_moe('cost');
% B.plot_trajectories()

n = (B.Kdem-1)*B.num_runs;
R = reshape(B.r(2,1:B.Kdem-1,:),1,n);
L = reshape(B.l(2,1:B.Kdem-1,:),1,n);

ind = (L==0 & R>0); % | (L==R);
R(ind) = 2;

N1 = reshape(B.n(1,1:B.Kdem-1,:),1,n);
N2 = reshape(B.n(2,1:B.Kdem-1,:),1,n);

% pointsize = 8;
% minR = min(R);
% maxR = max(R);
% [counts, ccode] = histc(R,linspace(minR,maxR,3));
% scatter3(N1,N2,R,pointsize,ccode(:));
% colormap([1 0 0;0 1 0; 0 0 1]);

figure
ind = R<0.01;
scatter3(N1(ind),N2(ind),R(ind),'k.'), hold on
ind = R>=0.01 & R<2;
scatter3(N1(ind),N2(ind),R(ind),'b.')
scatter3(N1(ind),N2(ind),L(ind),'mo','LineWidth',2)
ind = R>=2;
scatter3(N1(ind),N2(ind),R(ind),'r.')
xlabel('n1')
ylabel('n2')

