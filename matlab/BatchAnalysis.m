classdef BatchAnalysis < handle
    %BatchAnalysis Class for processing batch runs
    
    properties        
        num_runs        % total number of runs
        K               % time steps
        run_data        % structure with all the data
        all_ic          % all initial conditions
    end
    
    methods(Access=public)
        
        % read output of the batch runner
        function [this] = read(this,outfolder,prefix)
            batch_file = fullfile(outfolder,[prefix '.txt']);
            disp(['Reading data from ' batch_file]);
            X = load(batch_file);
            this.num_runs = size(X,1);
            this.run_data = repmat( struct( 'ic',[],  ...
                                            'demand',nan,  ...
                                            'ctm_check',nan,  ...
                                            'tvh',nan, ...
                                            'tvm',nan,  ...
                                            'cost',nan, ...
                                            'n',[], ...
                                            'f',[], ...
                                            'l',[], ...
                                            'r',[]),  ...
                                    1 , this.num_runs );
                                
            for i=1:this.num_runs
                this.run_data(i).ic = X(i,2:4);
                this.run_data(i).demand = X(i,5);
                this.run_data(i).ctm_check = X(i,6);
                this.run_data(i).tvh = X(i,7);
                this.run_data(i).tvm = X(i,8);
                this.run_data(i).cost = X(i,9);
                disp(['Reading ' fullfile(outfolder,sprintf('%s_%d_*.txt',prefix,i))])
                this.run_data(i).f = load(fullfile(outfolder,sprintf('%s_%d_f.txt',prefix,i)));
                this.run_data(i).l = load(fullfile(outfolder,sprintf('%s_%d_l.txt',prefix,i)));
                this.run_data(i).n = load(fullfile(outfolder,sprintf('%s_%d_n.txt',prefix,i)));
                this.run_data(i).r = load(fullfile(outfolder,sprintf('%s_%d_r.txt',prefix,i)));
                if(i==1)
                    this.K = size(this.run_data(1).n,2);
                end
            end
            
            % keep ic in more convenient form
            this.all_ic = cell2mat({this.run_data.ic}');
        end
        
        % plot all trajectories on a single time series plot
        function [] = plot_trajectories(this)
            x = {'n','f','r','l'};
            figure('Position',[772    62   560   617])
            for i=1:length(x)
                subplot(4,1,i)
                plot((0:this.K-1),this.get(x{i}))
                grid
                ylabel(x{i})
            end
        end
        
        % plot one slice of some moe
        function [] = plot_single_slice(this,slice_dim,slice_dim_val,moestr)
            
            if(~ismember(slice_dim,[1 2 3]))
                error('bad dim value')
            end
            
            ind =  this.all_ic(:,slice_dim)==slice_dim_val;
            if(isempty(ind))
                error('no values found')
            end
            
            keep_dims = setdiff([1 2 3],slice_dim);
            [ic1,ic2,Z] = this.cast_for_surf(ic(ind,keep_dims),[this.run_data(ind).(moestr)]);
            
            figure
            surf(ic1,ic2,Z)
            xlabel(num2str(keep_dims(1)))
            ylabel(num2str(keep_dims(2)))
            
        end
 
        % put all slices into a powerpoint file
        function [] = report_slices(this)
            disp('not done')
        end
        
    end
    
    methods(Access=private)
        
        % return variable n, f, r, or l as a matrix
        function [A]=get(this,var)
            A = cell2mat( cellfun(@(x) sum(x,1),{this.run_data.(var)},'UniformOutput',false)' );
            % add final nan column for flow variables
            if(strcmp(var,'f') || strcmp(var,'r'))
                A = [A nan(size(A,1),1)];
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
                    ind = ic(:,1)==ic1(i) & ic(:,2)==ic2(i);
                    if(any(ind))
                        Z(i,j) = moe(ind);
                    end
                end
            end
            
        end
        
    end
    
end
