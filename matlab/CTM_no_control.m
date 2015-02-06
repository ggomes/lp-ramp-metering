function [n,l,f,r,cost] = CTM_no_control(n0,l0)
NetworkData;
n = zeros(2,K+1);
l = zeros(2,K+1);
f = zeros(2,K);
r = zeros(2,K);
s = zeros(1,K);
n(:,1) = n0;
l(:,1) = l0;
for k = 1:K
    
    if i>K_dem
        d1 = 0; d2 = 0; d3 = 0;
    end;
    r(1,k) = min([l(1,k)+d2; N_c*(n1_jam-n(1,k))]);
    % r(1,k) = min([l(1,k)+d2; N_c*(n1_jam-n(1,k)); r1_bar]);
    r(2,k) = min([l(2,k)+d3; N_c*(n2_jam-n(2,k))]);
    % r(2,k) = min([l(2,k)+d3; N_c*(n2_jam-n(2,k)); r2_bar]);
    f(1,k) = min([beta1_bar*v1*(n(1,k)+Gamma*r(1,k)); w2*(n2_jam-n(2,k)-Gamma*r(2,k));f1_bar]);
    f(2,k) = min([beta2_bar*v2*(n(2,k)+Gamma*r(2,k));f2_bar]);
    s(1,k) = v1*(n(1,k)+Gamma*r(1,k));
    n(1,k+1) = n(1,k)+d1+r(1,k)-f(1,k)/beta1_bar;
    n(2,k+1) = n(2,k)+f(1,k)+r(2,k)-f(2,k)/beta2_bar;
    l(1,k+1) = l(1,k)+d2-r(1,k);
    l(2,k+1) = l(2,k)+d3-r(2,k);
end
cost = sum(sum(n)) + sum(sum(l)) - sum(sum(r)) - sum(sum(f));

