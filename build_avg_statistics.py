import glob
import sys
#import pdb

if sys.argv[1].startswith("d"):  # Building latency statistics
    d = sys.argv[1][1:]
    pattern = 'D' + d
    
    # Scan files reporting the number of blocks in the blockchain for different delays and compute the average for each cycle 
    files_scanned = 0
    blocks = []
    to_scan = [f for f in glob.glob('docs/statistics/latency_R*') if pattern in f ] 
    for filename in to_scan:    
        with open(filename) as in_file:
            cycle = 0
            for line in in_file:                    
                if files_scanned == 0:  # the first time we have to populate the arrays
                    if line.startswith("#"):
                        blocks.append(None)   
                    else:     
                        a = line.split('            ')   # exactly 12 spaces
                        blocks.append(int(a[0]))     
                else:
                    if line.startswith("#"):                            
                        continue                       
                    else:                                  # Calculate the average incrementally                
                        if cycle != 0:                                      
                            a = line.split('            ')                    
                            blocks[cycle] = ( (blocks[cycle] * files_scanned) + int(a[0]) ) / (files_scanned + 1)
                cycle+=1       
            files_scanned+=1
            
            
    with open('docs/statistics/avg/latency_D' + d + '_avg.dat', 'w+') as out_file:
                outs= '# Cycle blocks \n'
                for count in range(1, len(blocks)):
                    outs = outs + str(count) + '     ' + str(blocks[count]) + '\n'
                out_file.write(outs) 
                
