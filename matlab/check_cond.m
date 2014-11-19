% initialization
    clear all;
    clc;
    cd('C:\Users\Omid\Dropbox\ME290J Project');
% inputs
    K_dem = 10;
    K_cool = 15;
    N = K_dem+K_cool;
    n(1,:) = [1:1:6 6.3 6.5 6.4 6:-0.5:0.5 0.3 0.2 0.1 0.15];
    n(2,:) = [1:0.5:3 3.3 3.2 3.1 3:-0.25:0.5 0.3 0.2 0.1 0.15 0.05 0.1];
    n(3,:) = [1:1:6 6.1 6.4 6.3 6.3:-0.5:0.5 0.3 0.2 0.1 0.15];
    l(1,:) = [1:1:6 6.3 6.5 6.4 6:-0.5:0.5 0.3 0.2 0.1 0.15];
    l(2,:) = [1:1:6 6.3 6.5 6.4 6:-0.5:0.5 0.3 0.2 0.1 0.15];
%% condition for demand
    for i =1:3 n_grd(i,:) = gradient(n(i,:)); end;
    l_dem = 3;
    for i = 1:3
        if (abs(n_grd(i,K_dem-l_dem:K_dem-1))<0.3)
            check_dem = 1;
        else
            check_dem = 0;
        end;
    end;
%% condition for cool down
    l_cool = 3;
    for i = 1:3
        if (abs(n_grd(i,N-l_cool+1:N))<0.3) & (n(i,N-l_dem+1:N)<0.3)
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
%% figures
    figure('name','n'); 
        subplot(3,1,1); hold on; plot(n(1,:)); plot(K_dem,n(1,K_dem),'r*'); grid;
        subplot(3,1,2); hold on; plot(n(2,:)); plot(K_dem,n(2,K_dem),'r*'); grid;
        subplot(3,1,3); hold on; plot(n(3,:)); plot(K_dem,n(3,K_dem),'r*'); grid;
