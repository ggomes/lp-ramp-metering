%% Explicit MPC of LP
% Network Data
sim_dt = 3;
gamma = 1;
N = 5;

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
%% LTI construction
A = eye(5,5);

B = [-beta1_bar^-1   0   0   0   0
    1    -beta2_bar^-1   0   1   0
    0    1  --beta3_bar^-1   0   1
    0    0         0        -1   0
    0    0         0         0  -1];

f = [d1
     0
     0
     d2
     d3];
 
Ts = 3;
 
sys = LTISystem('A', A, 'B', B, 'f', f, 'Ts', Ts);

%% X & U polytope constructions
% Constructing x and u polytope in higher dimensions
Aineq = [-beta1_bar*v1  0  0  0  0  1  0  0  0  0
         0  -beta2_bar*v2  0  0  0  0  1  0  -beta2_bar*v2*gamma  0
         0  0  -beta3_bar*v3  0  0  0  0  1  0  -beta3_bar*v3*gamma
     
         0  w1  0  0  0  1  0  0 w1*gamma 0
         0  0  w2  0  0  0  1  0  0  w1*gamma
     
         0  0  0  -1  0  0  0  0  1  0
         0  0  0  0  -1  0  0  0  0  1];
 
bineq = [beta1_bar*v1*d1
                 0
                 0
             w2*n2_jam
             w3*n3_jam
                d2
                d3];
            
u_max = [f1_bar
         f2_bar
         f3_bar
         r1_bar
         r2_bar];
     
u_min = [-inf
         -inf
         -inf 
           0 
           0];
       
XUpolytope = Polyhedron('A', Aineq, 'b', bineq); 
% Xpolytope = XUpolytope.projection(5);
Upolytope = XUpolytope.projection(6:10);

AineqXinit = [eye(5)
             -eye(5)];
         
bineqXinit = [n1_jam
              n2_jam
              n3_jam
              l1_jam
              l2_jam
             -zeros(5,1)];
         
AeXinit = [0 0 0 0 0
           0 0 0 0 0
           0 0 0 0 0
           0 0 0 1 0
           0 0 0 0 1];
       
beXinit = [0
           0
           0
           0
           0];
       
Xinit = Polyhedron('A',AineqXinit,'b',bineqXinit,'Ae',AeXinit,'be',beXinit);       
%% Explicit MPC
sys.u.min = u_min;
sys.u.max = u_max;

sys.u.with('setConstraint');
% sys.x.setConstraint = Xpolytope;
sys.u.setConstraint = Upolytope;

sys.x.with('initialSet');
sys.x.initialSet = Xinit;

sys.u.penalty = AffFunction(-etha*ones(1,5));
sys.x.penalty = AffFunction(ones(1,5));

ctrl = MPCController(sys, N);
explicit_ctrl = ctrl.toExplicit();


       
            
     

 
