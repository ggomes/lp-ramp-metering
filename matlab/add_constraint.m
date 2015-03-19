function [cons, cons_counter, consMap] = add_constraint(cons,k,K_dem,cons_counter,consMap,n_opt,f_opt,l_opt,r_opt)
% This function adds the required constraints for every time step
NetworkData;
if k>K_dem
    d1 = 0; d2 = 0; d3 = 0;
end;

% Free Flow
cons = [cons, f_opt(1,k) <= beta1_bar*v1*n_opt(1,k) + beta1_bar*v1*Gamma*d1 + beta1_bar*v1*Gamma*r_opt(1,k)];
cons_counter = cons_counter + 1;
key = strcat('MLFLW_FF[0][',num2str(k-1),']');
consMap(key) = cons_counter;

cons = [cons, f_opt(2,k) <= beta2_bar*v2*n_opt(2,k) +  beta2_bar*v2*Gamma*r_opt(2,k)];
cons_counter = cons_counter + 1;
key = strcat('MLFLW_FF[1][',num2str(k-1),']');
consMap(key) = cons_counter;

% Main-line Conservation
cons = [cons, n_opt(1,k+1) == n_opt(1,k) - (beta1_bar^-1)*f_opt(1,k) + d1 + r_opt(1,k)];
cons_counter = cons_counter + 1;
key = strcat('MLCONS[0][',num2str(k-1),']');
consMap(key) = cons_counter;

cons = [cons, n_opt(2,k+1) == n_opt(2,k) + f_opt(1,k) + r_opt(2,k) - (beta2_bar^-1)*f_opt(2,k)];
cons_counter = cons_counter + 1;
key = strcat('MLCONS[1][',num2str(k-1),']');
consMap(key) = cons_counter;

% On-ramp Conservation
cons = [cons, l_opt(1,k+1) == l_opt(1,k) - r_opt(1,k)+ d2];
cons_counter = cons_counter + 1;
key = strcat('ORCONS[0][',num2str(k-1),']');
consMap(key) = cons_counter;

cons = [cons, l_opt(2,k+1) == l_opt(2,k) - r_opt(2,k)+ d3];
cons_counter = cons_counter + 1;
key = strcat('ORCONS[1][',num2str(k-1),']');
consMap(key) = cons_counter;

% MainLine Congestion
cons = [cons, f_opt(1,k) <= w2*n2_jam-w2*n_opt(2,k)-w2*Gamma*r_opt(2,k)];
cons_counter = cons_counter + 1;
key = strcat('MLFLW_CNG[0][',num2str(k-1),']');
consMap(key) = cons_counter;

% OR Flow
cons = [cons, r_opt(1,k) <= v_ramp*(d2 + l_opt(1,k))];
cons_counter = cons_counter + 1;
key = strcat('ORFLW_DEM[0][',num2str(k-1),']');
consMap(key) = cons_counter;

cons = [cons, r_opt(2,k) <= v_ramp*(d3 + l_opt(2,k))];
cons_counter = cons_counter + 1;
key = strcat('ORFLW_DEM[1][',num2str(k-1),']');
consMap(key) = cons_counter;

% OR Upper Bounds
cons = [cons, r_opt(1,k) <= r1_bar];
cons_counter = cons_counter + 1;
key = strcat('UB_r[0][',num2str(k-1),']');
consMap(key) = cons_counter;

cons = [cons, r_opt(2,k) <= r2_bar];
cons_counter = cons_counter + 1;
key = strcat('UB_r[1][',num2str(k-1),']');
consMap(key) = cons_counter;

% OR Lower Bounds
cons = [cons, 0 <= r_opt(1,k)];
cons_counter = cons_counter + 1;
key = strcat('LB_r[0][',num2str(k-1),']');
consMap(key) = cons_counter;

cons = [cons, 0 <= r_opt(2,k)];
cons_counter = cons_counter + 1;
key = strcat('LB_r[1][',num2str(k-1),']');
consMap(key) = cons_counter;

% ML Upper Bounds
cons = [cons, f_opt(1,k) <= f1_bar];
cons_counter = cons_counter + 1;
key = strcat('UB_f[0][',num2str(k-1),']');
consMap(key) = cons_counter;

cons = [cons, r_opt(2,k) <= f2_bar];
cons_counter = cons_counter + 1;
key = strcat('UB_f[1][',num2str(k-1),']');
consMap(key) = cons_counter;






