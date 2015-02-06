%% Explicit MPC of LP
% Network Data
clear all;
close all;
clc;
NetworkData;
n0 = [0;0];
l0 = [10;10];
%% Cost construction
n = sdpvar(2,K+1);
f = sdpvar(2,K);
l = sdpvar(2,K+1);
r = sdpvar(2,K);

cost = sum(sum(n)) + sum(sum(l)) - etha*(sum(sum(f)) - etha* sum(sum(r)));
cons = [];
% General Constraints
% eps = 0.1
% n0 = 0.5*n1_jam * ones(2,1);
if MP == 0
    cons = [cons, n(:,1) == n0];
elseif MP == 1
    cons = [cons, 0 <= n(:,1) <= n1_jam];
end;
cons = [cons, f <= f1_bar];
cons = [cons, 0<= r <= r1_bar];
%%%%%%%%%%%%%%%%%%%%%%%%%%%%
cons = [cons, l(:,1)==l0];
%%%%%%%%%%%%%%%%%%%%%%%%%%%%
for i = 1:K
    if i>K_dem
        d1 = 0; d2 = 0; d3 = 0;
    end;
    % Conservations
    cons = [cons, n(1,i+1) == n(1,i) - (beta1_bar^-1)*f(1,i) + d1 + r(1,i)];
    cons = [cons, n(2,i+1) == n(2,i) + f(1,i) + r(2,i) - (beta2_bar^-1)*f(2,i)];
    cons = [cons, l(1,i+1) == l(1,i) - r(1,i)+ d2];
    cons = [cons, l(2,i+1) == l(2,i) - r(2,i)+ d3];
    % MainLine FreeFlow
    cons = [cons, f(1,i) <= beta1_bar*v1*n(1,i) + beta1_bar*v1*Gamma*d1 + beta1_bar*v1*Gamma*r(1,i)];%?
    cons = [cons, f(2,i) <= beta2_bar*v2*n(2,i) +  beta2_bar*v2*Gamma*r(2,i)];
    % MainLine Congestion
    cons = [cons, f(1,i) <= w2*n2_jam-w2*n(2,i)-w2*Gamma*r(2,i)];
    % OR Flow
    cons = [cons, r(1,i) <= v_ramp*(d2 + l(1,i))];
    cons = [cons, r(2,i) <= v_ramp*(d3 + l(2,i))];
    % Ramp Flow Constraints
    N_c = 1-w1;
%     cons = [cons, r(1,i) <= N_c*(n1_jam-n(1,i))];
%     cons = [cons, r(2,i) <= N_c*(n2_jam-n(2,i))];
end


