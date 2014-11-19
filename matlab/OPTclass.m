%% Explicit MPC of LP
% Network Data
clear all;
clc;
sim_dt = 3;
gamma = 1;

beta1 = 0.1;
beta2 = 0;
beta3 = 0;

d1 = 0.5 * sim_dt;
d2 = 0.5 * sim_dt;
d3 = 0.5 * sim_dt;

beta1_bar = 1-beta1;
beta2_bar = 1-beta2;
beta3_bar = 1-beta3;

v1 = 0.24135 * sim_dt;
v2 = 0.24135 * sim_dt;
v3 = 0.24135 * sim_dt;

w1 = 0.06034 * sim_dt;
w2 = 0.06034 * sim_dt;
w3 = 0.06034 * sim_dt;

n1_jam = 11.51003;
n2_jam = 11.51003;
n3_jam = 11.51003;
l1_jam = inf;
l2_jam = inf;

f1_bar = 0.5556 * sim_dt;
f2_bar = 0.5556 * sim_dt;
f3_bar = 0.5556 * sim_dt;
r1_bar = 0.5556 * sim_dt;
r2_bar = 0.5556 * sim_dt;

etha = 0.1;

K_dem = 5;
K_cool = 5;
K = K_dem + K_cool;

%% Cost construction 
n = sdpvar(3,K+1);
f = sdpvar(3,K);
l = sdpvar(2,K+1);
r = sdpvar(2,K);

cost = sum(sum(n)) + sum(sum(l)) - etha*(sum(sum(f)) + sum(sum(r)));
cons = [];
% General Constraints
cons = [cons, 0 <= n(:,1) <= n1_jam];
cons = [cons, f <=f1_bar];
cons = [cons, 0<= r <=r1_bar];
% % Constraints at time 0
% % Conservations
% cons = [cons, n(1,2) == n(1,1) - (beta1_bar^-1)*f(1,1) + n(1,1) + d1];
% cons = [cons, n(2,2) == n(2,1) + f(1,1) + r(1,1)- (beta2_bar^-1)*f(2,1) + n(2,1)];
% cons = [cons, n(3,2) == n(3,1) + f(2,1) + r(2,1)- (beta3_bar^-1)*f(3,1) + n(3,1)];
% cons = [cons, l(1,2) == l(1,1) - r(1,1)+ d2 + l(1,1)];
% cons = [cons, l(2,2) == l(2,1) - r(2,1)+ d3 + l(2,1)];
% % MainLine FreeFlow
% cons = [cons, f(1,1) <= beta1_bar*v1*n(1,1) + beta1_bar*v1*gamma*d1];
% cons = [cons, f(2,1) <= beta2_bar*v2*gamma*r(1,1)+beta2_bar*v2*n(2,1)];
% cons = [cons, f(3,1) <= beta3_bar*v3*gamma*r(2,1)+beta3_bar*v3*n(3,1)];
% % MainLine Congestion
% cons = [cons, f(1,1) + w2*gamma*r(1,1)<= w2*n2_jam-w2*n(2,1)];
% cons = [cons, f(2,1) + w3*gamma*r(2,1)<= w3*n3_jam-w3*n(3,1)];
% % OR Flow
% cons = [cons, r(1,1) <= d2 + l(1,1)];
% cons = [cons, r(2,1) <= d3 + l(2,1)];
% Constraints Construction
% cons = [];
% cons = [cons, l(:,1)==[0;0]];
% for i = 1:K
%     % Conservations
%     cons = [cons, n(1,i+1) == n(1,i) - (beta1_bar^-1)*f(1,i) + d1];
%     cons = [cons, n(2,i+1) == n(2,i) + f(2,i) + r(1,i)- (beta2_bar^-1)*f(2,i)];
%     cons = [cons, n(3,i+1) == n(3,i) + f(3,i) + r(2,i)- (beta3_bar^-1)*f(3,i)];
%     cons = [cons, l(1,i+1) == l(1,i) - r(1,i)+ d2];
%     cons = [cons, l(2,i+1) == l(2,i) - r(2,i)+ d3];
%     % MainLine FreeFlow
%     cons = [cons, f(1,i) <= beta1_bar*v1*n(1,i) + beta1_bar*v1*gamma*d1];
%     cons = [cons, f(2,i) <= beta2_bar*v2*gamma*r(1,i)+beta2_bar*v2*n(2,i)];
%     cons = [cons, f(3,i) <= beta3_bar*v3*gamma*r(2,i)+beta3_bar*v3*n(3,i)];
%     % MainLine Congestion
%     cons = [cons, f(1,i) + w2*gamma*r(1,i)<= w2*n2_jam-w2*n(2,i)];
%     cons = [cons, f(2,i) + w3*gamma*r(2,i)<= w3*n3_jam-w3*n(3,i)];
%     % OR Flow
%     cons = [cons, r(1,i) <= d2 + l(1,i)];
%     cons = [cons, r(2,i) <= d3 + l(2,i)];
% end

