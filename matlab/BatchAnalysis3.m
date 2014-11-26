classdef BatchAnalysis3 < handle
    %BatchAnalysis Class for processing batch runs
    
    properties        
        num_runs        % total number of runs
        num_segments
        K               % time steps
        demand
        ctm_check
        tvh
        tvm
        cost
        ic
        n
        f
        l
        r
        lp_param           % lp parameters
    end
    
    methods(Access=public)
        
        % read output of the batch runner
        function [this] = read(this,outfolder,prefix)
            
            % Read lp parameters
            this.lp_param = this.load_parameters(outfolder,prefix);
            this.K = this.lp_param.Kdem + this.lp_param.K_cool + 1;
            
            % Read batch summary
            batch_file = fullfile(outfolder,[prefix '.txt']);
            disp(['Reading ' batch_file]);
            X = load(batch_file);            
            this.num_runs = size(X,1);
            this.num_segments = 3; %%%%%
            this.ic = X(:,2:4);
            this.demand = X(:,5);
            this.ctm_check = X(:,6);
            this.tvh = X(:,7);
            this.tvm = X(:,8);
            this.cost = X(:,9);
            
            % read run individual data
            this.n = nan(this.num_segments,this.K,this.num_runs);
            this.f = nan(this.num_segments,this.K,this.num_runs);
            this.l = nan(this.num_segments,this.K,this.num_runs);
            this.r = nan(this.num_segments,this.K,this.num_runs);
            for i=1:this.num_runs
                disp(['Reading ' fullfile(outfolder,sprintf('%s_%d_*.txt',prefix,i))])
                this.f(:,1:this.K-1,i) = load(fullfile(outfolder,sprintf('%s_%d_f.txt',prefix,i)));
                this.l(:,:,i)        = load(fullfile(outfolder,sprintf('%s_%d_l.txt',prefix,i)));
                this.n(:,:,i)        = load(fullfile(outfolder,sprintf('%s_%d_n.txt',prefix,i)));
                this.r(:,1:this.K-1,i) = load(fullfile(outfolder,sprintf('%s_%d_r.txt',prefix,i)));
            end
        end
        
        % plot all trajectories on a single time series plot
        function [] = plot_trajectories(this,run_num)
            if(nargin<2)
                run_num = 1:this.num_runs;
            end
            
            time = (0:this.K-1)*this.lp_param.sim_dt_in_seconds;                
            for i=1:this.num_segments
                
                figure('Position',[103+(i-1)*422    48   403   632])
                subplot(411)
                plot( time , reshape(this.n(i,:,run_num),this.K,length(run_num)) , ...
                    'LineWidth',2)
                grid, ylabel('n')
                subplot(412)
                plot( time , reshape(this.f(i,:,run_num),this.K,length(run_num)) , ...
                    'LineWidth',2)
                grid, ylabel('f')
                subplot(413)
                plot( time , reshape(this.l(i,:,run_num),this.K,length(run_num)) , ...
                    'LineWidth',2)
                grid, ylabel('l')
                subplot(414)
                plot( time , reshape(this.r(i,:,run_num),this.K,length(run_num)) , ...
                    'LineWidth',2)
                grid, ylabel('r')
                
            end
            
 
        end
        
        % plot one slice of some moe
        function [] = plot_single_slice(this,slice_dim,slice_dim_val,moestr)
            
            if(~ismember(slice_dim,[1 2 3]))
                error('bad dim value')
            end
            
            ind =  this.ic(:,slice_dim)==slice_dim_val;
            if(isempty(ind))
                error('no values found')
            end
            
            keep_dims = setdiff([1 2 3],slice_dim);
            [ic1,ic2,Z] = this.cast_for_surf(this.ic(ind,keep_dims),this.(moestr)(ind));
            
            figure
            surf(ic1,ic2,Z)
            xlabel(sprintf('n%d',keep_dims(1)))
            ylabel(sprintf('n%d',keep_dims(2)))
            title(sprintf('%s for n%d=%.2f',moestr,slice_dim,slice_dim_val))
            
        end
 
        % put all slices into a powerpoint file
        function [] = report_all_slices(this,moestr,pptfile)
            
            ic1 = sort(unique(this.ic(:,1)));
            ic2 = sort(unique(this.ic(:,2)));
            ic3 = sort(unique(this.ic(:,3)));
            
            % i1 slices
            for i=1:length(ic1)
                this.plot_single_slice(1,ic1(i),moestr);
            end
            
            % i2 slices
            for i=1:length(ic2)
                this.plot_single_slice(2,ic2(i),moestr);
            end
            
            % i3 slices
            for i=1:length(ic3)
                this.plot_single_slice(3,ic3(i),moestr);
            end
           
        end
        
    end
    
    methods(Access=private)
        
        % return variable n, f, r, or l as a matrix
        function [Z]=get(this,var)
            X={this.run_data.(var)};
            num_seg = size(X{1},1);
            num_time = size(X{1},2);
            num_runs = length(X);
            Z = nan(num_seg,num_time,num_runs);
            for i=1:num_runs
                Z(:,:,i) = X{i};
            end
        end        
        
        % convert ic and moe array to matrices for the "surf" or other such method
        function [ic1,ic2,Z] = cast_for_surf(~,ic,moe)
            ic1 = sort(unique(ic(:,1)));
            ic2 = sort(unique(ic(:,2)));
            nic1 = length(ic1);
            nic2 = length(ic2);
            Z = nan(nic1,nic2);
            for i=1:nic1
                for j=1:nic2
                    ind = ic(:,1)==ic1(i) & ic(:,2)==ic2(j);
                    if(any(ind))
                        Z(i,j) = moe(ind);
                    end
                end
            end
            
        end
        
        function [param]=load_parameters(~,outfolder,prefix)
            param=[];
            thisdir = pwd;
            cd(outfolder)
            eval(sprintf('param=%s();',prefix));
            cd(thisdir)
        end
    end
    
end
