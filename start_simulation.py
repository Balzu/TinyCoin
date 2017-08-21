#!/usr/bin/python
import sys

# Define the parameters of the simulation
# If the number of repetitions is specified by command line we use it, otherwise the default is to repeat the simulation just one time


if len(sys.argv) != 2: 
    repetitions = 1
else: 
    repetitions = sys.argv[1]

max_trans_per_block = '50'
reward = '10'
trans_prob = '0.15'
prob_cpu = '0.10'
prob_gpu = '0.30'
prob_fpga = '0.30'
prob_asic = '0.30'
prob_miner = ['0.30']
prob_sminer = ['0.10', '0.30', '0.50', '0.70', '0.90', '1.00']
prob_2miners = ['0.05']



# Read in the file
with open('tinycoin_config_for_script.txt', 'r') as file :
  config = file.read()

# Substitute in the configuration file the parameters that have a single value
config = config.replace('MAX_TRANS_PER_BLOCK', max_trans_per_block)
config = config.replace('REWARD', reward)
config = config.replace('TRANS_PROB', trans_prob)
config = config.replace('PROB_CPU', prob_cpu)
config = config.replace('PROB_GPU', prob_gpu)
config = config.replace('PROB_FPGA', prob_fpga)
config = config.replace('PROB_ASIC', prob_asic)
config = config.replace('PROB_MINER', prob_miner[0])
config = config.replace('PROB_2MINERS', prob_2miners[0])



repetitions = range(1,repetitions+1)
for count in repetitions:
# Replace the parameters in the configuration file   
    config_overwrite = config 
    config_overwrite = config_overwrite.replace('REPETITION', str(count))
    for p in prob_sminer:
        config_overwrite2 = config_overwrite  
        config_overwrite2 = config_overwrite2.replace('PROB_SMINER', p)
        # Write the file out again
        with open('tinycoin_config_overwritten.txt', 'w+') as file:
            file.write(config_overwrite2)
        
        #os.system("python runall.py --list > list")





