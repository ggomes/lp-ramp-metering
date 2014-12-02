clear 
close all

here = fileparts(mfilename('fullpath'));
root = fileparts(here);
outfolder = fullfile(root,'out');

% % load batch parameters (or_demand_vps,split,w,max_metering)
% batch_info=load(fullfile(outfolder,'batch_info.txt'));
% 
% % process the data
% for i=1:81
%     prefix = ['b' num2str(i-1)];
%     B = BatchAnalysis2;
%     B.read(outfolder,prefix);
%     B.or_demand_vps = batch_info(i,1);
%     B.batch_id = i-1;
%     B.split  = batch_info(i,2);
%     B.w  = batch_info(i,3);
%     B.max_metering = batch_info(i,4);
%     save(fullfile(here,prefix),'B')
%     clear B
% end

% % ctm check
% all_ctm_check = nan(121,81);
% for i=0:80
%     prefix = ['b' num2str(i+1)];
%     load(prefix)
%     all_ctm_check(:,i+1) = B.ctm_check;
% end
% surf(all_ctm_check)

[ppt,op]=openppt('phase_plots.ppt');
pause
for i=0:80
    prefix = ['b' num2str(i)];
    clear B
    load(prefix) 
    B.plot_traj;
    addslide(op,'')
    close
end
closeppt(ppt,op)

