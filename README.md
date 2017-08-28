## TinyCoin: Simulating fraudolent mining strategies in a simplified Bitcoin Network

Final Project for the course of P2P Systems of the Master Degree in Computer Science and Networking

### Folder structure
The *src* folder contains the source of the project, the *lib* folder the library needed to run the Peersim Simulator, the *bin* folder contains the classes compiled and packaged in a .jar file and the *docs* folder contains the relation of the project and two subfolders *statistics*, containing the statistics collected by running the program, and *plots*, containing the plots of the statistics. The subfolders inside *docs* are populated after a simulation is executed.
 
### Running an experiment
Running an experiment means running a single run of the Peersim Simulator with the configuration parameters defined in *tinycoin_config.txt*. To run an experiment you simply open the terminal and type 

> java -jar bin/tinycoin.jar peersim.mulator tinycoin_config.txt

### Running a simulation
A simulation consists in choosing a set of values for the parameters and, for each combination of such values, in runnning the experiment for a given number of times. At the end the experiment will be carried out several times with different parameters values and the results of the repeated execution of the same experiment will be averaged and plotted. 
It is possible to set up a simulation thanks to the use of scripts: 

* start_simulation.py
* build_avg_statistics.py
* plot_avg_statistics.py


