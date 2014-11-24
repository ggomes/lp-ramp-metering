%% Explicit MPC of LP
% Network Data
clear all;
clc;
sim_dt = 3;
gamma = 1;
N = 5;

beta1 = 0;
beta2 = 0.1;
beta3 = 0;

d1 = 1 * sim_dt;
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
cons = [cons, 0<= n <=n1_jam];
cons = [cons, f <= f1_bar];
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
%cons = [cons, l(:,1)==[0;0]];
for i = 1:K_dem
    % Conservations
    cons = [cons, n(1,i+1) == n(1,i) - (beta1_bar^-1)*f(1,i) + d1 + r(1,i)];
    cons = [cons, n(2,i+1) == n(2,i) + f(1,i) - (beta2_bar^-1)*f(2,i)];
    cons = [cons, n(3,i+1) == n(3,i) + f(2,i) + r(2,i)- (beta3_bar^-1)*f(3,i)];
    cons = [cons, l(1,i+1) == l(1,i) - r(1,i)+ d2];
    cons = [cons, l(2,i+1) == l(2,i) - r(2,i)+ d3];
    % MainLine FreeFlow
    cons = [cons, f(1,i) <= beta1_bar*v1*n(1,i) + beta1_bar*v1*gamma*d1 + beta1_bar*v1*gamma*r(1,i)];
    cons = [cons, f(2,i) <= beta2_bar*v2*n(2,i)];
    cons = [cons, f(3,i) <= beta3_bar*v3*gamma*r(2,i) + beta3_bar*v3*n(3,i)];
    % MainLine Congestion
    cons = [cons, f(1,i) <= w2*n2_jam-w2*n(2,i)];
    cons = [cons, f(2,i) + w3*gamma*r(2,i)<= w3*n3_jam-w3*n(3,i)];
    % OR Flow
    cons = [cons, r(1,i) <= d2 + l(1,i)];
    cons = [cons, r(2,i) <= d3 + l(2,i)];
end

for i = K_dem+1:K
    % Conservations
    cons = [cons, n(1,i+1) == n(1,i) - (beta1_bar^-1)*f(1,i) + r(1,i)];
    cons = [cons, n(2,i+1) == n(2,i) + f(1,i) - (beta2_bar^-1)*f(2,i)];
    cons = [cons, n(3,i+1) == n(3,i) + f(2,i) + r(2,i)- (beta3_bar^-1)*f(3,i)];
    cons = [cons, l(1,i+1) == l(1,i) - r(1,i)];
    cons = [cons, l(2,i+1) == l(2,i) - r(2,i)];
    % MainLine FreeFlow
    cons = [cons, f(1,i) <= beta1_bar*v1*n(1,i) + beta1_bar*v1*gamma*r(1,i)];
    cons = [cons, f(2,i) <= beta2_bar*v2*n(2,i)];
    cons = [cons, f(3,i) <= beta3_bar*v3*gamma*r(2,i) + beta3_bar*v3*n(3,i)];
    % MainLine Congestion
    cons = [cons, f(1,i) <= w2*n2_jam-w2*n(2,i)];
    cons = [cons, f(2,i) + w3*gamma*r(2,i)<= w3*n3_jam-w3*n(3,i)];
    % OR Flow
    cons = [cons, r(1,i) <= l(1,i)];
    cons = [cons, r(2,i) <= l(2,i)];
end

MPLP = Opt(cons,cost,n(:,1),r(:,1));
solution = MPLP.solve();