else:                         # Building all other statistics    
    p = sys.argv[1]
    pattern = 'P'+p[:-1]

    # Scan files reporting the number of honest/fraudolent blocks in the blockchain and compute the average for each cycle
    honest_blocks = []
    fraudolent_blocks = []
    files_scanned = 0
    to_scan = [f for f in glob.glob('docs/statistics/blockchain_R*') if pattern in f ] 
    for filename in to_scan:  
        with open(filename) as in_file:
            print filename
            cycle = 0
            for line in in_file:                    
                if files_scanned == 0:  
                    if line.startswith("#"):
                        honest_blocks.append(None)
                        fraudolent_blocks.append(None)
                    else:
                        a = line.split('            ')   
                        honest_blocks.append(int(a[0]))
                        fraudolent_blocks.append(int(a[1]))
                else:
                    if line.startswith("#"):                
                        continue
                    else:                                                 
                        if cycle != 0:                                      
                            a = line.split('            ') 
                            honest_blocks[cycle] = ( (honest_blocks[cycle] * files_scanned) + int(a[0]) ) / (files_scanned + 1)               
                            fraudolent_blocks[cycle] = ( ( fraudolent_blocks[cycle] * files_scanned ) + int(a[1]) ) / (files_scanned + 1)
                cycle+=1       
            files_scanned+=1            
            
    with open('docs/statistics/avg/blockchain_P' + p + '_avg.dat', 'w+') as out_file:
                outs= '# Cycle Honest_Blocks  Fraudolent_Blocks \n'
                for count in range(1, len(honest_blocks)):
                    outs = outs + str(count) + '     ' + str(honest_blocks[count]) + '     ' + str(fraudolent_blocks[count]) + '\n'
                out_file.write(outs)    
            
 
    # Scan files reporting the number of forks in the blockchain and compute the average for each cycle 
    files_scanned = 0
    forks = []
    to_scan = [f for f in glob.glob('docs/statistics/forks_R*') if pattern in f ] 
    for filename in to_scan:    
        with open(filename) as in_file:
            print filename
            cycle = 0
           # pdb.set_trace() 
            for line in in_file:                    
                if files_scanned == 0:  
                    if line.startswith("#"):
                        forks.append(None)   
                    else:     
                        a = line.split('            ')   
                        forks.append(int(a[0]))     
                else:
                    if line.startswith("#"):                            
                        continue                       
                    else:                                               
                        if cycle != 0:                                      
                            a = line.split('            ')                                              
                            forks[cycle] = ( (forks[cycle] * files_scanned) + int(a[0]) ) / (files_scanned + 1)
                cycle+=1       
            files_scanned+=1                    
            
    with open('docs/statistics/avg/forks_P' + p + '_avg.dat', 'w+') as out_file:
                outs= '# Cycle Forks \n'
                for count in range(1, len(forks)):
                    outs = outs + str(count) + '     ' + str(forks[count]) + '\n'    
                out_file.write(outs)   
            
            
    # Compute the ratio mined_blocks/hash_rate for honest and selfish miners
    honest_hr = []
    selfish_hr = []
    files_scanned = 0
    to_scan = [f for f in glob.glob('docs/statistics/hashrate_R*') if pattern in f ] 
    for filename in to_scan:  
        with open(filename) as in_file:
            cycle = 0
            for line in in_file:                    
                if files_scanned == 0:  
                    if line.startswith("#"):    
                        honest_hr.append(None)
                        selfish_hr.append(None)
                    else:
                        a = line.split('            ')   
                        honest_hr.append(int(a[0]))
                        selfish_hr.append(int(a[1]))
                else:
                    if line.startswith("#"):                
                        continue
                    else:                                                
                        if cycle != 0:                                      
                            a = line.split('            ') 
                            honest_hr[cycle] = ( (honest_hr[cycle] * files_scanned) + int(a[0]) ) / (files_scanned + 1)               
                            selfish_hr[cycle] = ( ( selfish_hr[cycle] * files_scanned ) + int(a[1]) ) / (files_scanned + 1)
                cycle+=1       
            files_scanned+=1            
            
    with open('docs/statistics/avg/hashrate_P' + p + '_avg.dat', 'w+') as out_file:
                outs= '# P(SMiner) HonestBlocks/Ghr  SelfishBlocks/Ghr \n'
                honest_ratio = float(honest_blocks[len(honest_blocks)-1]) / ((honest_hr[1] / 1000000000 ) + 1) # Add 1 to avoid division by zero
                selfish_ratio = float(fraudolent_blocks[len(fraudolent_blocks)-1]) / ((selfish_hr[1] / 1000000000) + 1 )            
                outs = outs + p + '        ' + str(honest_ratio) + '               ' + str(selfish_ratio) 
                out_file.write(outs)     
                
                
                
    # Compute the ratio reward/hash_rate for honest and selfish miners
    honest_reward = []
    selfish_reward = []
    files_scanned = 0
    to_scan = [f for f in glob.glob('docs/statistics/reward_R*') if pattern in f ] 
    for filename in to_scan:  
        with open(filename) as in_file:
            cycle = 0
            for line in in_file:                    
                if files_scanned == 0:  
                    if line.startswith("#"):    
                        honest_reward.append(None)
                        selfish_reward.append(None)
                    else:
                        a = line.split('            ')   
                        honest_reward.append(int(a[0]))
                        selfish_reward.append(int(a[1]))
                else:
                    if line.startswith("#"):                
                        continue
                    else:                                                
                        if cycle != 0:                                      
                            a = line.split('            ') 
                            honest_reward[cycle] = ( (honest_reward[cycle] * files_scanned) + int(a[0]) ) / (files_scanned + 1)               
                            selfish_reward[cycle] = ( ( selfish_reward[cycle] * files_scanned ) + int(a[1]) ) / (files_scanned + 1)
                cycle+=1       
            files_scanned+=1            
            
    with open('docs/statistics/avg/reward_P' + p + '_avg.dat', 'w+') as out_file:
                outs= '# P(SMiner) HonestReward/Ghr  SelfishReward/Ghr \n'    # Only use the reward computed for the last cycle
                honest_ratio = float(honest_reward[len(honest_reward)-1] / 1000) / ((honest_hr[1] / 1000000000 ) + 1) # Add 1 to avoid division by zero
                selfish_ratio = float(selfish_reward[len(selfish_reward)-1] / 1000) / ((selfish_hr[1] / 1000000000) + 1 )            
                outs = outs + p + '        ' + str(honest_ratio) + '               ' + str(selfish_ratio) 
                out_file.write(outs)       
            
  
     
        
        
