clear
close all

here = fileparts(mfilename('fullpath'));
root = fileparts(here);

% B = BatchAnalysis;
% B.read(fullfile(root,'out'),'batch_data_10');

load aaa

B.plot_single_slice(1,0,'tvh');
B.plot_trajectories