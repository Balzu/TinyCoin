#reset

# define axis
# remove border on top and right and set color to gray
set style line 11 lc rgb '#808080' lt 1
set border 3 back ls 11
set tics nomirror
# define grid
set style line 12 lc rgb '#808080' lt 0 lw 1
set grid back ls 12

# color definitions
set style line 1 lc rgb '#8b1a0e' pt 1 ps 1 lt 1 lw 2 # --- red
set style line 2 lc rgb '#5e9c36' pt 6 ps 1 lt 1 lw 2 # --- green
set style line 3 lc rgb '#440154' pt 6 ps 1 lt 1 lw 2 # --- dark purple
set style line 4 lc rgb '#3b518b' pt 6 ps 1 lt 1 lw 2 # --- blue
set style line 5 lc rgb '#472c7a' pt 6 ps 1 lt 1 lw 2 # --- purple
set style line 6 lc rgb '#ffd700' pt 6 ps 1 lt 1 lw 2 # --- yellow

#set xrange [0:50]
#set yrange [0:50]

set key left top
set terminal pngcairo size 500,500 enhanced font 'Verdana,9'

## To be set from script
#set xlabel 'x axis label'
#set ylabel 'y axis label'
#set output 'blockchain.png'
#plot '../statistics/blockchain_avg.dat' u 1:2 t 'Honest blocks' w lp ls 1, \
#     ''                  u 1:3 t 'Fraudolent blocks' w lp ls 2
