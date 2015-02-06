%% Explicit MPC of LP
NetworkData;
%[n1g,n2g]= meshgrid(0:0.5:n1_jam,0:0.5:n2_jam);
n1g = 0:0.5:n1_jam;
n2g = 0:0.5:n2_jam;
r1_tilda = [];
r2_tilda = [];
r1_opt = [];
r2_opt = [];
r1_no_Control = [];
r2_no_Control = [];
l0 = [10;10];

for ii = 1:size(n1g,2)
    for jj = 1:size(n2g,2)
        
        n0 = [n1g(ii);n2g(jj)];
        %% Cost construction
        
        n_opt = sdpvar(2,K+1);
        f_opt = sdpvar(2,K);
        l_opt = sdpvar(2,K+1);
        r_opt = sdpvar(2,K);
        
        cost = sum(sum(n_opt)) + sum(sum(l_opt)) - etha*(sum(sum(f_opt)) - etha* sum(sum(r_opt)));
        cons = [];
        % General Constraints
        eps = 0.1;
        cons = [cons, n_opt(:,1) == n0];
        
        cons = [cons, f_opt <= f1_bar];
        cons = [cons, 0<= r_opt <=r1_bar];
        %%%%%%%%%%%%%%%%%%%%%%%%%%%%
        cons = [cons, l_opt(:,1)==l0];
        %%%%%%%%%%%%%%%%%%%%%%%%%%%%
        for i = 1:K
            if i>K_dem
                d1 = 0; d2 = 0; d3 = 0;
            end;
            % Conservations
            cons = [cons, n_opt(1,i+1) == n_opt(1,i) - (beta1_bar^-1)*f_opt(1,i) + d1 + r_opt(1,i)];
            cons = [cons, n_opt(2,i+1) == n_opt(2,i) + f_opt(1,i) + r_opt(2,i) - (beta2_bar^-1)*f_opt(2,i)];
            cons = [cons, l_opt(1,i+1) == l_opt(1,i) - r_opt(1,i)+ d2];
            cons = [cons, l_opt(2,i+1) == l_opt(2,i) - r_opt(2,i)+ d3];
            % MainLine FreeFlow
            cons = [cons, f_opt(1,i) <= beta1_bar*v1*n_opt(1,i) + beta1_bar*v1*Gamma*d1 + beta1_bar*v1*Gamma*r_opt(1,i)];%?
            cons = [cons, f_opt(2,i) <= beta2_bar*v2*n_opt(2,i) +  beta2_bar*v2*Gamma*r_opt(2,i)];
            % MainLine Congestion
            cons = [cons, f_opt(1,i) <= w2*n2_jam-w2*n_opt(2,i)-w2*Gamma*r_opt(2,i)];
            % OR Flow
            cons = [cons, r_opt(1,i) <= d2 + l_opt(1,i)];
            cons = [cons, r_opt(2,i) <= d3 + l_opt(2,i)];
            % Ramp Flow Constraints
            % N_c = 1-w1;
            cons = [cons, r_opt(1,i) <= N_c*(n1_jam-n_opt(1,i))];
            cons = [cons, r_opt(2,i) <= N_c*(n2_jam-n_opt(2,i))];
        end
        
        DIAGNOSTIC = solvesdp(cons,cost);
        %%
        n_opt = double(n_opt);
        l_opt = double(l_opt);
        f_opt = double(f_opt);
        r_opt = double(r_opt);
        %% Plots
        
%         figure('name','f','position',[0 +100 500 400]);
%         subplot(2,1,1); hold on; plot(f_opt(1,:)); plot(K_dem,f_opt(1,K_dem),'r*'); grid;
%         subplot(2,1,2); hold on; plot(f_opt(2,:)); plot(K_dem,f_opt(2,K_dem),'r*'); grid;
%         
        %l_opt = double(l_opt);
%         figure('name','l','position',[600 +100 500 400]);
%         subplot(2,1,1); hold on; plot(l_opt(1,:)); plot(K_dem,l_opt(1,K_dem),'r*'); grid;
%         subplot(2,1,2); hold on; plot(l_opt(2,:)); plot(K_dem,l_opt(2,K_dem),'r*'); grid;
%         
        