for i = 1:K_dem
    % Conservations
    cons = [cons, n(1,i+1) == n(1,i) - (beta1_bar^-1)*f(1,i) + d1];
    cons = [cons, n(2,i+1) == n(2,i) + f(2,i) + r(1,i)- (beta2_bar^-1)*f(2,i)];
    cons = [cons, n(3,i+1) == n(3,i) + f(3,i) + r(2,i)- (beta3_bar^-1)*f(3,i)];
    cons = [cons, l(1,i+1) == l(1,i) - r(1,i)+ d2];
    cons = [cons, l(2,i+1) == l(2,i) - r(2,i)+ d3];
    % MainLine FreeFlow
    cons = [cons, f(1,i) <= beta1_bar*v1*n(1,i) + beta1_bar*v1*gamma*d1];
    cons = [cons, f(2,i) <= beta2_bar*v2*gamma*r(1,i)+beta2_bar*v2*n(2,i)];
    cons = [cons, f(3,i) <= beta3_bar*v3*gamma*r(2,i)+beta3_bar*v3*n(3,i)];
    % MainLine Congestion
    cons = [cons, f(1,i) + w2*gamma*r(1,i)<= w2*n2_jam-w2*n(2,i)];
    cons = [cons, f(2,i) + w3*gamma*r(2,i)<= w3*n3_jam-w3*n(3,i)];
    % OR Flow
    cons = [cons, r(1,i) <= d2 + l(1,i)];
    cons = [cons, r(2,i) <= d3 + l(2,i)];
end

for i = (K_dem+1):K
    % Conservations
    cons = [cons, n(1,i+1) == n(1,i) - (beta1_bar^-1)*f(1,i)];
    cons = [cons, n(2,i+1) == n(2,i) + f(2,i) + r(1,i)- (beta2_bar^-1)*f(2,i)];
    cons = [cons, n(3,i+1) == n(3,i) + f(3,i) + r(2,i)- (beta3_bar^-1)*f(3,i)];
    cons = [cons, l(1,i+1) == l(1,i) - r(1,i)];
    cons = [cons, l(2,i+1) == l(2,i) - r(2,i)];
    % MainLine FreeFlow
    cons = [cons, f(1,i) <= beta1_bar*v1*n(1,i) + beta1_bar*v1*gamma*d1];
    cons = [cons, f(2,i) <= beta2_bar*v2*gamma*r(1,i)+beta2_bar*v2*n(2,i)];
    cons = [cons, f(3,i) <= beta3_bar*v3*gamma*r(2,i)+beta3_bar*v3*n(3,i)];
    % MainLine Congestion
    cons = [cons, f(1,i) + w2*gamma*r(1,i)<= w2*n2_jam-w2*n(2,i)];
    cons = [cons, f(2,i) + w3*gamma*r(2,i)<= w3*n3_jam-w3*n(3,i)];
    % OR Flow
    cons = [cons, r(1,i) <= l(1,i)];
    cons = [cons, r(2,i) <= l(2,i)];
end

MPLP = Opt(cons,cost,n(:,1),r(:,1));
solution = MPLP.solve();
% save('10timeStepswithKcool.mat');
% solution.xopt.merge('obj');
% solution.xopt.merge('primal');
N = solution.xopt.Num
% figure; hold on; for i=1:N solution.xopt.Set(i).plot; end;

%% Controller
for i=1:N
    F = solution.xopt.Set(i).Functions('primal').F;
    g = solution.xopt.Set(i).Functions('primal').g;
    F11(i) = F(1,1);    F12(i) = F(1,2);    F13(i) = F(1,3);
    F21(i) = F(2,1);    F22(i) = F(2,2);    F23(i) = F(2,3);
    g11(i) = g(1,1);    g21(i) = g(2,1);