if MP == 1
    MPLP = Opt(cons,cost,n(:,1),r(:,1));
    solution = MPLP.solve();
    % solution.xopt.merge('obj');
    % solution.xopt.merge('primal');
    % figure; hold on; for i=1:N solution.xopt.Set(i).plot; end;
    %% Controller
    N = solution.xopt.Num
    for i=1:N
        F = solution.xopt.Set(i).Functions('primal').F;
        g = solution.xopt.Set(i).Functions('primal').g;
        F11(i) = F(1,1);    F12(i) = F(1,2);   % F13(i) = F(1,3);
        F21(i) = F(2,1);    F22(i) = F(2,2);   % F23(i) = F(2,3);
        g11(i) = g(1,1);    g21(i) = g(2,1);
    end;
    figure;
    subplot(2,3,1); plot(F11); title('F_{11}'); grid; xlim([1 N]); h = findobj(gcf,'type','line'); set(h,'linewidth',2);
    subplot(2,3,2); plot(F12); title('F_{12}'); grid; xlim([1 N]); h = findobj(gcf,'type','line'); set(h,'linewidth',2);
    %subplot(2,3,3); plot(F13); title('F_{13}');grid; xlim([1 N]); h = findobj(gcf,'type','line'); set(h,'linewidth',2);
    subplot(2,3,4); plot(F21); title('F_{21}');grid; xlim([1 N]); h = findobj(gcf,'type','line'); set(h,'linewidth',2);
    subplot(2,3,5); plot(F22); title('F_{22}');grid; xlim([1 N]); h = findobj(gcf,'type','line'); set(h,'linewidth',2);
    %subplot(2,3,6); plot(F23); title('F_{23}');grid; xlim([1 N]); h = findobj(gcf,'type','line'); set(h,'linewidth',2);
    figure;
    subplot(2,1,1); plot(g11); title('g_{1}');grid; xlim([1 N]); h = findobj(gcf,'type','line'); set(h,'linewidth',2);
    subplot(2,1,2); plot(g21); title('g_{2}');grid; xlim([1 N]); h = findobj(gcf,'type','line'); set(h,'linewidth',2);
    
    for i=1:N
        R_equal{i} = find((abs(F11-F11(i))<1e-3)+(abs(F12-F12(i))<1e-3)+(abs(F21-F21(i))<1e-3)+(abs(F22-F22(i))<1e-3)+(abs(g11-g11(i))<1e-3)+(abs(g21-g21(i))<1e-3) == 6);
        %         R_equal{i} = find((F11==F11(i))+(F12==F12(i))+(F13==F13(i))+(F21==F21(i))+(F22==F22(i))+(F23==F23(i))+(g11==g11(i))+(g21==g21(i))==8);
    end;
    R_equal_indx = [1:N; ones(1,N)];
    
    j = 1; counter = 0;
    while j<=N
        counter = counter+1;
        comb_reg.set(counter).r = R_equal{j};
        comb_reg.set(counter).region = solution.xopt.Set(R_equal{j});
        comb_reg.set(counter).F = solution.xopt.Set(R_equal{j}(1)).Functions('primal').F;
        comb_reg.set(counter).g = solution.xopt.Set(R_equal{j}(1)).Functions('primal').g;
        for i=1:size(R_equal{j},2)
            R_equal_indx(2,R_equal{j}(i)) = 0;
        end;
        %         j = R_equal{j}(end)+1;
        j = min(find(R_equal_indx(2,:)>0));
                figure; comb_reg.set(counter).region.plot;
    end;
    comb_reg.N = counter
    %%%%%
    %%
    figure; 
    for i = 1:counter
        if mod(counter,3) == 0 N_plt = (counter-mod(counter,3))/3;
        else
            N_plt = (counter-mod(counter,3))/3+1;
        end;
        subplot(3,N_plt,i); comb_reg.set(i).region.plot;
    end;
    %%
for i = 1:counter    
    clear temp_Xmin temp_Xmax temp_Ymin temp_Ymax
    N_r = size(comb_reg.set(i).region,1);
    for j = 1:N_r
        V = comb_reg.set(i).region(j).V;
        temp_Xmin(j) = min(V(:,1)); temp_Xmax(j) = max(V(:,1));
        temp_Ymin(j) = min(V(:,2)); temp_Ymax(j) = max(V(:,2));
    end;
    Xmin(i) = min(temp_Xmin); Xmax(i) = max(temp_Xmax);
    Ymin(i) = min(temp_Ymin); Ymax(i) = max(temp_Ymax);   
end;
Regions = 1:counter;
region_sel = find(abs(Xmin-min(Xmin))<1e-3 & abs(Ymin-min(Ymin))<1e-3);
Regions(region_sel) = 0;
% Orders = [5; 3; 1; 7; 4; 2; 8; 6];
% Orders = Orders(end:-1:1)
% %% figures (sorted)
%     figure; 
%     for i = 1:counter
%         k = Orders(i);
%         subplot(3,mod(counter,3)+1,i); comb_reg.set(k).region.plot;
%     end;
    
    %%
    % finding V represntation of the whole region
    clear R F_cnt g_cnt R_cnt
    for i = 1:counter
        nV0 = 0;
        clear V
        for j = 1:size(comb_reg.set(i).region,1)
            nV1 = nV0+size(comb_reg.set(i).region(j).V,1);
            V(nV0+1:nV1,:) = comb_reg.set(i).region(j).V;
            nV0 = nV1;
        end;
        R(i) = Polyhedron('V',V);
        F_cnt(:,:,i) = comb_reg.set(i).F;
        g_cnt(:,:,i) = comb_reg.set(i).g;
        R_cnt(i) = F_cnt(:,:,i)*R(i)+g_cnt(:,:,i);
    end;
    %%%%%%%%
    ncr = f1_bar/v1;
    %%%%%%%%
    
    figure; hold on; R.plot; line([ncr;ncr],[0;ncr]); line([ncr,n1_jam],[ncr,ncr]); line([0,ncr],[n2_jam, ncr]); h = findobj(gcf,'type','line'); set(h,'linewidth',2); set(h,'color','k'); set(h,'LineStyle','-.');
    figure; R_cnt.plot;
        figure;
    for i = 1:size(R,2)
        R_size = size(R,2);
        if mod(R_size,3) == 0 N_plt = (R_size-mod(R_size,3))/3;
        else
            N_plt = (R_size-mod(R_size,3))/3+1
        end;
        subplot(3,N_plt,i); R(i).plot; xlim([0 n1_jam]); ylim([0 n1_jam]);
    end;
    
        figure; 
    for i = 1:size(R,2)
        R_cnt_size = size(R_cnt,2);
        if mod(R_cnt_size,3) == 0 N_plt = (R_cnt_size-mod(R_cnt_size,3))/3;
        else
            N_plt = (R_cnt_size-mod(R_cnt_size,3))/3+1
        end;
        subplot(3,N_plt,i); R_cnt(i).plot; xlim([-0.1 1.2*r1_bar]); ylim([-0.1 1.2*r1_bar]);
    end;
    
    %%
    
    %%%%%
    figure; hold on; for i=1:comb_reg.N comb_reg.set(i).region.plot; end;
    % Cost
        F_obj = solution.xopt.Set(i).Functions('obj').F;
        g_obj = solution.xopt.Set(i).Functions('obj').g;
        F11_obj(i) = F_obj(1,1);    F12_obj(i) = F_obj(1,2);    %F13_obj(i) = F_obj(1,3);
        g11_obj(i) = g_obj(1,1);
