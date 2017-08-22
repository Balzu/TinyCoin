import glob

honest_blocks = []
fraudolent_blocks = []

files_scanned = 0
for filename in glob.glob('docs/statistics/*blockchain_R*'):    
    with open(filename) as in_file:
        cycle = 0
        for line in in_file:
            if line.startswith("#"):
                honest_blocks.append(None)
                fraudolent_blocks.append(None)
                continue        
            if files_scanned == 0:  # the first time we have to populate the arrays
                a = line.split('            ')   #exactly 12 spaces
                honest_blocks.append(int(a[0]))
                fraudolent_blocks.append(int(a[1]))
            else:                                  # Calculate the average incrementally                
                if cycle != 0:                                      
                    a = line.split('            ')                  
                    print a[0]    
                    honest_blocks[cycle] = ( (honest_blocks[cycle] * files_scanned) + int(a[0]) ) / (files_scanned + 1)
               # print honest_blocks
               # print honest_blocks[cycle]                
                    fraudolent_blocks[cycle] = ( ( fraudolent_blocks[cycle] * files_scanned ) + int(a[1]) ) / (files_scanned + 1)
            cycle+=1       
        files_scanned+=1
            
            
with open('docs/statistics/blockchain_avg', 'w+') as out_file:
            outs= '# Cycle Honest_Blocks  Fraudolent_Blocks \n'
            for count in range(1, len(honest_blocks)-1):
                outs = outs + str(count) + '     ' + str(honest_blocks[count]) + '     ' + str(fraudolent_blocks[count]) + '\n'
            out_file.write(outs)
                
            
        
        
        
