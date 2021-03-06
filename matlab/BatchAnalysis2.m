classdef BatchAnalysis2 < handle
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
        Kdem
        Kcool
        dt
        
        batch_id
        or_demand_vps 
        split 
        w 
        max_metering
    end
    
    methods(Access=public)
        
        % read output of the batch runner
        function [this] = read(this,outfolder,prefix)
            
            % Read lp parameters
            param = this.load_parameters(outfolder,prefix);
            this.Kdem = param.Kdem;
            this.Kcool = param.K_cool;
            this.dt = param.sim_dt_in_seconds;
            this.K = this.Kdem + this.Kcool + 1;
            
            % Read batch summary
            batch_file = fullfile(outfolder,[prefix '.txt']);
            disp(['Reading ' batch_file]);
            X = load(batch_file);
            this.num_runs = size(X,1);
            this.num_segments = 2; %%%%%
            this.ic = X(:,2:3);
            this.demand = X(:,4);
            this.ctm_check = X(:,5);
            this.tvh = X(:,6);
            this.tvm = X(:,7);
            this.cost = X(:,8);
            
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
        function [] = plot_time(this,run_num)
            if(nargin<2)
                run_num = 1:this.num_runs;
            end
            
            time = (0:this.K-1)*this.dt;
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
        
        function [] = plot_traj(this,run_num)
            
            if(nargin<2)
                run_num = 1:this.num_runs;
            end
            
            m = (this.Kdem-1)*length(run_num);
            R = reshape(this.r(2,1:this.Kdem-1,run_num),1,m);
            L = reshape(this.l(2,1:this.Kdem-1,run_num),1,m);
            
            ind = (L==0 & R>0); % | (L==R);
            R(ind) = 2;
            
            N1 = reshape(this.n(1,1:this.Kdem-1,run_num),1,m);
            N2 = reshape(this.n(2,1:this.Kdem-1,run_num),1,m);
            
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
            view(0,90);
            
            title(sprintf('batch id=%d, ordem=%.2f, split=%.1f, maxmeter=%d',this.batch_id,this.or_demand_vps,this.split,this.max_metering))
            
        end
        
        % plot one slice of some moe
        function [] = plot_moe(this,moestr)
            [ic1,ic2,Z] = this.cast_for_surf(this.ic,this.(moestr));
            figure
            surf(ic1,ic2,Z)
            xlabel('n1')
            ylabel('n2')
            title(moestr)
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