end;
figure; 
    subplot(2,3,1); plot(F11); title('F_{11}'); grid; xlim([1 N]); h = findobj(gcf,'type','line'); set(h,'linewidth',2);
    subplot(2,3,2); plot(F12); title('F_{12}'); grid; xlim([1 N]); h = findobj(gcf,'type','line'); set(h,'linewidth',2);
    subplot(2,3,3); plot(F13); title('F_{13}');grid; xlim([1 N]); h = findobj(gcf,'type','line'); set(h,'linewidth',2);
    subplot(2,3,4); plot(F21); title('F_{21}');grid; xlim([1 N]); h = findobj(gcf,'type','line'); set(h,'linewidth',2);
    subplot(2,3,5); plot(F22); title('F_{22}');grid; xlim([1 N]); h = findobj(gcf,'type','line'); set(h,'linewidth',2);
    subplot(2,3,6); plot(F23); title('F_{23}');grid; xlim([1 N]); h = findobj(gcf,'type','line'); set(h,'linewidth',2);
figure;
    subplot(2,1,1); plot(g11); title('g_{1}');grid; xlim([1 N]); h = findobj(gcf,'type','line'); set(h,'linewidth',2);
    subplot(2,1,2); plot(g21); title('g_{2}');grid; xlim([1 N]); h = findobj(gcf,'type','line'); set(h,'linewidth',2);
    
unique_controls.CR(1).set(1) = solution.xopt.Set(1);
unique_controls.CR(1).F = solution.xopt.Set(1).Functions('primal').F;
unique_controls.CR(1).g = solution.xopt.Set(1).Functions('primal').g;

for i = 1:N
    for j = 1:size(unique_controls.CR,2)
        is_gain_equal = solution.xopt.Set(i).Functions('primal').F == unique_control.CR(j).F;
        is_const_equal = solution.xopt.Set(i).Functions('primal').g == unique_control.CR(j).g
        
        if (is_gain_equal && is_const_equal)
            nSets = size(unique_controls.CR(j).set,2)

%     for i=1:N
%         R_equal{i} = find((F11==F11(i))+(F12==F12(i))+(F13==F13(i))+(F21==F21(i))+(F22==F22(i))+(F23==F23(i))+(g11==g11(i))+(g21==g21(i))==8);
%     end;
%         R_equal_indx = [1:N; ones(1,N)];
%     
%     j = 1; counter = 0;
%     while j<=N
%         counter = counter+1;
%         comb_reg.set(counter).r = R_equal{j};
%         comb_reg.set(counter).region = solution.xopt.Set(R_equal{j});
%         comb_reg.set(counter).F = solution.xopt.Set(R_equal{j}(1)).Functions('primal').F;
%         comb_reg.set(counter).g = solution.xopt.Set(R_equal{j}(1)).Functions('primal').g;
%         for i=1:size(R_equal{j},2)
%             R_equal_indx(2,R_equal{j}(i)) = 0;   
%         end;
%         j = R_equal{j}(end)+1;
%         figure; comb_reg.set(counter).region.plot;
%     end;
%         comb_reg.N = counter
     
%     figure; hold on; for i=1:comb_reg.N comb_reg.set(i).region.plot; end;
%% Cost
for i=1:N
    F_obj = solution.xopt.Set(i).Functions('obj').F;
    g_obj = solution.xopt.Set(i).Functions('obj').g;
    F11_obj(i) = F_obj(1,1);    F12_obj(i) = F_obj(1,2);    F13_obj(i) = F_obj(1,3);
    g11_obj(i) = g_obj(1,1);
end;
figure; 
    subplot(1,3,1); plot(F11_obj); grid; xlim([1 N]); h = findobj(gcf,'type','line'); set(h,'linewidth',2);
    subplot(1,3,2); plot(F12_obj); grid; xlim([1 N]); h = findobj(gcf,'type','line'); set(h,'linewidth',2);
    subplot(1,3,3); plot(F13_obj); grid; xlim([1 N]); h = findobj(gcf,'type','line'); set(h,'linewidth',2);
figure;
    plot(g11_obj); grid; xlim([1 N]); h = findobj(gcf,'type','line'); set(h,'linewidth',2);
    
    
    