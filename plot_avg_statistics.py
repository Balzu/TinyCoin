import glob
import os

with open ("docs/plots/template.gnu") as filein:
    plot_template = filein.read()
    
# Plot graphs of blocks mined by honest miners/selfish miners
blockchain_template = plot_template
blockchain_template += 'set xlabel \'Cycles \' \nset ylabel\'Mined Blocks \' \n'
to_plot = [f for f in glob.glob('docs/statistics/avg/blockchain*')] 
for filename in to_plot:  
    blockchain_plot = blockchain_template 
    p = filename.split('P')[1][:4]
    blockchain_plot += 'set output \'docs/plots/blockchain_P' + p + '.png\' \n'     # Sets the name of the output file
    blockchain_plot += 'plot \'' + filename + '\' u 1:2 t \'Honest blocks\' w lp ls 1, \\\n\'\'                  u 1:3 t \'Fraudolent blocks\' w lp ls 2'
    with open('docs/plots/temp.gnu', 'w+') as file:
            file.write(blockchain_plot)
    os.system ('gnuplot docs/plots/temp.gnu')
            
            
# Plot graphs of forks
forks_plot = plot_template
forks_plot += 'set xlabel \'Cycles \' \nset ylabel\'Forks \' \n'
forks_plot += 'set output \'docs/plots/forks.png\' \nplot'
to_plot = [f for f in glob.glob('docs/statistics/avg/forks*')] 
count = 0
for filename in to_plot:
    p = filename.split('P')[1][:4]    
    count += 1
    forks_plot += ' \'' + filename + '\' u 1:2 t \'P(selfish miner) = ' + p + '\' w lp ls ' + str(count) + ', \\\n'
with open('docs/plots/temp.gnu', 'w+') as file:
        file.write(forks_plot)
os.system ('gnuplot docs/plots/temp.gnu')



# Plot mined_blocks/ hash_rate graph
# Make one file of the several ones
to_merge = [f for f in glob.glob('docs/statistics/avg/hashrate_P*')] 
hr_file = ''
for filename in to_merge:
    with open(filename) as in_file:        
        hr_file = hr_file + in_file.read() + '\n'
with open('docs/statistics/avg/hashrate_avg_merged.dat', 'w+') as file:
        file.write(hr_file)
hr_plot = plot_template
hr_plot += 'set xlabel \'P(selfish miner) \' \nset ylabel\'Mined blocks / Hash rate \' \n'
hr_plot += 'set output \'docs/plots/blocks_per_hashrate.png\' \n'
hr_plot += 'plot \'docs/statistics/avg/hashrate_avg_merged.dat\' u 1:2 smooth unique t \'Honest miners\' w lp ls 1, \\\n\'\'                  u 1:3 smooth unique t \'Selfish miners\' w lp ls 2'
with open('docs/plots/temp.gnu', 'w+') as file:
        file.write(hr_plot)
os.system ('gnuplot docs/plots/temp.gnu')

# Plot reward / hash_rate graph
# Make one file of the several ones
to_merge = [f for f in glob.glob('docs/statistics/avg/reward_P*')] 
rew_file = ''
for filename in to_merge:
    with open(filename) as in_file:        
        rew_file = rew_file + in_file.read() + '\n'
with open('docs/statistics/avg/reward_avg_merged.dat', 'w+') as file:
        file.write(rew_file)
rew_plot = plot_template
rew_plot += 'set xlabel \'P(selfish miner) \' \nset ylabel\'Reward / Hash rate \' \n'
rew_plot += 'set output \'docs/plots/reward_per_hashrate.png\' \n'
rew_plot += 'plot \'docs/statistics/avg/reward_avg_merged.dat\' u 1:2 smooth unique t \'Honest miners\' w lp ls 1, \\\n\'\'                  u 1:3 smooth unique t \'Selfish miners\' w lp ls 2'
with open('docs/plots/temp.gnu', 'w+') as file:
        file.write(rew_plot)
os.system ('gnuplot docs/plots/temp.gnu')


# Plot graphs of mined blocks for different values of message delay
delay_plot = plot_template
delay_plot += 'set xlabel \'Cycles \' \nset ylabel\'Mined Blocks \' \n'
delay_plot += 'set output \'docs/plots/blocks_with_delay.png\' \nplot'
to_plot = [f for f in glob.glob('docs/statistics/avg/latency*')] 
count = 0
for filename in to_plot:
    p = filename.split('D')[1][:1]    
    count += 1
    delay_plot += ' \'' + filename + '\' u 1:2 t \'Delay = 0.' + p + '0\' w lp ls ' + str(count) + ', \\\n'
with open('docs/plots/temp.gnu', 'w+') as file:
        file.write(delay_plot)
os.system ('gnuplot docs/plots/temp.gnu')
