clear
close all

here = fileparts(mfilename('fullpath'));
root = fileparts(here);

X = load(fullfile(root,'out','batch_data_10.txt'));

data.run_index = X(:,1);
data.ic = X(:,2:4);
data.demand = X(:,5);
data.ctm_check = X(:,6);
data.tvh = X(:,7);
data.tvm = X(:,8);
data.cost = X(:,9);
clear X

plot_slice(data,3,0.5);


