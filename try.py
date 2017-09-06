import os
import sys

if len(sys.argv) != 2: 
    repetitions = 1
else: 
    repetitions = int(sys.argv[1])

# Set parameters values
size = '1000'
cycles = '20'
cycle_length = '1000'
drop = '0'
max_trans_per_block = '50'
reward = '10'
trans_prob = '0.15'
prob_cpu = '0.10'
prob_gpu = '0.30'
prob_fpga = '0.30'
prob_asic = '0.30'
prob_miner = ['0.30']
prob_sminer = [ '0.10', '0.30', '0.50', '0.70','0.90', '1.00'] 
prob_2miners = ['0.05']
delay = ['0', '10', '50', '90']

     
        
# Make the averages of the various statistics
for p in prob_sminer:
    os.system('python build_avg_statistics.py ' + p)
    
for d in delay:
    os.system('python build_avg_statistics.py d' + d)
    
# Plot the averaged statistics    
os.system('python plot_avg_statistics.py')
