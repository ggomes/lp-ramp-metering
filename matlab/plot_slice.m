function [  ] = plot_slice(data,slice_dim,slice_dim_val)

if(~ismember(slice_dim,[1 2 3]))
    error('bad dim value')
end

ind = data.ic(:,slice_dim)==slice_dim_val;
if(isempty(ind))
    error('no values found')
end

keep_dims = setdiff([1 2 3],slice_dim);

ic = data.ic(ind,keep_dims);
% tvh = data.tvh(ind);
cost = data.cost(ind);


[ic1,ic2,Z] = make_a_matrix(ic,cost);
    
figure  
surf(ic1,ic2,Z)
xlabel(num2str(keep_dims(1)))
ylabel(num2str(keep_dims(2)))

function [ic1,ic2,Z] = make_a_matrix(ic,z)
ic1 = sort(unique(ic(:,1)));
ic2 = sort(unique(ic(:,2)));
nic1 = length(ic1);
nic2 = length(ic2);
Z = nan(nic1,nic2);
for i=1:nic1
    for j=1:nic2
        ind = ic(:,1)==ic1(i) & ic(:,2)==ic2(i);
        if(any(ind))
            Z(i,j) = z(ind);
        end
    end
end