%         figure('name','r','position',[1200 +100 500 400]);
%         subplot(2,1,1); hold on; plot(r_opt(1,:)); plot(K_dem,r_opt(1,K_dem),'r*'); grid;
%         subplot(2,1,2); hold on; plot(r_opt(2,:)); plot(K_dem,r_opt(2,K_dem),'r*'); grid;
%         
%         figure('name','n','position',[600 600 500 400]);
%         subplot(2,1,1); hold on; plot(n_opt(1,:)); plot(K_dem,n_opt(1,K_dem),'r*'); grid;
%         subplot(2,1,2); hold on; plot(n_opt(2,:)); plot(K_dem,n_opt(2,K_dem),'r*'); grid;
%         %% CTM Behavior
        [CTM_flow_1, CTM_flow_2, CTM_dens_1, CTM_dens_2] = CTM_check(n_opt,l_opt,f_opt,r_opt);
        if CTM_dens_1 == false || CTM_dens_2 == false || CTM_flow_1 == false || CTM_flow_2 == false
            error('The solution is not CTM-like')
        end
        %% No Control
        n0_no_control = n0;
        [n,l,f,r,cost_no_Control] = CTM_no_control(n0_no_control,l0);
        % %% Plotting
%         figure('name','f No Control','position',[0 +100 500 400]);
%         subplot(2,1,1); hold on; plot(f(1,:)); plot(K_dem,f(1,K_dem),'r*'); grid;
%         subplot(2,1,2); hold on; plot(f(2,:)); plot(K_dem,f(2,K_dem),'r*'); grid;
%                
%         figure('name','l No Control','position',[600 +100 500 400]);
%         subplot(2,1,1); hold on; plot(l(1,:)); plot(K_dem,l(1,K_dem),'r*'); grid;
%         subplot(2,1,2); hold on; plot(l(2,:)); plot(K_dem,l(2,K_dem),'r*'); grid;
%         
%         figure('name','r No Control','position',[1200 +100 500 400]);
%         subplot(2,1,1); hold on; plot(r(1,:)); plot(K_dem,r(1,K_dem),'r*'); grid;
%         subplot(2,1,2); hold on; plot(r(2,:)); plot(K_dem,r(2,K_dem),'r*'); grid;
%         
%         figure('name','n No Control','position',[600 600 500 400]);
%         subplot(2,1,1); hold on; plot(n(1,:)); plot(K_dem,n(1,K_dem),'r*'); grid;
%         subplot(2,1,2); hold on; plot(n(2,:)); plot(K_dem,n(2,K_dem),'r*'); grid;
        %% Saving Results
        r1_tilda(ii,jj) = r_opt(1,1) - r(1,1);
        r2_tilda(ii,jj) = r_opt(2,1) - r(2,1);
        r1_opt(ii,jj) = r_opt(1,1);
        r2_opt(ii,jj) = r_opt(2,1);
        r1_no_Control(ii,jj) = r(1,1);
        r2_no_Control(ii,jj) = r(2,1);
        
    end
end

figure();
surf(n1g,n2g,r1_tilda);
title(strcat('r_1_{opt}-r_1-beta2-',num2str(beta2)));

figure()
surf(n1g,n2g,r2_tilda);
title(strcat('r_2_{opt}-r_2-beta2-',num2str(beta2)));

figure();
surf(n1g,n2g,r1_opt);
title(strcat('r_1_{opt}-beta2-',num2str(beta2)));

figure()
surf(n1g,n2g,r2_opt);
title(strcat('r_2_{opt}-beta2-',num2str(beta2)));

figure();
surf(n1g,n2g,r1_no_Control);
title(strcat('r_1_{No Control}-beta2-',num2str(beta2)));

figure()
surf(n1g,n2g,r2_no_Control);
title(strcat('r_2_{No Control}-beta2-',num2str(beta2)));





