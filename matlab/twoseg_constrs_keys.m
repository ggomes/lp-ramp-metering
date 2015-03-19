%% Explicit MPC of LP
% Network Data
clear all;
close all;
clc;
NetworkData;
n0 = [0;0];
l0 = [0;0];
%% Cost construction
n_opt = sdpvar(2,K+1);
f_opt = sdpvar(2,K);
l_opt = sdpvar(2,K+1);
r_opt = sdpvar(2,K);

% cost = sum(sum(n_opt + l_opt)) - etha * (sum(sum(r_opt + f_opt)));
cost = sum(sum(n_opt)) + sum(sum(l_opt)) - etha*(sum(sum(f_opt)) - etha* sum(sum(r_opt)));
cons = [];
% Creating a Map between constraints' index and name
consMap = containers.Map();
% General Constraints
% eps = 0.1
% n0 = 0.5*n1_jam * ones(2,1);
cons_counter = 0;
cons = [cons, n_opt(:,1) == n0];
cons_counter = cons_counter + 1;
consMap('n[0]') = cons_counter;
cons = [cons, l_opt(:,1) == l0];
cons_counter = cons_counter + 1;
consMap('l[0]') = cons_counter;
%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%
for k = 1:K
    [cons, cons_counter, consMap] = add_constraint(cons,k,K_dem,cons_counter,consMap,n_opt,f_opt,l_opt,r_opt);
    N_c = 1-w1;
    %     cons = [cons, r_opt(1,k) <= N_c*(n1_jam-n_opt(1,k))];
    %     cons = [cons, r_opt(2,k) <= N_c*(n2_jam-n_opt(2,k))];
end

DIAGNOSTIC = solvesdp(cons,cost);
%     DIAGNOSTIC = optimize(cons,cost);
if DIAGNOSTIC.problem ~= 0
    error('The problem is infeasible')
end
n_opt = double(n_opt);
l_opt = double(l_opt);
f_opt = double(f_opt);
r_opt = double(r_opt);

[CTM_flow_1, CTM_flow_2, CTM_dens_1, CTM_dens_2] = CTM_check(n_opt,l_opt,f_opt,r_opt);
if CTM_dens_1 == false || CTM_dens_2 == false || CTM_flow_1 == false || CTM_flow_2 == false
    error('The solution is not CTM-like')
end

%%
figure('name','f','position',[0 +100 500 400]);
subplot(2,1,1); hold on; plot(f_opt(1,:)); plot(K_dem,f_opt(1,K_dem),'r*'); grid;
subplot(2,1,2); hold on; plot(f_opt(2,:)); plot(K_dem,f_opt(2,K_dem),'r*'); grid;
%%
figure('name','l','position',[600 +100 500 400]);
subplot(2,1,1); hold on; plot(l_opt(1,:)); plot(K_dem+1,l_opt(1,K_dem+1),'r*'); grid;
subplot(2,1,2); hold on; plot(l_opt(2,:)); plot(K_dem+1,l_opt(2,K_dem+1),'r*'); grid;
%%
figure('name','r','position',[1200 +100 500 400]);
subplot(2,1,1); hold on; plot(r_opt(1,:)); plot(K_dem,r_opt(1,K_dem),'r*'); grid;
subplot(2,1,2); hold on; plot(r_opt(2,:)); plot(K_dem,r_opt(2,K_dem),'r*'); grid;
%% figures
figure('name','n','position',[600 600 500 400]);
subplot(2,1,1); hold on; plot(n_opt(1,:)); plot(K_dem+1,n_opt(1,K_dem+1),'r*'); grid;
subplot(2,1,2); hold on; plot(n_opt(2,:)); plot(K_dem+1,n_opt(2,K_dem+1),'r*'); grid;


%[n_no,l_no,f_no,r_no,cost_no] = CTM_no_control(n0,l0);
%,[0 +100 500 400]
% figure('name','f No Control');
% subplot(2,1,1); hold on; plot(f_no(1,:)); plot(K_dem,f_no(1,K_dem),'r*'); grid;
% subplot(2,1,2); hold on; plot(f_no(2,:)); plot(K_dem,f_no(2,K_dem),'r*'); grid;
%
% figure('name','n No Control');
% subplot(2,1,1); hold on; plot(n_no(1,:)); plot(K_dem+1,n_no(1,K_dem+1),'r*'); grid;
% subplot(2,1,2); hold on; plot(n_no(2,:)); plot(K_dem+1,n_no(2,K_dem+1),'r*'); grid;
%
% figure('name','l No Control');
% subplot(2,1,1); hold on; plot(l_no(1,:)); plot(K_dem+1,l_no(1,K_dem+1),'r*'); grid;
% subplot(2,1,2); hold on; plot(l_no(2,:)); plot(K_dem+1,l_no(2,K_dem+1),'r*'); grid;
%
% figure('name','r No Control');
% subplot(2,1,1); hold on; plot(r_no(1,:)); plot(K_dem,r_no(1,K_dem),'r*'); grid;
% subplot(2,1,2); hold on; plot(r_no(2,:)); plot(K_dem,r_no(2,K_dem),'r*'); grid;

cost_optimal = double(cost);
% display(cost_no);
display(cost_optimal);
% abs(cost_optimal-cost_no)<= 0.001
% display('Main-line Flows are equal?')
% all(all(abs(f_opt-f_no)<= 0.0001))
% display('On-ramp Flows are equal?')
% all(all(abs(r_opt-r_no)<= 0.0001))