%     end;
    figure;
        subplot(1,2,1); plot(F11_obj); grid; xlim([1 N]); h = findobj(gcf,'type','line'); set(h,'linewidth',2);
        subplot(1,2,2); plot(F12_obj); grid; xlim([1 N]); h = findobj(gcf,'type','line'); set(h,'linewidth',2);
    %subplot(1,3,3); plot(F13_obj); grid; xlim([1 N]); h = findobj(gcf,'type','line'); set(h,'linewidth',2);
    figure;
        plot(g11_obj); grid; xlim([1 N]); h = findobj(gcf,'type','line'); set(h,'linewidth',2);
end;

if MP == 0
    DIAGNOSTIC = solvesdp(cons,cost);
    double(n)
    double(l)
    %%
    n = double(n)
    l = double(l)
    %% condition for demand
    K = K_dem+K_cool;
    for i =1:2 n_grd(i,:) = gradient(n(i,:)); end;
    l_dem = 2;
    for i = 1:2
        if (abs(n_grd(i,K_dem-l_dem:K_dem-1))<0.3)
            check_dem = 1;
        else
            check_dem = 0;
        end;
    end;
    %% condition for cool down
    l_cool = 2;
    for i = 1:2
        if (abs(n_grd(i,K-l_cool+1:K))<0.3) & (n(i,K-l_dem+1:K)<0.3)
            check_cool = 1;
        else
            check_cool = 0;
        end;
    end;
    %% all conditions
    if (check_dem == 1) && (check_cool == 1)
        check =1;
    else
        check = 0;
    end;
    
    check_dem
    check_cool
    check
    %%
    f = double(f);
    figure('name','f','position',[0 +100 500 400]);
    subplot(2,1,1); hold on; plot(f(1,:)); plot(K_dem,f(1,K_dem),'r*'); grid;
    subplot(2,1,2); hold on; plot(f(2,:)); plot(K_dem,f(2,K_dem),'r*'); grid;
    %%
    l = double(l);
    figure('name','l','position',[600 +100 500 400]);
    subplot(2,1,1); hold on; plot(l(1,:)); plot(K_dem,l(1,K_dem),'r*'); grid;
    subplot(2,1,2); hold on; plot(l(2,:)); plot(K_dem,l(2,K_dem),'r*'); grid;
    %%
    r = double(r);
    figure('name','r','position',[1200 +100 500 400]);
    subplot(2,1,1); hold on; plot(r(1,:)); plot(K_dem,r(1,K_dem),'r*'); grid;
    subplot(2,1,2); hold on; plot(r(2,:)); plot(K_dem,r(2,K_dem),'r*'); grid;
    %% figures
    figure('name','n','position',[600 600 500 400]);
    subplot(2,1,1); hold on; plot(n(1,:)); plot(K_dem,n(1,K_dem),'r*'); grid;
    subplot(2,1,2); hold on; plot(n(2,:)); plot(K_dem,n(2,K_dem),'r*'); grid;
    
end;

[n_no,l_no,f_no,r_no,cost_no] = CTM_no_control(n0,l0);
[CTM_flow_1, CTM_flow_2, CTM_dens_1, CTM_dens_2] = CTM_check(n,l,f,r);
if CTM_dens_1 == false || CTM_dens_2 == false || CTM_flow_1 == false || CTM_flow_2 == false
    error('The solution is not CTM-like')
end
display(cost_no);
double(cost);